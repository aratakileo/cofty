from os.path import abspath, normpath
from json import dumps


class ImmutableObject:
    __inited_values = tuple()
    __allow_mutation = tuple()

    def __init__(self, *allow_mutation: str):
        self.__allow_mutation = allow_mutation

    def __setattr__(self, key, value):
        if key in self.__inited_values and key not in self.__allow_mutation:
            raise NameError(f'`{self.__class__.__name__}` is immutable')

        super().__setattr__('__inited_values', (*self.__inited_values, key))
        super().__setattr__(key, value)

    @property
    def __immutable_dict__(self):
        _dict = self.__dict__.copy()
        del _dict['__inited_values'], _dict[f'_{ImmutableObject.__name__}__allow_mutation']

        return ImmutableDict(_dict)


class StaticTypedObject:
    __value_types = dict()

    def __setattr__(self, key, value):
        if key not in self.__value_types:
            super().__setattr__(key, value)
            self.__value_types[key] = type(value)
            return

        if type(value) != self.__value_types[key] and not issubclass(type(value), self.__value_types[key]):
            raise TypeError(f'expected type `{self.__value_types[key].__name__}` but got `{type(value).__name__}`')

        super().__setattr__(key, value)

    def __mark_as_inited_attributes__(self, *keys):
        for key in keys: self.__setattr__(key, self.__getattribute__(key))

    def __mark_as_not_inited_attributes__(self, *keys):
        for key in keys:
            if key in self.__value_types:
                del self.__value_types[key]


class Result(ImmutableObject):
    def __init__(self, ok_value, err_value):
        super().__init__()
        self.ok_value, self.err_value = ok_value, err_value

        if ok_value is None and err_value is None:
            raise ValueError('both `ok_value` and `err_value` are None')

    @property
    def is_err(self):
        return self.err_value is not None

    @property
    def is_ok(self):
        return self.ok_value is not None

    def map(self, map_func):
        return map_func(self.ok_value)

    def map_err(self, map_func):
        return map_func(self.err_value)

    def flat_map(self, map_func):
        if self.is_ok:
            return Result.ok(map_func(self.ok_value))

        return self

    def flat_map_err(self, map_func):
        if self.is_err:
            return Result.err(map_func(self.err_value))

        return self

    def unwrap(self):
        return self.ok_value if self.is_ok else self.err_value

    @staticmethod
    def ok(result):
        return Result(result, None)

    @staticmethod
    def err(error):
        return Result(None, error)


class JsonSerializable:
    def to_json_serializable(self):
        raise RuntimeError('not inited')

    @staticmethod
    def from_json_serializable(obj):
        raise RuntimeError('not inited')


class TextFile(ImmutableObject, JsonSerializable):
    path: str
    text: str

    def __init__(self, path: str, text: str):
        super().__init__()
        self.path = normpath(path)
        self.abspath = abspath(self.path)
        self.text = text

    def to_json_serializable(self):
        return {
            'path': self.path.replace('\\', '/'),
            'text': self.text
        }

    @staticmethod
    def from_json_serializable(obj: dict[str, str]):
        return TextFile(obj['path'], obj['text'])

    @staticmethod
    def read(path: str):
        with open(path, 'r', encoding='utf-8') as f:
            return TextFile(path, f.read())

    def __eq__(self, other: 'TextFile'):
        return self.abspath == other.abspath

    def __str__(self):
        return self.text

    def __repr__(self):
        return f'TextFile.read({self.path.__repr__()})'


def json_encoder(obj):
    if isinstance(obj, JsonSerializable):
        return obj.to_json_serializable()

    if isinstance(obj, int | float | bool | str | list | tuple):
        return obj

    raise ValueError(f'{obj.__class__.__name__} is not JSON serializable')


def json_decoder(obj, cls):
    if issubclass(cls, JsonSerializable):
        return cls.from_json_serializable(obj)

    return obj


def json_encode(obj):
    return dumps(obj, indent=3, default=json_encoder)


class ImmutableDict(ImmutableObject, JsonSerializable):
    __mutable: dict

    def __init__(self, source):
        super().__init__()

        if isinstance(source, ImmutableDict):
            self.__mutable = source.__mutable
        else:
            self.__mutable = dict(source)

    def __len__(self):
        return self.__mutable.__len__()

    def __getitem__(self, item):
        return self.__mutable.__getitem__(item)

    def __contains__(self, item):
        return self.__mutable.__contains__(item)

    def __eq__(self, other):
        if isinstance(other, ImmutableDict):
            return self.__mutable == other.__mutable

        return self.__mutable == other

    def __or__(self, other):
        if isinstance(other, ImmutableDict):
            return ImmutableDict(self.__mutable | other.__mutable)

        return ImmutableDict(self.__mutable | other)

    def __ror__(self, other):
        if isinstance(other, ImmutableDict):
            return ImmutableDict(other.__mutable | self.__mutable)

        return other | self.__mutable

    def __reversed__(self):
        return ImmutableDict(self.__mutable.__reversed__())

    def __iter__(self):
        return self.__iter__()

    def items(self):
        return self.__mutable.items()

    def values(self):
        return self.__mutable.values()

    def keys(self):
        return self.__mutable.keys()

    def as_mutable(self):
        return self.__mutable.copy()

    def to_json_serializable(self):
        return self.as_mutable()

    @staticmethod
    def empty():
        return EMPTY_IMMUTABLE_DICT

    @staticmethod
    def of(source):
        if isinstance(source, ImmutableDict):
            return source

        mutable_dict = dict(source)

        return EMPTY_IMMUTABLE_DICT if not mutable_dict else ImmutableDict(mutable_dict)

    def __bool__(self):
        return bool(self.__mutable)

    def __str__(self):
        return self.__mutable.__str__()

    def __repr__(self):
        return f'{ImmutableDict.__name__}({self.__mutable.__repr__()})'


EMPTY_IMMUTABLE_DICT = ImmutableDict(dict())
