from src.utils.arataki_typing import ImmutableObject, json_encode, Result
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

    def result_or_err(self, result: Result):
        if self.has_err:
            return Result.err(self.error)

        return result
