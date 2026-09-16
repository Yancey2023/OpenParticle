from __future__ import annotations
import struct
from abc import ABCMeta, abstractmethod
from typing import BinaryIO


class DataColor:
    __metaclass__ = ABCMeta

    @abstractmethod
    def _write_to_file(self, fp: BinaryIO) -> None:
        pass

    @staticmethod
    def write_data_color(fp: BinaryIO, dataColor: None | DataColor) -> None:
        if dataColor is None:
            fp.write(struct.pack('>B', 0))
        else:
            dataColor._write_to_file(fp)


# 颜色不变
class DataColorStatic(DataColor):

    def __init__(self, color: int):
        self.color = color

    def _write_to_file(self, fp: BinaryIO) -> None:
        fp.write(struct.pack('>B', 1))
        fp.write(struct.pack('>I', self.color))


# 为每一个tick设置颜色
class DataColorFree(DataColor):

    def __init__(self, colors: list[int]):
        self.colors = colors

    def _write_to_file(self, fp: BinaryIO) -> None:
        fp.write(struct.pack('>B', 2))
        fp.write(struct.pack('>I', len(self.colors)))
        for color in self.colors:
            fp.write(struct.pack('>I', color))
