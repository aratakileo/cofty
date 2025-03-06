from os.path import dirname, abspath, isfile, join as path_join, normpath, isdir
from src.utils.arataki_typing import TextFile, Result, ImmutableObject
from src.utils.namespace_utils import ObjectPath, NotInitedModuleObject
from src.core.parser import ParserContext, Parser
from src.core.errors import ErrorHandler
from src.core.namespace import Namespace
from src.core.lexer import parse_tokens
from os import walk as os_walk

from src.utils.parser_utils import ErrorBuffer

BUILTINS_FILE = TextFile.read('src/core/builtin/__init__.cft')


class ProcessContext(ImmutableObject):
    def __init__(
            self,
            namespace: Namespace,
            error_buffer: ErrorBuffer,
            bodies: dict[str, dict[str, any]]
    ):
        super().__init__()
        self.namespace = namespace
        self.error_buffer = error_buffer
        self.bodies = bodies

    @staticmethod
    def new():
        return ProcessContext(Namespace(), ErrorBuffer(), dict())


class ModuleInitializer(ImmutableObject):
    def __init__(
            self,
            root_obj_abs_path: ObjectPath,
            root_dir: str,
            file: TextFile,
            context: ProcessContext,
            create_obj: bool,
            lazyinit: bool
    ):
        super().__init__()

        root_obj_abs_path.is_abs_or_throw()

        self.root_obj_path = root_obj_abs_path
        self.root_dir = normpath(abspath(root_dir))
        self.file = file
        self.context = context
        self.create_obj = create_obj
        self.lazyinit = lazyinit

    def init(self):
        obj_relative_path = ObjectPath.relative(
            self.file.abspath[len(self.root_dir) + 1:-len('.cft')].replace('\\', '/').split('/')
        )
        obj_abs_path = self.root_obj_path + obj_relative_path

        if self.lazyinit:
            return self.context.namespace.define_module(obj_abs_path, NotInitedModuleObject(self.notlazy()))

        if not self.create_obj:
            obj_abs_path = obj_abs_path.go_up()

        module_obj = self.context.namespace.define_module(obj_abs_path)
        tokens_result = parse_tokens(self.file)

        if tokens_result.is_err:
            self.context.error_buffer.error = tokens_result.err_value
            return None

        namespace_obj_path = self.context.namespace.current_obj_path
        self.context.namespace.goto(obj_abs_path)

        parser_context = ParserContext(
            tokens_result.ok_value,
            self.file,
            self.context.namespace,
            self.context.error_buffer
        )
        body_result = Parser(parser_context).parse()

        self.context.namespace.goto(namespace_obj_path)

        if body_result.is_err:
            return None

        self.context.bodies[str(obj_abs_path[1:])] = body_result.ok_value

        return module_obj

    def notlazy(self):
        return ModuleInitializer(
            self.root_obj_path,
            self.root_dir,
            self.file,
            self.context,
            self.create_obj,
            False
        )


class CoftyProcessor(ImmutableObject):
    def __init__(self, main_file: TextFile):
        super().__init__()
        self.error_handler = ErrorHandler()
        self.main_file = main_file
        self.context = ProcessContext.new()

        self.context.namespace.define_module(ObjectPath.builtins_path())
        self.context.namespace.define_module(ObjectPath.main_path())

    def __preprocess_modules(self, abs_root: ObjectPath, root_dir: str, ignore_files: list[str]):
        abs_root.is_abs_or_throw()

        if isfile(root_dir):
            raise NotADirectoryError(f'expected directory, but got file `{root_dir}`')

        if not isdir(root_dir):
            raise RuntimeError(f'directory `{root_dir}` does not exist')

        root_dir = normpath(root_dir)

        for dirpath, dirnames, filenames in os_walk(root_dir):
            for filename in [f for f in filenames if f.endswith('.cft')]:
                file_path = normpath(abspath(path_join(dirpath, filename)))

                if file_path in ignore_files:
                    continue

                ModuleInitializer(
                    abs_root,
                    root_dir,
                    TextFile.read(file_path),
                    self.context,
                    True,
                    True
                ).init()

                if self.context.error_buffer.has_err:
                    break

    def __process_root_file(self, file: TextFile, file_path: ObjectPath):
        file_path.is_abs_or_throw()

        root_dir = dirname(file.abspath)

        self.__preprocess_modules(file_path, root_dir, [file.abspath])
        ModuleInitializer(file_path, root_dir, file, self.context, False, False).init()

    def process(self):
        self.__process_root_file(BUILTINS_FILE, ObjectPath.builtins_path())

        if self.context.error_buffer.has_err:
            return Result.err(self.context.error_buffer.error)

        self.__process_root_file(self.main_file, ObjectPath.main_path())

        return self.context.error_buffer.ok_or_err_result(self.context.bodies)
