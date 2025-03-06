from src.utils.arataki_typing import ImmutableObject
from os.path import dirname
from re import compile
from enum import Enum


class Tokens(Enum):
    INVALID = 0
    IGNORE = 1
    KEYWORD = 2
    COMMENT = 3
    NUM = 4
    STR = 5
    ID = 6
    NEWL = 7
    SEP = 8
    OP = 9
    PARENTHESIS = 10


with open(dirname(__file__) + '/keywords.txt', 'r', encoding='utf-8') as f:
    TOKEN_DESCRIPTIONS = {
        Tokens.KEYWORD: '|'.join(f.read().splitlines()),
        Tokens.COMMENT: r'--.*(:?\n|\Z)',
        Tokens.NUM: r'_*\d+[\d_]*(?:\.[\d_]*|[fF])?',
        Tokens.ID: r'(?!_*\d+)[A-Za-z\d_]+',
        Tokens.STR: r'\'(?:\\.|[^\'])*\'|"(?:\\.|[^"])*"',
        Tokens.NEWL: r'\n',
        Tokens.IGNORE: r'\s+',
        Tokens.SEP: r'[,:\.]',
        Tokens.PARENTHESIS: r'[(){}\[\]]',
        Tokens.OP: r'->|(?:\+|-|/|\*\*?|>|<|=)=?',
        Tokens.INVALID: r'.+'
    }

TOKEN_REGEX = compile('|'.join(f'(?P<{key.name}>{value})' for key, value in TOKEN_DESCRIPTIONS.items()))


class Token(ImmutableObject):
    type: Tokens
    value: str
    start: int
    end: int

    def __init__(self, type: Tokens, value: str, start: int, end: int):
        super().__init__()

        self.type, self.value, self.start, self.end = type, value, start, end

    def __eq__(self, other):
        basic_check = other.type == self.type and other.value == self.value

        if isinstance(other, Token):
            return basic_check and self.start == other.start and self.end == other.end

        return other.type == self.type and other.value == self.value

    def is_kw(self, kw: str):
        return self.type == Tokens.KEYWORD and self.value == kw

    def is_op(self, op: str):
        return self.type == Tokens.OP and self.value == op

    def is_sep(self, sep: str):
        return self.type == Tokens.SEP and self.value == sep

    def is_parenthesis(self, parenthesis: str):
        return self.type == Tokens.PARENTHESIS and self.value == parenthesis

    def is_value(self):
        return self.type in (Tokens.STR, Tokens.NUM)

    def is_access_modifier(self):
        return self.type == Tokens.KEYWORD and self.value in ('public', 'private', 'protected')

    def __str__(self):
        return f'{Token.__name__}.{self.type.name}({self.value.__repr__()}, start={self.start}, end={self.end})'

    def __repr__(self):
        return f'{Token.__name__}(' \
               f'{Tokens.__name__}[\'{self.type.name}\'], {self.value.__repr__()}, start={self.start}, end={self.end}' \
               f')'


class IteratorBreaker(ImmutableObject):
    type: Tokens
    required: bool

    def __init__(self, type: Tokens, value, required=False):
        super().__init__()

        self.type, self.value, self.required = type, value, required

    def __eq__(self, other):
        basic_check = other.type == self.type and other.value == self.value

        if isinstance(other, IteratorBreaker):
            return basic_check and self.required == other.required

        return basic_check

    @staticmethod
    def required(type: Tokens, value: str):
        return IteratorBreaker(type, value, True)

    def __str__(self):
        return f'{IteratorBreaker.__name__}.{self.type.name}({self.value.__repr__()}, required={self.required})'


PARENTHESIS = {
    '(': ')',
    '[': ']',
    '{': '}'
}


def get_closing_parenthesis(opening_parenthesis: str):
    return PARENTHESIS[opening_parenthesis]


def get_opening_parenthesis(closing_parenthesis: str):
    for key, value in PARENTHESIS.items():
        if value == closing_parenthesis:
            return key

    raise KeyError(f'invalid closing parenthesis: `{closing_parenthesis}`')
