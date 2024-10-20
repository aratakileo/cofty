from src.utils.namespace_utils import ObjectPath, TYPE_TYPE, FunctionObject, ValueObject, PreFunctionObject
from src.utils.arataki_typing import Result, TextFile, ImmutableObject
from src.utils.parser_utils import TreeBuilder, ErrorSegmentAnchor, ErrorBuffer
from src.core.tokens import Token, Tokens, IteratorBreaker
from src.utils.exceptions import ReturnStatementError
from src.core.namespace import Namespace
from src.utils import as_serial_number
from src.core.errors import Error

BIG_BODY_BREAKER = IteratorBreaker(Tokens.PARENTHESIS, '}')


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

    def ok_result(self, result):
        return self.context.error_buffer.result_or_err(Result.ok(result))


class BodyParser(BasicParser):
    def __init__(self, context: ParserContext):
        super().__init__(context)

        self.expression_parser, self.name_parser = ExpressionParser(self), NameParser(self)
        self.var_init_parser, self.var_set_parser = VariableParser.var_init(self), VariableParser.set_value(self)
        self.current_function_parser = None

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

        self.context.namespace.start_subbody()
        self.next()

        if self.current_function_parser is not None:
            self.current_function_parser.body_level += 1

        body_result = self.parse('sub-body', BIG_BODY_BREAKER)

        if body_result.is_err:
            return body_result

        self.context.namespace.finish_subbody()

        if self.current_function_parser is not None:
            self.current_function_parser.body_level -= 1

        return body_result

    def parse(self, body_type: str, breaker: IteratorBreaker = None):
        body = []
        tree = self.new_tree(body_type, body=body)

        is_root_body, is_sub_body, is_func_body = body_type == 'root', body_type == 'sub-body', body_type == 'func-body'
        is_in_func_body = self.current_function_parser is not None
        ignore_blocks_except_breaker = False

        while self.has_next():
            block_finished = False

            if is_in_func_body and not ignore_blocks_except_breaker:
                if self.token.is_kw('return'):
                    value_result = self.current_function_parser.parse_return()

                    if value_result.is_err:
                        return value_result

                    body.append(value_result.ok_value.build())
                    ignore_blocks_except_breaker = True

            if (is_root_body or is_sub_body or is_in_func_body) and not ignore_blocks_except_breaker:
                if self.token.is_kw('let'):
                    init_result = self.var_init_parser.parse()

                    if init_result.is_err:
                        return init_result

                    body.append(init_result.ok_value.build())
                    block_finished = True
                elif self.token.type == Tokens.ID and self.peek(lambda token: token.is_op('=')):
                    value_set_result = self.var_set_parser.parse()

                    if value_set_result.is_err:
                        return value_set_result

                    body.append(value_set_result.ok_value.build())
                    block_finished = True
                elif self.token.is_parenthesis('{'):
                    sub_body_result = self.parse_sub_body()

                    if sub_body_result.is_err:
                        return sub_body_result

                    body.append(sub_body_result.ok_value.build())
                    block_finished = True

            if is_root_body and not block_finished and not ignore_blocks_except_breaker:
                if self.token.is_kw('fn'):
                    self.current_function_parser = FunctionParser(self)
                    init_func_result = self.current_function_parser.parse()

                    if init_func_result.is_err:
                        return init_func_result

                    body.append(init_func_result.ok_value.build())
                elif self.token.is_kw('typedef'):
                    if not self.peek(lambda token: token.type == Tokens.ID):
                        return self.build_error()

                    self.next()

                    if self.context.namespace.define_obj(self.token.value, TYPE_TYPE) is None:
                        return self.build_error(NameError(f'already exists'))

            if breaker is not None and breaker == self.token:
                self.next()

                if is_func_body:
                    self.current_function_parser = None
                    self.context.namespace.go_up()

                return self.ok_result(tree)

            if not self.finish_block():
                return self.build_error()

        if breaker is not None and breaker.required and self.token != breaker:
            return self.build_error(SyntaxError(f'expected `{breaker.value}` here'))

        return self.ok_result(tree)


class NameParser(BasicParser):
    def __init__(self, body_parser: BodyParser):
        super().__init__(body_parser.context)
        self.body_parser = body_parser

    def parse_name(self):
        if self.token.type != Tokens.ID:
            return self.build_error()

        full_name = (self.token.value,)

        if not self.has_next():
            return self.ok_result(full_name)

        while self.peek(lambda token: token.is_sep('.')):
            self.next()

            if not self.peek():
                return self.build_error()

            self.next()

            if self.token.type != Tokens.ID:
                return self.build_error()

            full_name = (*full_name, self.token.value)

        return self.ok_result(full_name)

    def parse_type(self):
        anchor_start = self.token.start
        name_result = self.parse_name()

        if name_result.is_err:
            return name_result

        defined_type = self.context.namespace.find_obj_type(name_result.ok_value)

        if defined_type is None or self.context.namespace.get_type(defined_type.type_name) != TYPE_TYPE:
            return self.build_error(
                SyntaxError('does not exist') if defined_type is None else TypeError('not a type'),
                anchor=ErrorSegmentAnchor(anchor_start, self.token.end)
            )

        return self.ok_result(defined_type)


