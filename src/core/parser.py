from src.utils.namespace_utils import ObjectPath, TYPE_TYPE, FunctionObject, ValueObject, PreFunctionObject, LinkObject
from src.utils.parser_utils import TreeBuilder, ErrorSegmentAnchor, ErrorBuffer, BodyParsingContext
from src.utils.namespace_utils import ClassObject, Accessibility, NotInitedModuleObject
from src.utils.arataki_typing import Result, TextFile, ImmutableObject
from src.core.tokens import Token, Tokens, IteratorBreaker
from src.utils.exceptions import ReturnStatementError
from src.core.namespace import Namespace
from src.utils import as_serial_number
from src.core.errors import Error


class ParserContext(ImmutableObject):
    tokens: list[Token]
    token_index: int
    file: TextFile
    namespace: Namespace
    error_buffer: ErrorBuffer

    def __init__(self, tokens: list[Token], file: TextFile, namespace: Namespace, err_buffer: ErrorBuffer):
        super().__init__('token_index', 'error_buffer')
        self.tokens, self.file, self.namespace = tokens, file, namespace

        self.token_index = 0
        self.error_buffer = err_buffer

        self.body_parser = BodyParser(self)
        self.expression_parser = ExpressionParser(self)
        self.name_parser = NameParser(self)
        self.func_parser = None

    @property
    def is_in_func(self):
        return self.func_parser is not None

    def start_subbody(self):
        self.namespace.start_subbody()

        if self.is_in_func:
            self.func_parser.body_level += 1

    def finish_subbody(self):
        self.namespace.finish_subbody()

        if self.is_in_func:
            self.func_parser.body_level -= 1

    def start_func_parsing(self):
        self.func_parser = FunctionParser(self)
        return self.func_parser

    def finish_func_parsing(self):
        self.func_parser = None


class BasicParser(ImmutableObject):
    context: ParserContext

    def __init__(self, context: ParserContext, *allow_mutation: str):
        super().__init__(*allow_mutation)
        self.context = context

    def has_next(self, step=1):
        return self.context.token_index + step < len(self.context.tokens)

    def next(self, step=1):
        if self.has_next(step):
            self.context.token_index += step
            return self.context.tokens[self.context.token_index]
        else:
            return None

    def peek(self, seeker=None, step=1):
        if not self.has_next(step):
            return None

        peekable = self.context.tokens[self.context.token_index + step]

        if seeker is None:
            return peekable

        return seeker(peekable)

    @property
    def token(self):
        return self.context.tokens[self.context.token_index]

    def new_tree(self, type: str, _dict: dict[str, any] = None, **kwargs):
        tree = TreeBuilder(type, self).update(kwargs)

        if _dict is None:
            return tree

        return tree.update(_dict)

    def build_error_after_token(
            self,
            err: Exception = SyntaxError('invalid syntax'),
            anchor: Token | ErrorSegmentAnchor = None
    ):
        return self.build_error(err, lambda _err: _err.move_cursor_at_end(1), anchor)

    def build_error(
            self,
            err: Exception = SyntaxError('invalid syntax'),
            error_seeker=None,
            anchor: Token | ErrorSegmentAnchor = None
    ):
        err = Error(
            err,
            anchor if anchor is not None else self.token,
            self.context.file
        )

        if error_seeker is None:
            err.underscore_segment_by_cursor()
        else:
            error_seeker(err)

        self.context.error_buffer.error = err

        return Result.err(err)

    def build_error_token_before(
            self,
            err: Exception = SyntaxError('invalid syntax'),
            error_seeker=None
    ):
        return self.build_error(err, error_seeker, self.peek(step=-1))

    def build_get_unaccessable_obj_error(
            self,
            accessibility: Accessibility,
            error_seeker=None,
            anchor: Token | ErrorSegmentAnchor = None
    ):
        return self.build_error(ReferenceError(
            f'try to get access to {accessibility.access_modifier} unit `{accessibility.unaccessable_path}`'
        ), error_seeker, anchor)

    def ok_result(self, result):
        return self.context.error_buffer.ok_or_err_result(result)


