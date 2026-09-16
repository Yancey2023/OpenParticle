from __future__ import annotations

from abc import ABCMeta, abstractmethod
from typing import Callable

from openparticle.api.math import matrix_util
from openparticle.api.data.Identifier import Identifier
from openparticle.api.math.vec3 import Vec3
from openparticle.api.data.data_color import DataColor, DataColorStatic, DataColorFree
from openparticle.api.data.data_particle import DataParticle, DataParticleSingle, DataParticleCompound, \
    DataParticleManager, DataParticleTransform
from openparticle.api.data.data_matrix import DataMatrix, DataMatrixStatic, DataMatrixFree


class Particle:
    __metaclass__ = ABCMeta

    def __init__(self, tick_add: int, age: int):
        self._is_use_cache = False
        self.cache = None
        self.tick_add = tick_add
        self.max_age = age

    @abstractmethod
    def run(self) -> DataParticle:
        pass

    def run_with_cache(self) -> DataParticle:
        if self._is_use_cache and self.cache is not None:
            return self.cache
        dataParticle = self.run()
        if self._is_use_cache:
            self.cache = dataParticle
        return dataParticle

    def use_cache(self) -> Particle:
        self._is_use_cache = True
        return self

    def output(self, path: str) -> None:
        DataParticleManager(self.run()).output(path)

    @classmethod
    def create(cls, identifier: Identifier, age: int) -> Particle:
        return ParticleDefault(identifier, age)

    def repeat(self, times: int) -> Particle:
        return ParticleCompound([self for _ in range(times)])

    @classmethod
    def compound(cls, children: list[Particle]) -> Particle:
        return ParticleCompound(children)

    def shape(self, positions: list[Vec3]) -> Particle:
        return Particle.compound([self.offset_static(position) for position in positions])

    def tick(self, tick: int) -> Particle:
        return ParticleTransform(self, None, None, tick)

    def transform(self, data_matrix: DataMatrix) -> Particle:
        return ParticleTransform(self, data_matrix, None, 0)

    def color(self, data_color: DataColor) -> Particle:
        return ParticleTransform(self, None, data_color, 0)

    def offset_static(self, offset: Vec3) -> Particle:
        return self.transform(DataMatrixStatic(matrix_util.offset(offset)))

    def offset_free(self, offsets: list[Vec3]) -> Particle:
        return self.transform(DataMatrixFree([matrix_util.offset(offset) for offset in offsets]))

    def rotate_static(self, rotate: Vec3) -> Particle:
        return self.transform(DataMatrixStatic(matrix_util.rotateXYZ(rotate)))

    def rotate_free(self, rotates: list[Vec3]) -> Particle:
        return self.transform(DataMatrixFree([matrix_util.rotateXYZ(rotate) for rotate in rotates]))

    def color_static(self, color: int) -> Particle:
        return self.color(DataColorStatic(color))

    def color_free(self, colors: list[int]) -> Particle:
        return self.color(DataColorFree(colors))

    def apply(self, function: Callable[[Particle], Particle]) -> Particle:
        return ParticleFunction(self, function).use_cache()

    def apply_random(self, function: Callable[[Particle], Particle]) -> Particle:
        return ParticleFunction(self, function)


class ParticleDefault(Particle):

    def __init__(self, identifier: Identifier, age: int):
        super().__init__(0, age)
        self.__identifier = identifier
        self._is_use_cache = True

    def run(self) -> DataParticle:
        return DataParticleSingle(self.__identifier, self.max_age)


class ParticleCompound(Particle):

    def __init__(self, children: list[Particle]):
        if len(children) == 0:
            raise RuntimeError("No children")
        tick_add = min(child.tick_add for child in children)
        age = max(child.tick_add + child.max_age for child in children) - tick_add
        super().__init__(tick_add, age)
        self.__children = children
        self._is_use_cache = True
        for child in children:
            if not child._is_use_cache:
                self._is_use_cache = False
                break

    def run(self) -> DataParticle:
        return DataParticleCompound([particle.run_with_cache() for particle in self.__children])


class ParticleTransform(Particle):

    def __init__(self, particle: Particle, data_matrix: None | DataMatrix, data_color: None | DataColor, tick_add: int):
        super().__init__(particle.tick_add + tick_add, particle.max_age)
        self.__particle = particle
        self.__data_matrix = data_matrix
        self.__data_color = data_color
        self.__tick_add = tick_add
        self._is_use_cache = particle._is_use_cache

    def run(self) -> DataParticle:
        return DataParticleTransform(
            self.__particle.run_with_cache(),
            self.__data_matrix,
            self.__data_color,
            self.__tick_add
        )


class ParticleFunction(Particle):

    def __init__(self, particle: Particle, function: Callable[[Particle], Particle]):
        super().__init__(particle.tick_add, particle.max_age)
        self.__particle = particle
        self.__function = function

    def run(self) -> DataParticle:
        new_particle = self.__function(self.__particle)
        if new_particle.max_age != self.__particle.max_age:
            raise RuntimeError('Particle age mismatch')
        return new_particle.run_with_cache()
