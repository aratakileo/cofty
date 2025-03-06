from src.utils.arataki_typing import TextFile, json_encode
from src.cofty import CoftyProcessor


processor = CoftyProcessor(TextFile.read('test/main.cft'))

print(
    processor.process()
    .flat_map(lambda tree: f'Compiled successfully!\nBody:\n{json_encode(tree)}')
    .flat_map_err(lambda err: f'Compiled unsuccessfully\n{err}')
    .unwrap(),
    '\nNamespace:',
    processor.context.namespace,
    sep='\n'
)
