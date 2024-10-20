from src.utils.arataki_typing import TextFile, json_encode
from src.cofty import CoftyProcessor
from src.utils.namespace_utils import ObjectPath

processor = CoftyProcessor(TextFile.read('test/main.cft'))

print(processor.process().flat_map(lambda tree: json_encode(tree)).unwrap(), processor.namespace, sep='\n')