class FunctionParser(BasicParser):
    args: dict[str, dict[str, any]]

    def __init__(self, context: ParserContext):
        super().__init__(
            context,
            'returnable_type',
            'body_level',
            'has_completed_returnable_statement'
        )
        self.args = dict()
        self.arg_parser = VariableParser.arg_init(self.context)
        self.returnable_type = None
        self.body_level = 0

    def __name_check(self, name: ObjectPath):
        if name[-1] in self.args:
            return self.build_error(NameError(
                'argument with this name is already defined as '
                + as_serial_number(list(self.args.keys()).index(name[-1]) + 1)
                + ' argument'
            ))

        return self.ok_result('OK')

    def parse_return(self):
        tree = self.new_tree('return-value')

        if not self.token.is_kw('return'):
            return self.build_error()

        expression_result = None

        if self.context.expression_parser.token_is_expression():
            self.next()

            expression_result = self.context.expression_parser.parse_value_expression()

            if expression_result.is_err:
                return expression_result

            new_returnable_type = expression_result.ok_value['value-type']
            tree['return-value'] = expression_result.ok_value.build()
        else:
            new_returnable_type = self.context.namespace.none_type

        if self.returnable_type is None:
            self.returnable_type = new_returnable_type

        if new_returnable_type != self.returnable_type:
            exception = TypeError(f'expected returnable type `{self.returnable_type}` but got `{new_returnable_type}`')

            if expression_result is None:
                return self.build_error_after_token(exception)

            return self.build_error(exception, anchor=expression_result.ok_value.as_err_anchor())
        else:
            self.returnable_type = new_returnable_type

        return self.ok_result(tree)

    def parse_arg(self):
        arg_result = self.arg_parser.parse(name_checker=self.__name_check)

        if arg_result.is_err:
            return arg_result

        arg_tree = arg_result.ok_value.build()
        arg_tree['type'] = arg_tree['value-type']

        arg_name = arg_tree['name']

        del arg_tree['start'], arg_tree['end'], arg_tree['name'], arg_tree['value-type']

        self.args[arg_name] = arg_tree

        return self.ok_result('SUCCESSFUL')

    def parse(self, access_modifier: str | None):
        if not self.token.is_kw('fn'):
            return self.build_error()

        func_name_exception = SyntaxError('expected function name')

        if self.next() is None:
            return self.build_error_after_token(func_name_exception)

        if self.token.type != Tokens.ID:
            return self.build_error(func_name_exception)

        func_name = ObjectPath.relative(self.token.value)
        tree = self.new_tree('func-init', {'name': self.token.value, 'access-modifier': access_modifier})

        open_parenthesis_exception = SyntaxError('expected `(`')

        if self.next() is None:
            return self.build_error_after_token(open_parenthesis_exception)

        if not self.token.is_parenthesis('('):
            return self.build_error(open_parenthesis_exception)

        if self.next() is None:
            return self.build_error_after_token(SyntaxError('expected arguments description or `)`'))

        arg_trees_result = self.context.expression_parser.parse_enumeration(
            self.parse_arg,
            IteratorBreaker(Tokens.PARENTHESIS, ')')
        )

        if arg_trees_result.is_err:
            return arg_trees_result

        if self.next() is None:
            return self.build_error_after_token(SyntaxError('expecting returnable type or function body'))

        if self.token.is_op('->'):
            if self.next() is None:
                return self.build_error_after_token(SyntaxError('expected returnable type'))

            type_result = self.context.name_parser.parse_type()

            if type_result.is_err:
                return type_result

            self.returnable_type = type_result.ok_value

            if self.next() is None:
                return self.build_error_after_token()

        if not self.token.is_parenthesis('{'):
            return self.build_error(SyntaxError('expected function body'))

        if self.next() is None:
            return self.build_error_after_token()

        pre_function = PreFunctionObject(self.args, access_modifier)
        self.context.namespace.define(func_name, pre_function)
        self.context.namespace.goto(func_name)

        body_result = self.context.body_parser.parse(BodyParsingContext.funcbody())

        if body_result.is_err:
            return body_result

        if self.body_level != 0:
            return self.build_error_token_before(ReturnStatementError('expected return statement'))

        if self.returnable_type is None:
            self.returnable_type = self.context.namespace.none_type

        self.context.namespace[func_name] = FunctionObject(
            self.args,
            access_modifier,
            self.returnable_type,
            pre_function.body
        )

        tree.update({
            'args': self.args,
            'returnable-type': self.returnable_type,
            'body': body_result.ok_value['body']
        })

        return self.ok_result(tree)


