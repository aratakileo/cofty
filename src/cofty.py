from src.utils.namespace_utils import ObjectPath, NotInitedModule
from os.path import dirname, basename, abspath, isfile
from src.utils.arataki_typing import TextFile, Result
from src.utils.exceptions import InitModuleFileError
from src.core.parser import Parser, ParserContext
from src.utils.parser_utils import ErrorBuffer
from src.core.namespace import Namespace
from src.core.errors import ErrorHandler
from src.core.lexer import parse_tokens
from pathlib import Path


BUILTIN_PROJECT_PATH = 'src/core/builtin'


class CoftyProcessor:
    error_handler: ErrorHandler = ErrorHandler()
    namespace: Namespace = Namespace()
    main_file: TextFile
    project_path: str
    bodies: dict[str, dict[str, any]]
    error_buffer: ErrorBuffer

    def __init__(self, main_file: TextFile):
        self.main_file = main_file
        self.project_path = dirname(main_file.abspath)
        self.bodies = dict()
        self.error_buffer = ErrorBuffer()

        self.namespace.define_module_object('$builtins', BUILTIN_PROJECT_PATH)
        self.namespace.define_module_object('$main', main_file)

    def process_file(self, file: TextFile, init_module: bool, define_file_module=True):
        if not (init_module or define_file_module):
            return

        project_path_length = len(self.project_path) + 1
        module_obj_relative_path = ObjectPath.relative(
            file.abspath[project_path_length:-4].replace('\\', '/').split('/')
        )
        file_module_name = module_obj_relative_path[-1]
        default_obj_path = self.namespace.current_obj_path

        if define_file_module:
            temp_obj_path = default_obj_path
            temp_project_path = self.project_path.replace('\\', '/')

            for path_segment in module_obj_relative_path.segments[:-1]:
                temp_obj_path += path_segment
                temp_project_path += '/' + path_segment

                if temp_obj_path in self.namespace:
                    self.namespace.goto(temp_obj_path)
                    continue

                self.namespace.define_module_object(path_segment, temp_project_path)
                self.namespace.goto(temp_obj_path)

            if file_module_name != '__init__':
                self.namespace.define_module_object(file_module_name, file)
                self.namespace.goto(temp_obj_path + file_module_name)

        if not init_module:
            last_module_name = self.namespace.current_obj_path[-1]

            self.namespace.goto(self.namespace.current_obj_path[:-1])

            del self.namespace.current_dict[last_module_name]

            current_obj_path_snapshot = self.namespace.current_obj_path

            def module_initializer():
                del self.namespace[current_obj_path_snapshot]['body'][last_module_name]
                self.process_file(file, True, define_file_module)

            self.namespace.define(last_module_name, NotInitedModule(module_initializer))

            if self.namespace.current_obj_path != default_obj_path:
                self.namespace.goto(default_obj_path)

            return Result.ok('SUCCESSFUL')

        tokens_result = parse_tokens(file)

        if tokens_result.is_err:
            return tokens_result

        parser = Parser(ParserContext(tokens_result.ok_value, file, self.namespace, self.error_buffer))
        body_result = parser.parse()

        if self.namespace.current_obj_path != default_obj_path:
            self.namespace.goto(default_obj_path)

        if body_result.is_ok:
            self.bodies[f'{basename(self.project_path)}.{module_obj_relative_path.__str__()}'] = body_result.ok_value

        return body_result

    def process_project(self, project_path_or_main_file: str | TextFile, define_project_module=True):
        ignore_files = tuple()

        if isinstance(project_path_or_main_file, str):
            self.project_path = abspath(project_path_or_main_file)
            main_file = None
        else:
            main_file = project_path_or_main_file
            self.project_path = dirname(main_file.abspath)
            ignore_files = (main_file.abspath,)

        module_init_file_path = self.project_path + '/__init__.cft'

        if isfile(module_init_file_path):
            if main_file is not None:
                raise InitModuleFileError(
                    f'it is impossible to process `{module_init_file_path}` '
                    f'while main file `{main_file.abspath}` described'
                )

            module_init_file = TextFile.read(module_init_file_path)
            ignore_files = (*ignore_files, module_init_file.abspath)
        else:
            module_init_file = None

        project_module_name = basename(self.project_path)
        default_obj_path = self.namespace.current_obj_path

        if define_project_module:
            self.namespace.define_module_object(project_module_name, self.project_path)
            self.namespace.goto(default_obj_path + project_module_name)

        for file_path in Path(self.project_path).glob('*.cft'):
            if file_path.__str__() in ignore_files:
                continue

            result = self.process_file(TextFile.read(file_path.__str__()), False)

            if result.is_err:
                return result

        if main_file is not None:
            result = self.process_file(main_file, True, False)

            if result.is_err:
                return result

        if module_init_file is not None:
            result = self.process_file(module_init_file, True, False)

            if result.is_err:
                return result

        if self.namespace.current_obj_path != default_obj_path:
            self.namespace.goto(default_obj_path)

        return Result.ok('SUCCESSFUL')

    def process(self):
        self.namespace.goto(ObjectPath.builtins_path())

        result = self.process_project(BUILTIN_PROJECT_PATH, define_project_module=False)

        if result.is_err:
            return result

        self.namespace.goto(ObjectPath.abs('$main'))

        result = self.process_project(self.main_file, define_project_module=False)

        return result if result.is_err else Result.ok(self.bodies)
