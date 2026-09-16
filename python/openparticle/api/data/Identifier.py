from __future__ import annotations
import struct
from typing import BinaryIO


class Identifier:

    def __init__(self, namespace: str, value: str):
        self.namespace = namespace
        self.value = value
        self.id = -1

    def write_to_file(self, fp: BinaryIO) -> None:
        if self.namespace == "minecraft":
            fp.write(struct.pack('?', True))
        else:
            fp.write(struct.pack('?', False))
            fp.write(struct.pack('>H', len(self.namespace)))
            fp.write(self.namespace.encode('utf-8'))
        fp.write(struct.pack('>H', len(self.value)))
        fp.write(self.value.encode('utf-8'))