class ClassParser(BasicParser):
    def __init__(self, context: ParserContext):
        super().__init__(context)

        self.var_init_parser = VariableParser.var_init(self.context)

    def parse(self, access_modifier: str):
        if not self.token.is_kw('class'):
            return self.build_error()

        class_name_exception = SyntaxError('expected class name')

        if self.next() is None:
            return self.build_error_after_token(class_name_exception)

        if self.token.type != Tokens.ID:
            return self.build_error(class_name_exception)

        class_name = ObjectPath.relative(self.token.value)
        tree = self.new_tree('class-init', {'name': self.token.value, 'access-modifier': access_modifier})

        open_parenthesis_exception = SyntaxError('expected `{`')

        if self.next() is None:
            return self.build_error_after_token(open_parenthesis_exception)

        if not self.token.is_parenthesis('{'):
            return self.build_error(open_parenthesis_exception)

        if self.next() is None:
            return self.build_error_after_token(SyntaxError('expected class body or `}`'))

        self.context.namespace.define(class_name, ClassObject(access_modifier))
        self.context.namespace.goto(class_name)

        body_result = self.context.body_parser.parse(BodyParsingContext.classbody())

        if body_result.is_err:
            return body_result

        tree['body'] = body_result.ok_value['body']

        return self.ok_result(tree)


