from __future__ import annotations
import struct
from abc import ABCMeta, abstractmethod
from typing import BinaryIO
import numpy as np
from numpy.typing import NDArray


class DataMatrix:
    __metaclass__ = ABCMeta

    @abstractmethod
    def _write_to_file(self, fp: BinaryIO) -> None:
        pass

    @abstractmethod
    def get_matrix(self, age: int) -> NDArray[np.float32]:
        pass

    @staticmethod
    def _write_matrix(fp: BinaryIO, matrix: NDArray[np.float32]) -> None:
        if len(matrix.shape) != 2 or matrix.shape[0] != 4 or matrix.shape[1] != 4:
            raise ValueError('Invalid matrix shape')
        fp.write(struct.pack('>ffffffffffffffff',
                             matrix[0, 0], matrix[1, 0], matrix[2, 0], matrix[3, 0],
                             matrix[0, 1], matrix[1, 1], matrix[2, 1], matrix[3, 1],
                             matrix[0, 2], matrix[1, 2], matrix[2, 2], matrix[3, 2],
                             matrix[0, 3], matrix[1, 3], matrix[2, 3], matrix[3, 3]
                             ))
        pass

    @staticmethod
    def write_data_matrix(fp: BinaryIO, dataVec3: None | DataMatrix) -> None:
        if dataVec3 is None:
            fp.write(struct.pack('>B', 0))
        else:
            dataVec3._write_to_file(fp)


# 位置不变
class DataMatrixStatic(DataMatrix):

    def __init__(self, matrix: NDArray[np.float32]):
        self.matrix = matrix

    def _write_to_file(self, fp: BinaryIO) -> None:
        fp.write(struct.pack('>B', 1))
        DataMatrix._write_matrix(fp, self.matrix)

    def get_matrix(self, age: int) -> NDArray[np.float32]:
        return self.matrix


# 为每一个tick设置位置
class DataMatrixFree(DataMatrix):

    def __init__(self, matrices: list[NDArray[np.float32]]):
        self.matrices = matrices

    def _write_to_file(self, fp: BinaryIO) -> None:
        fp.write(struct.pack('>B', 2))
        fp.write(struct.pack('>I', len(self.matrices)))
        for matrix in self.matrices:
            DataMatrix._write_matrix(fp, matrix)

    def get_matrix(self, age: int) -> NDArray[np.float32]:
        return self.matrices[age]
