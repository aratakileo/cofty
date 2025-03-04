from src.utils.arataki_typing import ImmutableObject, JsonSerializable, ImmutableDict


class ObjectPath(ImmutableObject):
    segments: tuple

    def __init__(self, segments: list | tuple | str):
        super().__init__()
        if isinstance(segments, str):
            self.segments = (*segments.split('.'),)
        elif isinstance(segments, tuple):
            self.segments = segments
        else:
            self.segments = (*segments,)

        if not self.segments:
            raise ValueError('path can not be empty')

    def __eq__(self, other):
        if not isinstance(other, ObjectPath):
            raise ValueError('expected ObjectPath value')

        return self.segments == other.segments

    def __contains__(self, item):
        return item in self.segments

    def __getitem__(self, item):
        if isinstance(item, slice):
            return ObjectPath(self.segments[item])

        return self.segments[item]

    def __iter__(self):
        return self.segments.__iter__()

    def __len__(self):
        return self.segments.__len__()

    def __add__(self, other):
        if isinstance(other, str):
            other = other.split('.')

        if '$' in other:
            raise ValueError(f'impossible to concatenate `{self.__str__()}` with `{".".join(other)}`')

        if isinstance(other, ObjectPath):
            return ObjectPath(self.segments + other.segments)

        return ObjectPath(self.segments + (*other,))

    def hierarchy_from_top(self) -> tuple['ObjectPath']:
        path = self
        return (self, *(path := path.go_up() for i in range(len(self.segments) - 1)))[::-1]

    @property
    def is_abs(self):
        return self.segments[0] == '$'

    @property
    def is_root(self):
        return self.segments == ('$',)

    @property
    def first_segment(self):
        return self.segments[0]

    @property
    def last_segment(self):
        return self.segments[-1]

    def as_abs(self):
        return ObjectPath.abs(self.segments)

    def as_relative(self, strict=True):
        if not self.is_abs and strict:
            raise ValueError(f'path `{self.__str__()}` is not absolute')

        return ObjectPath(self.segments[1:])

    def go_up(self):
        if len(self.segments) == 1:
            raise ValueError(f'path `{self.__str__()}` are already at the very top')

        return ObjectPath(self.segments[:-1])

    def startswith(self, path):
        if isinstance(path, str):
            path = path.split('.')

        if len(self.segments) < len(path):
            return False

        for i in range(len(path)):
            if path[i] != self.segments[i]:
                return False

        return True

    @staticmethod
    def abs(segments: list | tuple | str):
        if isinstance(segments, str):
            segments = segments.split('.')

        if segments[0] == '$':
            return ObjectPath(segments)

        return ObjectPath(('$', *segments))

    @staticmethod
    def relative(segments: list | tuple | str):
        if isinstance(segments, str):
            segments = segments.split('.')

        if segments[0] == '$':
            raise ValueError(f'expected relative path, but got absolute path `{".".join(segments)}`')

        if '$' in segments:
            raise ValueError(f'invalid path `{".".join(segments)}`')

        return ObjectPath(segments)

    @staticmethod
    def root_path():
        return ObjectPath('$')

    @staticmethod
    def builtins_path():
        return ObjectPath('$.$builtins')

    def __str__(self):
        return '.'.join(self.segments)

    def __repr__(self):
        return f'{ObjectPath.__name__}({self.__str__().__repr__()})'


class ObjectType(ImmutableObject, JsonSerializable):
    type_name: ObjectPath | str

    def __init__(self, type_name: ObjectPath | str):
        super().__init__()
        self.type_name = type_name

        if isinstance(type_name, ObjectPath) and not type_name.is_abs:
            raise ValueError('object path of object type should be absolute')

    def __eq__(self, other):
        self_is_stringified = isinstance(self.type_name, str)

        if isinstance(other, str):
            return self_is_stringified and self.type_name == other

        return self_is_stringified == isinstance(other.type_name, str) and self.type_name == other.type_name

    @property
    def is_root(self):
        return isinstance(self.type_name, str) and self.type_name.startswith('$')

    @property
    def is_namespace(self):
        return self.is_root and self.type_name in ('$body', '$root', '$module', '$class')

    @property
    def is_func(self):
        return self.is_root and self.type_name in ('$func', '$pre-func')

    @property
    def is_type(self):
        return self.is_root and self.type_name in ('$class', '$type')

    def to_json_serializable(self):
        return self.__str__()

    @staticmethod
    def from_json_serializable(obj: str):
        if obj != '$' and not obj.startswith('$.'):
            return ObjectType(obj)

        return ObjectType(ObjectPath.abs(obj))

    def __str__(self):
        return self.type_name.__str__()

    def __repr__(self):
        return f'{ObjectType.__name__}({self.type_name.__repr__()})'


