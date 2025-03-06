from src.utils.arataki_typing import ImmutableObject, json_encode, Result
from src.core.tokens import IteratorBreaker, Tokens
from src.core.errors import Error


class ErrorSegmentAnchor(ImmutableObject):
    start: int
    end: int

    def __init__(self, start: int, end: int):
        super().__init__()

        self.start, self.end = start, end


class TreeBuilder(ImmutableObject):
    tree: dict[str, any]

    def __init__(self, type: str, parser):
        super().__init__()

        self.parser = parser

        self.tree = {
            'type': type,
            'start': parser.token.start
        }

    def __contains__(self, item):
        return item in self.tree

    def __setitem__(self, key: str, value):
        self.tree[key] = value

    def __getitem__(self, item: str):
        return self.tree[item]

    @property
    def start(self):
        return self.tree['start']

    @property
    def end(self):
        return self.parser.token.end if 'end' not in self.tree else self.tree['end']

    @end.setter
    def end(self, end: int):
        self.tree['end'] = end

    def build(self):
        self.end = self.parser.token.end
        return self.tree

    def update(self, _dict: dict[str, any]):
        self.tree.update(_dict)
        return self

    def char_range(self):
        return self.end - self.start

    def as_err_anchor(self):
        return ErrorSegmentAnchor(self.start, self.end)

    @staticmethod
    def of(_dict: dict, parser):
        tree_builder = TreeBuilder(_dict['type'], parser)
        tree_builder.update(_dict)

        return tree_builder

    def __str__(self):
        return json_encode(self.build())


class ErrorBuffer:
    error: Error | None

    def __init__(self):
        self.error = None

    @property
    def has_err(self):
        return self.error is not None

    def ok_or_err_result(self, ok_value):
        if self.has_err:
            return Result.err(self.error)

        return Result.ok(ok_value)


BIG_BODY_BREAKER = IteratorBreaker(Tokens.PARENTHESIS, '}')


class BodyParsingContext:
    def __init__(self, body_type: str, breaker: IteratorBreaker = None):
        self.body_type = body_type
        self.breaker = breaker
        self._access_modifier = 'public'
        self._access_modifier_has_changed = False

    @property
    def is_root_body(self):
        return self.body_type == 'root'

    @property
    def is_subbody(self):
        return self.body_type == 'sub-body'

    @property
    def is_func_body(self):
        return self.body_type == 'func-body'

    @property
    def is_class_body(self):
        return self.body_type == 'class-body'

    @property
    def is_subbody_like(self):
        return self.is_subbody or self.is_root_body

    def set_default_access_modifier(self):
        if not self.access_modifier_is_allowed:
            self._access_modifier = None
            return

        self._access_modifier = 'public'
        self._access_modifier_has_changed = False

        if self.is_class_body:
            self._access_modifier = 'private'

    @property
    def access_modifier_is_allowed(self):
        return self.is_class_body or self.is_root_body

    @property
    def access_modifier_has_changed(self):
        return self._access_modifier_has_changed

    @property
    def access_modifier(self) -> str | None:
        return self._access_modifier

    @access_modifier.setter
    def access_modifier(self, value: str):
        if not self.access_modifier_is_allowed:
            raise ValueError('Access modifier is not allowed in this context')

        if self._access_modifier_has_changed:
            raise ValueError('Access modifier has changed for the body without reset')

        if value is None:
            raise ValueError('Access modifier is None')

        self._access_modifier_has_changed = True
        self._access_modifier = value

    @staticmethod
    def subbody():
        return BodyParsingContext('sub-body', BIG_BODY_BREAKER)

    @staticmethod
    def funcbody():
        return BodyParsingContext('func-body', BIG_BODY_BREAKER)

    @staticmethod
    def classbody():
        return BodyParsingContext('class-body', BIG_BODY_BREAKER)

    @staticmethod
    def rootbody():
        return BodyParsingContext('root')
