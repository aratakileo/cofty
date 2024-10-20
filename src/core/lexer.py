from src.core.tokens import TOKEN_REGEX, Tokens, Token, get_opening_parenthesis
from src.utils.arataki_typing import TextFile, Result
from src.core.errors import Error
from re import finditer


def parse_tokens(file: TextFile):
    tokens = []
    parenthesis_stack = []
    last_open_parenthesis_token = None

    for segment in finditer(TOKEN_REGEX, file.text):
        type = segment.lastgroup

        if type in (Tokens.IGNORE.name, Tokens.COMMENT.name):
            continue

        value = segment.group()
        token = Token(Tokens[type], value, segment.start(), segment.end())

        if type == Tokens.INVALID.name:
            return Result.err(Error(SyntaxError('invalid syntax'), token, file).underscore_segment_by_cursor())

        if type == Tokens.PARENTHESIS.name:
            if value in '[{(':
                last_open_parenthesis_token = token
                parenthesis_stack.append(value)
            elif value in ']})':
                if parenthesis_stack and parenthesis_stack[-1] == get_opening_parenthesis(value):
                    del parenthesis_stack[-1]
                else:
                    return Result.err(Error(
                        SyntaxError('invalid close pair'),
                        token,
                        file
                    ).underscore_segment_by_cursor())

        tokens.append(token)

    if parenthesis_stack:
        return Result.err(Error(
            SyntaxError('not a closed pair'),
            last_open_parenthesis_token,
            file
        ).underscore_segment_by_cursor())

    return Result.ok(tokens)