class ExpressionParser(BasicParser):
    def __init__(self, body_parser: BodyParser):
        super().__init__(body_parser.context)
        self.body_parser = body_parser

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

            name_result = self.body_parser.name_parser.parse_name()

            if name_result.is_err:
                return name_result

            name = ObjectPath.relative(name_result.ok_value)

            value_obj = self.context.namespace[name]
            err_anchor = ErrorSegmentAnchor(start, self.token.end)

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
        tree = tree.update({
            'expr': value,
            'value-type': self.context.namespace.find_obj_type(
                'str' if value['data']['type'] == 'str' else ('int' if value['data']['format'] == 'int' else 'float')
            )
        })

        return self.ok_result(tree)


class VariableParser(BasicParser):
    def __init__(
            self,
            body_parser: BodyParser,
            let_kw: bool,
            mut_kw: bool,
            value_required: bool,
            supports_typing: bool
    ):
        super().__init__(body_parser.context)

        self.body_parser = body_parser

        self.let_kw, self.mut_kw = let_kw, mut_kw
        self.value_required, self.supports_typing = value_required, supports_typing

    def parse(self, name_checker=None):
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

        tree = self.new_tree(op_type)

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

        value_type = None

        if self.supports_typing and self.peek(lambda token: token.is_sep(':')):
            self.next()

            if self.next() is None:
                return self.build_error_after_token()

            type_result = self.body_parser.name_parser.parse_type()

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

            content_result = self.body_parser.expression_parser.parse_value_expression()

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
                mutable
            )

            self.context.namespace.define(value_name, value_object)

        if 'value-type' not in tree:
            tree['value-type'] = value_content_type

        return self.ok_result(tree)

    @staticmethod
    def var_init(body_parser: BodyParser):
        return VariableParser(body_parser, let_kw=True, mut_kw=True, value_required=False, supports_typing=True)

    @staticmethod
    def set_value(body_parser: BodyParser):
        return VariableParser(body_parser, let_kw=False, mut_kw=False, value_required=True, supports_typing=False)

    @staticmethod
    def arg_init(body_parser: BodyParser):
        return VariableParser(body_parser, let_kw=False, mut_kw=True, value_required=False, supports_typing=True)


class FunctionParser(BasicParser):
    args: dict[str, dict[str, any]]

    def __init__(self, body_parser: BodyParser):
        super().__init__(
            body_parser.context,
            'returnable_type',
            'body_level',
            'has_completed_returnable_statement'
        )
        self.args = dict()
        self.arg_parser = VariableParser.arg_init(body_parser)
        self.body_parser = body_parser
        self.returnable_type = None
        self.body_level = 0
        self.has_completed_returnable_statement = False

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

        if self.body_parser.expression_parser.token_is_expression():
            self.next()

            expression_result = self.body_parser.expression_parser.parse_value_expression()

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

        if self.body_level == 0:
            self.has_completed_returnable_statement = True

        return self.ok_result(tree)

    def parse_arg(self):
        arg_result = self.arg_parser.parse(self.__name_check)

        if arg_result.is_err:
            return arg_result

        arg_tree = arg_result.ok_value.build()
        arg_tree['type'] = arg_tree['value-type']

        arg_name = arg_tree['name']

        del arg_tree['start'], arg_tree['end'], arg_tree['name'], arg_tree['value-type']

        self.args[arg_name] = arg_tree

        return self.ok_result('SUCCESSFUL')

    def parse(self):
        if not self.token.is_kw('fn'):
            return self.build_error()

        func_name_exception = SyntaxError('expected function name')

        if self.next() is None:
            return self.build_error_after_token(func_name_exception)

        if self.token.type != Tokens.ID:
            return self.build_error(func_name_exception)

        func_name = ObjectPath.relative(self.token.value)
        tree = self.new_tree('func-init', name=self.token.value)

        open_parenthesis_exception = SyntaxError('expected `(`')

        if self.next() is None:
            return self.build_error_after_token(open_parenthesis_exception)

        if not self.token.is_parenthesis('('):
            return self.build_error(open_parenthesis_exception)

        if self.next() is None:
            return self.build_error_after_token(SyntaxError('expected arguments description or `)`'))

        arg_trees_result = self.body_parser.expression_parser.parse_enumeration(
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

            type_result = self.body_parser.name_parser.parse_type()

            if type_result.is_err:
                return type_result

            self.returnable_type = type_result.ok_value

            if self.next() is None:
                return self.build_error_after_token()

        if not self.token.is_parenthesis('{'):
            return self.build_error(SyntaxError('expected function body'))

        if self.next() is None:
            return self.build_error_after_token()

        pre_function = PreFunctionObject(self.args)
        self.context.namespace.define(func_name, pre_function)
        self.context.namespace.goto(func_name)

        body_result = self.body_parser.parse('func-body', BIG_BODY_BREAKER)

        if body_result.is_err:
            return body_result

        if self.returnable_type is None or not self.has_completed_returnable_statement:
            return self.build_error(ReturnStatementError('expected return statement'), anchor=self.peek(step=-1))

        self.context.namespace[func_name] = FunctionObject(self.args, self.returnable_type, pre_function.body)

        tree.update({
            'args': self.args,
            'returnable-type': self.returnable_type,
            'body': body_result.ok_value['body']
        })

        return self.ok_result(tree)


class Parser(BasicParser):
    def __init__(self, context: ParserContext):
        super().__init__(context)
        self.body_parser = BodyParser(context)

    def parse(self):
        tree_result = self.body_parser.parse('root')

        if tree_result.is_err:
            return tree_result

        last_parsed_body = tree_result.ok_value.build()
        last_parsed_body['type'] = 'root'

        return self.ok_result(last_parsed_body)


__all__ = (
    'Parser',
)
