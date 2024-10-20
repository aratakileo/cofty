from src.utils.arataki_typing import TextFile
from src.utils import unequal_or_else
from src.core.tokens import Token


class Error:
    file: TextFile
    err: Exception
    uncertain_segment_start: int
    uncertain_segment_end: int
    cursor_start: int = 0
    cursor_end: int = -1
    segment_line: int

    def __init__(self, err: Exception, token: Token, file: TextFile):
        self.err = err
        self.uncertain_segment_start, self.uncertain_segment_end = token.start, token.end
        self.segment_line = file.text[:self.uncertain_segment_end].count('\n') + 1
        self.file = file

    @property
    def dirty_segment_start(self):
        return max(0, self.file.text.rfind('\n', 0, self.uncertain_segment_start))

    @property
    def segment_end(self):
        return unequal_or_else(self.file.text.find('\n', self.uncertain_segment_end), -1, len(self.file.text))

    @property
    def segment(self):
        return self.file.text[self.dirty_segment_start: self.segment_end]

    @property
    def segment_start(self):
        return self.dirty_segment_start + (len(self.segment) - len(self.segment.lstrip()))

    @property
    def segment_length(self):
        return self.segment_end - self.segment_start

    @property
    def cursor_length(self):
        return self.cursor_end - self.cursor_start

    @property
    def uncertain_segment_length(self):
        return self.uncertain_segment_end - self.uncertain_segment_start

    def set_cursor_position(self, start: int, length: int = 1):
        self.cursor_start = start
        self.cursor_end = self.cursor_start + length

        return self

    def move_cursor_at_end(self, offset: int = 0, length: int = 1):
        self.cursor_end = self.uncertain_segment_start - self.segment_start + self.uncertain_segment_length + offset
        self.cursor_start = self.cursor_end - length

        return self

    def underscore_segment_by_cursor(self, start_offset: int = 0, end_offset: int = 0):
        self.cursor_start = self.uncertain_segment_start - self.segment_start + start_offset
        self.cursor_end = self.cursor_start + self.uncertain_segment_length - start_offset + end_offset

        return self

    def __str__(self):
        return f'   File "{self.file.abspath}", line {self.segment_line}\n' \
               f'      {self.segment.lstrip()}\n' \
               f'      {" " * self.cursor_start}{"^" * self.cursor_length}\n' \
               f'{self.err.__class__.__name__}: {self.err}'


class ErrorHandler:
    errors: list[Error] = []

    @property
    def has_errors(self):
        return len(self.errors) > 0

    def describe_error(self, file: TextFile, message: str, token: Token):
        err = Error(message, token, file)

        self.errors.append(err)

        return err

    def trace(self):
        output = ''

        for i, err in enumerate(self.errors):
            output += err.__str__()

            if i != len(self.errors) - 1:
                output += '\n'

        return output

    def print(self):
        print(self.trace())
