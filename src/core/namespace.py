from src.utils.arataki_typing import TextFile, StaticTypedObject, json_encode
from src.utils.namespace_utils import *


class Namespace(StaticTypedObject):
    current_obj_path: ObjectPath = ObjectPath.root_path()
    root_object: dict[str, str | ObjectType | dict[str, any]]
    current_dict: dict[str, str | ObjectType | dict[str, any]]

    def __init__(self):
        self.root_object = {
            'type': ROOT_TYPE,
            'body': {}
        }
        self.current_dict = self.root_object['body']
        self.__none_type = None

        self.__mark_as_inited_attributes__('current_obj_path')
        self.__mark_as_not_inited_attributes__('_Namespace__none_type')

    def __contains__(self, obj_path: ObjectPath):
        return self[obj_path] is not None

    def __setitem__(self, obj_path: ObjectPath, value: NamespaceObject | dict[str, any]):
        _current_path = self.obj_context_abs_path(obj_path).go_up()
        _current_obj = current_obj = self[_current_path]

        while _current_obj['type'] == '$body' and len(_current_path) > 2:
            _current_path = _current_path.go_up()
            _current_obj = self[_current_path]
        else:
            if _current_obj['type'].is_func:
                current_obj = _current_obj

        current_obj['body'][obj_path.last_segment] = value

    def __getitem__(self, obj_path: ObjectPath):
        if obj_path.is_root:
            return self.root_object

        obj_abs_path = self.obj_context_abs_path(obj_path)
        obj_relative_path = obj_abs_path.as_relative(False)
        builtins_obj_path = ObjectPath.builtins_path()

        current_body = self.root_object['body']
        current_obj = self.root_object
        current_abs_path = ObjectPath.root_path()

        parent_func_obj = None

        for i, segment in enumerate(obj_relative_path):
            if segment not in current_body:
                if parent_func_obj is not None \
                        and segment in parent_func_obj.args:
                    current_obj = parent_func_obj.args[segment]
                elif obj_path.is_abs:
                    return None
                elif '$builtins' not in obj_path:
                    result = self[builtins_obj_path + obj_path]

                    if result is None: break

                    return result
                else:
                    break
            else:
                current_obj = current_body[segment]

            if i == len(obj_relative_path) - 1: return current_obj

            current_obj_type = current_obj['type']

            if current_obj_type == NOT_INITED_MODULE_TYPE:
                current_obj.init()
                current_obj = current_body[segment]
                current_obj_type = current_obj['type']

            if current_obj_type.is_func:
                parent_func_obj = current_obj

            current_abs_path = current_abs_path + segment

            if not self.can_come_in(current_obj, current_abs_path):
                return None

            current_body = current_obj['body']
        else:
            return current_body

        if self.current_obj_path.is_root: return None

        for i in range(len(self.current_obj_path)):
            result = self[self.current_obj_path[:i + 1] + obj_path]

            if result is not None:
                return result

        return None

    def check_accessibility(self, obj_path: ObjectPath):
        obj = self[obj_path]

        if obj is None:
            return None

        for path in self.obj_context_abs_path(obj_path).hierarchy_from_top():
            current_obj = self[path]

            if 'access_modifier' in current_obj \
                    and current_obj['access_modifier'] == 'private' \
                    and path.go_up() != self.current_obj_path:
                return Accessibility.unaccessable('private', path)

        return Accessibility.accessable(None if 'access_modifier' not in obj else obj['access_modifier'])

    @property
    def none_type(self):
        if self.__none_type is None:
            self.__none_type = ObjectType(ObjectPath.builtins_path() + 'none')

            if self.__none_type.type_name not in self:
                raise RuntimeError('none type does not exist')

        return self.__none_type

    def can_come_in(self, obj: NamespaceObject | dict[str, any], obj_abs_path: ObjectPath):
        obj_type = obj['type']

        if obj_type.is_namespace:
            return True

        return obj_type.is_func and self.current_obj_path.startswith(obj_abs_path)

    def obj_context_abs_path(self, obj_path: ObjectPath):
        return obj_path if obj_path.is_abs else (self.current_obj_path + obj_path)

    def find_obj_abs_path(self, obj_relative_path: ObjectPath):
        if obj_relative_path.is_abs:
            raise ValueError(f'expected relative path, but got absolute path `{obj_relative_path}`')

        possible_abs_path = self.obj_context_abs_path(obj_relative_path)

        if possible_abs_path in self:
            return possible_abs_path

        if not self.current_obj_path.startswith(ObjectPath.builtins_path()):
            possible_abs_path = ObjectPath.builtins_path() + obj_relative_path

            if possible_abs_path in self:
                return possible_abs_path

        if self.current_obj_path.is_root:
            return None

        for i in range(len(self.current_obj_path)):
            possible_abs_path = self.current_obj_path[:i + 1] + obj_relative_path

            if possible_abs_path in self:
                return possible_abs_path

        return None

    def find_obj_type(self, relative_path: ObjectPath | list | tuple | str):
        obj_relative_path = relative_path if isinstance(relative_path, ObjectPath) \
            else ObjectPath.relative(relative_path)

        obj_abs_path = self.find_obj_abs_path(obj_relative_path)

        if obj_abs_path is None:
            return None

        return ObjectType(obj_abs_path)

    def define(self, obj_path: ObjectPath | str, obj: NamespaceObject | dict[str, any], ignore_global_namespace=True):
        obj_path = obj_path if isinstance(obj_path, ObjectPath) else ObjectPath.relative(obj_path)

        if obj_path.is_abs and (
                len(obj_path) - 1 != len(self.current_obj_path) or not obj_path.startswith(self.current_obj_path)
        ):
            raise ValueError(f'unacceptable object path `{obj_path}`')

        if obj_path in self and (not ignore_global_namespace or obj_path[-1] in self.current_dict):
            return None

        self[obj_path] = obj

        return obj

    def define_obj(self, name: str, type: ObjectType, ignore_global_namespace=True) -> dict[str, any] | None:
        return self.define(name, {'type': type}, ignore_global_namespace)

    def get_type(self, obj_path: ObjectPath):
        return None if obj_path not in self else self[obj_path]['type']

    def define_namespace_object(self, name: str, type: ObjectType, ignore_global_namespace=True):
        if not type.is_namespace:
            return None

        new_obj = self.define_obj(name, type, ignore_global_namespace)
        new_obj.update(body=dict())
        return new_obj

    def define_module_object(self, name: str, directory_or_file: TextFile | str, ignore_global_namespace=True):
        new_obj = self.define_namespace_object(name, MODULE_TYPE, ignore_global_namespace)
        new_obj.update(path=directory_or_file)

        return new_obj

    def is_root_object(self, obj_path: ObjectPath, type: ObjectType = None):
        if obj_path not in self:
            return False

        return self[obj_path]['type'].is_root and (type is None or self[obj_path]['type'] == type)

    def get_root_object(self, obj_path: ObjectPath):
        if not self.is_root_object(obj_path):
            return None

        return self[obj_path]

    def goto(self, obj_path: ObjectPath):
        obj = self[obj_path]

        obj_type = obj['type']

        if obj is None or not obj_type.is_namespace and not obj_type.is_func:
            return False

        self.current_dict = obj['body']
        self.current_obj_path = self.obj_context_abs_path(obj_path)

        return True

    def go_up(self):
        return self.goto(self.current_obj_path.go_up())

    def finish_subbody(self):
        self.go_up()
        del self.current_dict['$body']

    def start_subbody(self):
        new_obj = self.define_namespace_object('$body', BODY_TYPE)

        self.goto(ObjectPath.relative('$body'))

        return new_obj

    def is_type(self, obj_type: ObjectType):
        return self.is_root_object(obj_type.type_name, TYPE_TYPE)

    def is_var(self, obj_path: ObjectPath):
        return obj_path in self and not self.is_root_object(obj_path)

    def define_var(self, name: str, type: ObjectType, mutable: bool, value: dict[str, any] = None):
        if not self.is_type(type):
            return None

        new_obj = self.define_obj(name, type)

        if new_obj is None:
            return None

        new_obj.update({
            'value': value,
            'mutable': mutable
        })

        return new_obj

    def var_is_mutable(self, obj_path: ObjectPath):
        if not self.is_var(obj_path):
            return False

        return self[obj_path]['mutable']

    def var_is_inited(self, obj_path: ObjectPath):
        if not self.is_var(obj_path):
            return False

        return self[obj_path]['value'] is not None

    def can_set_var_with(self, obj_path: ObjectPath, value: dict[str, any]):
        if not self.is_var(obj_path):
            return False

        return self[obj_path]['type'] == value['value-type'] and (
                not self.var_is_inited(obj_path) or self.var_is_mutable(obj_path)
        )

    def set_var(self, obj_path: ObjectPath, value: dict[str, any]):
        if self.can_set_var_with(obj_path, value):
            self[obj_path]['value'] = value
            return True

        return False

    def __str__(self):
        return json_encode(self.root_object)


__all__ = (
    'Namespace',
)