class BodyParser(BasicParser):
    def __init__(self, context: ParserContext):
        super().__init__(context)

        self.var_init_parser = VariableParser.var_init(self.context)
        self.var_set_parser = VariableParser.set_value(self.context)

    def finish_block(self):
        if not self.has_next():
            return True

        if self.token.type != Tokens.NEWL:
            self.next()

        if self.token.type == Tokens.NEWL:
            self.next()
            return True

        self.build_error()
        return False

    def parse_sub_body(self):
        if not self.token.is_parenthesis('{'):
            return self.build_error()

        self.context.start_subbody()
        self.next()

        body_result = self.parse(BodyParsingContext.subbody())

        if body_result.is_err:
            return body_result

        self.context.finish_subbody()

        return body_result

    def parse(self, parsing_context: BodyParsingContext):
        body = []
        tree = self.new_tree(parsing_context.body_type, body=body)

        ignore_blocks_except_breaker = False

        while self.has_next():
            parsing_context.set_default_access_modifier()

            if not ignore_blocks_except_breaker:
                if self.token.is_access_modifier():
                    if not parsing_context.access_modifier_is_allowed:
                        return self.build_not_allowed_error('access modifier')

                    parsing_context.access_modifier = self.token.value

                    self.next()

                if self.token.is_kw('return'):
                    if not self.context.is_in_func:
                        return self.build_not_allowed_error('return statement')

                    value_result = self.context.func_parser.parse_return()

                    if value_result.is_err:
                        return value_result

                    body.append(value_result.ok_value.build())
                    ignore_blocks_except_breaker = True
                elif self.token.is_kw('let'):
                    if not parsing_context.is_subbody_like and not self.context.is_in_func and not parsing_context.is_class_body:
                        return self.build_not_allowed_error('variable initialisation')

                    init_result = self.var_init_parser.parse(parsing_context.access_modifier)

                    if init_result.is_err:
                        return init_result

                    body.append(init_result.ok_value.build())
                elif self.token.type == Tokens.ID and self.peek(lambda token: token.is_op('=')):
                    if not parsing_context.is_subbody_like and not self.context.is_in_func:
                        return self.build_not_allowed_error('setting the value')

                    if parsing_context.access_modifier_has_changed:
                        return self.build_access_modifier_not_allowed_error()

                    value_set_result = self.var_set_parser.parse()

                    if value_set_result.is_err:
                        return value_set_result

                    body.append(value_set_result.ok_value.build())
                elif self.token.is_parenthesis('{'):
                    if not parsing_context.is_subbody_like and not self.context.is_in_func:
                        return self.build_not_allowed_error()

                    if parsing_context.access_modifier_has_changed:
                        return self.build_access_modifier_not_allowed_error()

                    sub_body_result = self.parse_sub_body()

                    if sub_body_result.is_err:
                        return sub_body_result

                    body.append(sub_body_result.ok_value.build())
                elif self.token.is_kw('fn'):
                    if not parsing_context.is_root_body and not parsing_context.is_class_body:
                        return self.build_not_allowed_error('function')

                    init_func_result = self.context.start_func_parsing().parse(parsing_context.access_modifier)

                    if init_func_result.is_err:
                        return init_func_result

                    body.append(init_func_result.ok_value.build())
                elif self.token.is_kw('class'):
                    if not parsing_context.is_root_body and not parsing_context.is_class_body:
                        return self.build_not_allowed_error('class')

                    init_func_result = ClassParser(self.context).parse(parsing_context.access_modifier)

                    if init_func_result.is_err:
                        return init_func_result

                    body.append(init_func_result.ok_value.build())
                elif self.token.is_kw('import'):
                    if not parsing_context.is_subbody_like and parsing_context.is_func_body:
                        return self.build_not_allowed_error('import')

                    parse_result = self.parse_import()

                    if parse_result.is_err:
                        return parse_result
                elif self.token.is_kw('typedef'):
                    if not parsing_context.is_root_body:
                        return self.build_not_allowed_error()

                    if not self.peek(lambda token: token.type == Tokens.ID):
                        return self.build_error()

                    self.next()

                    obj = self.context.namespace.define_obj(self.token.value, TYPE_TYPE)

                    if obj is None:
                        return self.build_error(NameError(f'already exists'))

                    obj['access_modifier'] = parsing_context.access_modifier

            if parsing_context.breaker is not None and parsing_context.breaker == self.token:
                self.next()

                if parsing_context.is_func_body or parsing_context.is_class_body:
                    self.context.finish_func_parsing()
                    self.context.namespace.go_up()

                return self.ok_result(tree)

            if not self.finish_block():
                return self.build_error()

        if parsing_context.breaker is not None and parsing_context.breaker.required and self.token != parsing_context.breaker:
            return self.build_error(SyntaxError(f'expected `{parsing_context.breaker.value}` here'))

        return self.ok_result(tree)

    def parse_import(self):
        if not self.peek(lambda token: token.type == Tokens.ID):
            return self.build_error()

        anchor_start = self.next().start
        parsed_path_result = self.context.name_parser.parse_name(until_fail=True)

        if parsed_path_result.is_err:
            return parsed_path_result

        importall = self.peek(lambda token: token.is_sep('.')) and self.peek(lambda token: token.is_op('*'), 2)
        anchor = ErrorSegmentAnchor(anchor_start, self.token.end)
        relative_path = ObjectPath(parsed_path_result.ok_value)

        obj = self.context.namespace[relative_path]

        if obj is None:
            return self.build_error(NameError('does not exist'), anchor=anchor)

        abs_path = self.context.namespace.find_obj_abs_path(relative_path)
        accessibility = self.context.namespace.check_accessibility(abs_path)

        if not accessibility.is_accessable:
            return self.build_get_unaccessable_obj_error(accessibility, anchor=anchor)

        if not importall:
            if self.context.namespace.is_obj_in_this_module(abs_path):
                return self.build_error(ImportError('already exists in this scope'), anchor=anchor)

            link_name = ObjectPath.relative(relative_path[-1])

            if self.context.namespace.define(
                    link_name,
                    LinkObject(self.context.namespace.normalize_path(abs_path))
            ) is None:
                defined_path = self.context.namespace.obj_context_abs_path(link_name)
                return self.build_error(NameError(f'already defined in this scope as `{defined_path}`'))

            return self.ok_result('SUCCESSFULLY')

        self.next(2)

        if isinstance(obj, NotInitedModuleObject):
            obj = obj.init()

        obj_body = obj['body']

        if self.context.namespace.is_obj_in_this_module(abs_path + tuple(obj_body.keys())[0]):
            return self.build_error(
                ImportError('already exists in this scope'),
                anchor=ErrorSegmentAnchor(anchor_start, self.token.end)
            )

        for key in obj['body'].keys():
            link_name = ObjectPath.relative(key)

            if self.context.namespace.define(
                    link_name,
                    LinkObject(self.context.namespace.normalize_path(abs_path + key))
            ) is None:
                defined_path = self.context.namespace.obj_context_abs_path(ObjectPath.relative(key))
                return self.build_error(NameError(f'`{key}` has already defined in this scope as `{defined_path}`'))

        return self.ok_result('SUCCESSFULLY')

    def build_not_allowed_error(self, target: str = None):
        return self.build_error(SyntaxError(
            'not allowed here' if target is None else f'{target} is not allowed here'
        ))

    def build_access_modifier_not_allowed_error(self):
        return self.build_error_token_before(SyntaxError('access modifier is not allowed here'))