ROOT_TYPE, BODY_TYPE, MODULE_TYPE = ObjectType('$root'), ObjectType('$body'), ObjectType('$module')
TYPE_TYPE, NOT_INITED_MODULE_TYPE = ObjectType('$type'), ObjectType('$not-inited-module')
FUNC_TYPE, PRE_FUNC_TYPE = ObjectType('$func'), ObjectType('$pre-func')
CLASS_TYPE = ObjectType('$class')


class NamespaceObject(JsonSerializable, ImmutableObject):
    def __init__(self, type: ObjectType, *allow_mutation: str):
        super().__init__(*allow_mutation)
        self.type = type

    def __getitem__(self, item):
        return self.__getattribute__(item)

    def __contains__(self, item):
        return item in self.__immutable_dict__

    def to_json_serializable(self):
        return self.__immutable_dict__


class ValueObject(NamespaceObject):
    def __init__(
            self,
            type: ObjectType | None,
            value: dict[str, any],
            access_modifier: str | None,
            mutable: bool = None
    ):
        if type is None:
            if value is None:
                raise ValueError('both type and value are None')

            type = value['value-type']

        if mutable is not None:
            super().__init__(type, 'value')

            self.mutable = mutable
        else:
            super().__init__(type)

            self.mutable = False

        self.value = value
        self.access_modifier = access_modifier


class BodyHaverObject(NamespaceObject):
    def __init__(self, type: ObjectType, body: dict = ...):
        super().__init__(type)

        self.body = body if body is not ... else dict()


class PreFunctionObject(BodyHaverObject):
    def __init__(self, args: ImmutableDict | dict[str, dict[str, str]], access_modifier: str | None):
        super().__init__(PRE_FUNC_TYPE)

        if isinstance(args, ImmutableDict):
            self.args = args
        else:
            self.args = ImmutableDict(args)

        self.access_modifier = access_modifier


class FunctionObject(BodyHaverObject):
    def __init__(
            self,
            args: ImmutableDict | dict[str, dict[str, str]],
            access_modifier: str | None,
            returnable_type: ObjectType,
            body: dict[str, any]
    ):
        super().__init__(FUNC_TYPE, body)

        if isinstance(args, ImmutableDict):
            self.args = args
        else:
            self.args = ImmutableDict(args)

        self.returnable_type = returnable_type
        self.access_modifier = access_modifier


class ClassObject(BodyHaverObject):
    def __init__(self, access_modifier: str):
        super().__init__(CLASS_TYPE)
        self.access_modifier = access_modifier


class NotInitedModule(NamespaceObject):
    def __init__(self, initializer):
        super().__init__(NOT_INITED_MODULE_TYPE)
        self.init = initializer

    def to_json_serializable(self):
        return {'type': self.type}


class Accessibility(ImmutableObject):
    def __init__(self, is_accessable: bool, access_modifier: str | None, unaccessable_path: ObjectPath | None = None):
        super().__init__()
        self.is_accessable = is_accessable
        self.access_modifier = access_modifier
        self.unaccessable_path = unaccessable_path

    @staticmethod
    def accessable(access_modifier: str | None):
        return Accessibility(True, access_modifier)

    @staticmethod
    def unaccessable(access_modifier: str, unaccessable_path: ObjectPath):
        return Accessibility(False, access_modifier, unaccessable_path)


__all__ = (
    'ObjectPath',
    'ObjectType',
    'NamespaceObject',
    'ValueObject',
    'PreFunctionObject',
    'FunctionObject',
    'ClassObject',
    'NotInitedModule',
    'Accessibility',
    'ROOT_TYPE',
    'BODY_TYPE',
    'MODULE_TYPE',
    'TYPE_TYPE',
    'FUNC_TYPE',
    'PRE_FUNC_TYPE',
    'NOT_INITED_MODULE_TYPE'
)
