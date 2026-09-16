from __future__ import annotations
import struct
from abc import ABCMeta, abstractmethod
from typing import BinaryIO

import numpy as np
from numpy.typing import NDArray

from openparticle.api.data.data_color import DataColor
from openparticle.api.data.data_matrix import DataMatrix, DataMatrixFree, DataMatrixStatic
from openparticle.api.data.Identifier import Identifier


class DataParticle:
    __metaclass__ = ABCMeta

    def __init__(self):
        self.id = -1
        self.is_collect = False
        pass

    @abstractmethod
    def write_to_file(self, fp: BinaryIO) -> None:
        pass

    @abstractmethod
    def get_particle_count(self) -> int:
        pass

    @staticmethod
    def collect(root: DataParticle, data_particle_list: list[DataParticle]) -> None:
        if len(data_particle_list) != 0:
            raise RuntimeError('data_particle_list should be empty')
        root._collect(data_particle_list)
        for data_particle in data_particle_list:
            data_particle.is_collect = False

    @abstractmethod
    def _collect(self, data_particle_list: list[DataParticle]) -> None:
        pass

    @abstractmethod
    def optimize(self):
        pass


# 单个粒子
class DataParticleSingle(DataParticle):

    def __init__(self, identifier: Identifier, age: int):
        super().__init__()
        self.identifier = identifier
        self.age = age

    def write_to_file(self, fp: BinaryIO) -> None:
        fp.write(struct.pack('>B', 0))
        if self.identifier.id < 0:
            raise 'id should be a positive integer'
        fp.write(struct.pack('>I', self.identifier.id))
        fp.write(struct.pack('>I', self.age))

    def get_particle_count(self) -> int:
        return 1

    def _collect(self, data_particle_list: list[DataParticle]) -> None:
        if not self.is_collect:
            self.is_collect = True
            data_particle_list.append(self)

    def optimize(self) -> None:
        pass


# 合并多个粒子
class DataParticleCompound(DataParticle):

    def __init__(self, data_particles: list[DataParticle]):
        super().__init__()
        self.children = data_particles

    def write_to_file(self, fp: BinaryIO) -> None:
        fp.write(struct.pack('>B', 1))
        fp.write(struct.pack('>I', len(self.children)))
        for dataParticle in self.children:
            if dataParticle.id < 0:
                raise 'id should be a positive integer'
            fp.write(struct.pack('>I', dataParticle.id))

    def get_particle_count(self) -> int:
        __sum = 0
        for data_particle in self.children:
            __sum += data_particle.get_particle_count()
        return __sum

    def _collect(self, data_particle_list: list[DataParticle]) -> None:
        if not self.is_collect:
            for child in self.children:
                child._collect(data_particle_list)
            self.is_collect = True
            data_particle_list.append(self)

    def optimize(self) -> None:
        tasks: list[DataParticle] = self.children.copy()
        newChildren: list[DataParticle] = list()
        i = 0
        while i < len(tasks):
            child = tasks[i]
            i += 1
            if isinstance(child, DataParticleCompound):
                for child1 in child.children:
                    tasks.append(child1)
                continue
            elif isinstance(child, DataParticleTransform):
                if child.tick_add == 0 and child.data_matrix is None and child.data_color is None:
                    tasks.append(child.child)
                    continue
            newChildren.append(child)
        self.children = newChildren


# 改变粒子的时间，位置，颜色
class DataParticleTransform(DataParticle):

    def __init__(self, child: DataParticle,
                 data_matrix: None | DataMatrix,
                 data_color: None | DataColor,
                 tick_add: int):
        super().__init__()
        self.child = child
        self.data_matrix = data_matrix
        self.data_color = data_color
        self.tick_add = tick_add

    def write_to_file(self, fp: BinaryIO) -> None:
        fp.write(struct.pack('>B', 2))
        if self.child.id < 0:
            raise 'id should be a positive integer'
        fp.write(struct.pack('>I', self.child.id))
        DataMatrix.write_data_matrix(fp, self.data_matrix)
        DataColor.write_data_color(fp, self.data_color)
        fp.write(struct.pack('>I', self.tick_add))

    def get_particle_count(self) -> int:
        return self.child.get_particle_count()

    def _collect(self, data_particle_list: list[DataParticle]) -> None:
        if not self.is_collect:
            self.child._collect(data_particle_list)
            self.is_collect = True
            data_particle_list.append(self)

    def optimize(self) -> None:
        if isinstance(self.child, DataParticleCompound):
            if len(self.child.children) > 1:
                return
            self.child = self.child.children[0]
        if isinstance(self.child, DataParticleTransform):
            if self.child.data_matrix is not None:
                if self.data_matrix is None:
                    self.data_matrix = self.child.data_matrix
                else:
                    age = 0
                    if isinstance(self.data_matrix, DataMatrixFree):
                        age = len(self.data_matrix.matrices)
                    elif isinstance(self.child.data_matrix, DataMatrixFree):
                        age = len(self.child.data_matrix.matrices)
                    if age == 0:
                        self.data_matrix = DataMatrixStatic(np.dot(
                            self.data_matrix.get_matrix(0),
                            self.child.data_matrix.get_matrix(0)
                        ))
                    else:
                        final_matrices: list[NDArray[np.float32]] = list()
                        for i in range(age):
                            final_matrices.append(np.dot(
                                self.data_matrix.get_matrix(i),
                                self.child.data_matrix.get_matrix(i)
                            ))
                        self.data_matrix = DataMatrixFree(final_matrices)
            if self.data_color is None:
                self.data_color = self.child.data_color
            self.tick_add += self.child.tick_add
            self.child = self.child.child


class DataParticleManager:

    def __init__(self, root: DataParticle):
        if root is DataParticleCompound:
            raise TypeError('root data particle cannot be compounded')
        self.__root = root
        self.__data_particle_list: list[DataParticle] = list()
        DataParticle.collect(self.__root, self.__data_particle_list)
        for data_particle in self.__data_particle_list:
            data_particle.optimize()
        self.__data_particle_list.clear()
        DataParticle.collect(self.__root, self.__data_particle_list)
        self.__identifier_list: list[Identifier] = list()
        for data_particle in self.__data_particle_list:
            if isinstance(data_particle, DataParticleSingle):
                if data_particle.identifier not in self.__identifier_list:
                    self.__identifier_list.append(data_particle.identifier)

    def write_to_file(self, fp: BinaryIO) -> None:
        fp.write(struct.pack('>I', len(self.__identifier_list)))
        for i, identifier in enumerate(self.__identifier_list):
            identifier.id = i
            identifier.write_to_file(fp)
        fp.write(struct.pack('>I', len(self.__data_particle_list)))
        for i, data_particle in enumerate(self.__data_particle_list):
            data_particle.id = i
            data_particle.write_to_file(fp)
        fp.write(struct.pack('>I', self.__root.id))

    def output(self, path: str) -> None:
        with open(path, 'wb') as fp:
            self.write_to_file(fp)