class NameParser(BasicParser):
    def parse_name(self, until_fail=False):
        if self.token.type != Tokens.ID:
            return self.build_error()

        full_name = (self.token.value,)

        if not self.has_next():
            return self.ok_result(full_name)

        while (
                (next_is_dot := self.peek(lambda token: token.is_sep('.')))
                and self.peek(lambda token: token.type == Tokens.ID, 2)
        ):
            self.next(2)
            full_name = (*full_name, self.token.value)

        if not until_fail and next_is_dot:
            return self.build_error(anchor=self.next())

        return self.ok_result(full_name)

    def parse_type(self):
        anchor_start = self.token.start
        name_result = self.parse_name()

        if name_result.is_err:
            return name_result

        defined_type = self.context.namespace.find_obj_type(name_result.ok_value)
        anchor = ErrorSegmentAnchor(anchor_start, self.token.end)

        if defined_type is None:
            return self.build_error(SyntaxError('does not exist'), anchor=anchor)

        if not self.context.namespace.get_type(defined_type.type_name).is_type:
            return self.build_error(TypeError('not a type'), anchor=anchor)

        accessibility = self.context.namespace.check_accessibility(defined_type.type_name)

        if not accessibility.is_accessable:
            return self.build_get_unaccessable_obj_error(accessibility, anchor=anchor)

        return self.ok_result(defined_type)


class ExpressionParser(BasicParser):
    def parse_enumeration(self, segment_parser_fn, breaker: IteratorBreaker = IteratorBreaker(Tokens.NEWL, '\n')):
        expect_comma = False

        while self.token != breaker:
            if self.token.is_sep(','):
                if not expect_comma:
                    return self.build_error()

                expect_comma = False
            elif self.token == breaker:
                self.next()
                break
            else:
                if expect_comma:
                    return self.build_error(
                        SyntaxError('expected `,` here'),
                        lambda err: err.move_cursor_at_end(1)
                    )

                tree_result = segment_parser_fn()
                expect_comma = True

                if tree_result.is_err:
                    return tree_result

            self.next()
        else:
            if breaker.required:
                return self.build_error(
                    SyntaxError(f'expected `{breaker.value}` here'),
                    lambda err: err.move_cursor_at_end(1)
                )

        return self.ok_result('SUCCESSFUL')

    def parse_value(self):
        if not self.token.is_value():
            return self.build_error()

        data = {
            'type': 'num' if self.token.type == Tokens.NUM else 'str',
            'value': self.token.value
        }
        tree = self.new_tree('$value', data=data)

        if self.token.type == Tokens.NUM:
            data['format'] = 'real' if '.' in self.token.value or self.token.value.lower().endswith('f') else 'int'

        return self.ok_result(tree)

    def token_is_expression(self, offset=1):
        return bool(self.peek(lambda token: token.is_value() or token.type == Tokens.ID, offset))

    def parse_value_expression(self):
        tree = self.new_tree('$expression')

        if self.token.type == Tokens.ID:
            start = self.token.start

            name_result = self.context.name_parser.parse_name()

            if name_result.is_err:
                return name_result

            name = ObjectPath.relative(name_result.ok_value)
            accessibility = self.context.namespace.check_accessibility(name)
            err_anchor = ErrorSegmentAnchor(start, self.token.end)

            if not accessibility.is_accessable:
                return self.build_get_unaccessable_obj_error(accessibility, anchor=err_anchor)

            value_obj = self.context.namespace[name]

            if value_obj is None:
                return self.build_error(NameError('does not exist'), anchor=err_anchor)

            if value_obj['type'].is_root:
                return self.build_error(NameError('not a variable'), anchor=err_anchor)

            if value_obj['value'] is None:
                return self.build_error(ValueError('not inited'), anchor=err_anchor)

            return self.ok_result(tree.update({
                'expr': {
                    'type': '$link',
                    'target': self.context.namespace.find_obj_abs_path(name).__str__()
                },
                'value-type': value_obj['type']
            }))

        value = self.parse_value().ok_value.build()
        value_type = self.context.namespace.find_obj_type(
            'str' if value['data']['type'] == 'str' else ('int' if value['data']['format'] == 'int' else 'float')
        )

        if value_type is None:
            raise ValueError('value type is None')

        tree = tree.update({
            'expr': value,
            'value-type': value_type
        })

        return self.ok_result(tree)


class VariableParser(BasicParser):
    def __init__(
            self,
            context: ParserContext,
            let_kw: bool,
            mut_kw: bool,
            value_required: bool,
            supports_typing: bool
    ):
        super().__init__(context)

        self.let_kw, self.mut_kw = let_kw, mut_kw
        self.value_required, self.supports_typing = value_required, supports_typing

    def parse(self, access_modifier: str | None = None, name_checker=None):
        is_var_init = is_arg_init = is_value_set = False

        if self.let_kw:
            op_type = 'init-value'
            is_var_init = True
        elif self.mut_kw:
            op_type = '$arg'
            is_arg_init = True
        else:
            op_type = 'set-value'
            is_value_set = True

        tree = self.new_tree(op_type, {'access-modifier': access_modifier})

        if self.let_kw:
            if not self.token.is_kw('let'):
                return self.build_error()

            if self.next() is None:
                return self.build_error_after_token()

        mutable = None

        if self.mut_kw:
            if self.token.is_kw('mut'):
                if self.next() is None:
                    return self.build_error_after_token()

                mutable = True
            else:
                mutable = False

            tree['mutable'] = mutable

        if self.token.type != Tokens.ID:
            return self.build_error(SyntaxError(f'expected {"argument" if is_arg_init else "variable"} name'))

        value_name = ObjectPath.relative(self.token.value)
        tree['name'] = self.token.value

        if self.let_kw and value_name in self.context.namespace:
            return self.build_error(NameError('already exists'))

        if name_checker is not None:
            checking_result = name_checker(value_name)

            if checking_result.is_err:
                return checking_result

        if is_value_set:
            if value_name not in self.context.namespace:
                return self.build_error(NameError('does not exist'))

            value_object = self.context.namespace[value_name]

            if value_object['type'].is_root:
                return self.build_error(NameError('not an assignable'))

            if not value_object['mutable'] and ('value' not in value_object or value_object['value'] is not None):
                return self.build_error(NameError('is immutable'))

            accessibility = self.context.namespace.check_accessibility(value_name)

            if not accessibility.is_accessable:
                return self.build_get_unaccessable_obj_error(accessibility)

        value_type = None

        if self.supports_typing and self.peek(lambda token: token.is_sep(':')):
            self.next()

            if self.next() is None:
                return self.build_error_after_token()

            type_result = self.context.name_parser.parse_type()

            if type_result.is_err:
                return type_result

            value_type = type_result.ok_value

            tree['value-type'] = value_type

        value_content = None
        value_content_type = None

        if self.peek(lambda token: token.is_op('=')):
            self.next()

            if self.next() is None:
                return self.build_error_after_token()

            content_result = self.context.expression_parser.parse_value_expression()

            if content_result.is_err:
                return content_result

            value_content = content_result.ok_value.build()
            value_content_type = value_content['value-type']

            tree['value'] = value_content
        elif self.value_required:
            return self.build_error_after_token(SyntaxError('value assignment is expected'))

        if self.supports_typing and value_content is None and value_type is None:
            return self.build_error_after_token(TypeError('value type description is expected'))

        _value_type = value_type if not self.value_required else self.context.namespace.get_type(value_name)

        if _value_type is not None and value_content_type is not None and _value_type != value_content_type:
            return self.build_error(TypeError(f'expected type `{_value_type}` but got `{value_content_type}`'))

        if not is_arg_init:
            value_object = ValueObject(
                _value_type if _value_type is not None else value_content_type,
                value_content,
                access_modifier,
                mutable
            )

            self.context.namespace.define(value_name, value_object)

        if 'value-type' not in tree:
            tree['value-type'] = value_content_type

        return self.ok_result(tree)

    @staticmethod
    def var_init(context: ParserContext):
        return VariableParser(context, let_kw=True, mut_kw=True, value_required=False, supports_typing=True)

    @staticmethod
    def set_value(context: ParserContext):
        return VariableParser(context, let_kw=False, mut_kw=False, value_required=True, supports_typing=False)

    @staticmethod
    def arg_init(context: ParserContext):
        return VariableParser(context, let_kw=False, mut_kw=True, value_required=False, supports_typing=True)


class Parser(BasicParser):
    def __init__(self, context: ParserContext):
        super().__init__(context)

    def parse(self):
        tree_result = self.context.body_parser.parse(BodyParsingContext.rootbody())

        if tree_result.is_err:
            return tree_result

        last_parsed_body = tree_result.ok_value.build()
        last_parsed_body['type'] = 'root'

        return self.ok_result(last_parsed_body)


__all__ = (
    'Parser',
    'ParserContext'
)
