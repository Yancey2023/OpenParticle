import math
from random import random
from typing import Callable

import numpy as np
from numpy.typing import NDArray

from openparticle.api.data.data_matrix import DataMatrixFree
from openparticle.api.math import matrix_util
from openparticle.api.math.vec3 import Vec3
from openparticle.api.particle.particle import Particle


def random_tick(random_range: float) -> Callable[[Particle], Particle]:
    return lambda particle: particle.tick(int(random() * random_range))


def random_position(random_range: float) -> Callable[[Particle], Particle]:
    return lambda particle: particle.offset_static(Vec3.random(random_range))


def random_rotate() -> Callable[[Particle], Particle]:
    return lambda particle: particle.rotate_static(Vec3.random(math.pi))


def random_move(rotate_change_range: float, speed: float) -> Callable[[Particle], Particle]:
    def inner(particle: Particle) -> Particle:
        rotate = Vec3.random(math.pi)
        rotate_change = Vec3.zero()
        offset = Vec3.zero()
        matrix_list: list[NDArray[np.float32]] = list()
        for age in range(particle.max_age):
            rotate_matrix = matrix_util.rotateXYZ(rotate)
            matrix_list.append(np.dot(rotate_matrix, matrix_util.offset(offset)))
            rotate = rotate.add(rotate_change)
            if age % 10 == 0:
                rotate_change = Vec3.random(rotate_change_range)
            offset = offset.add(matrix_util.apply(rotate_matrix, Vec3(0, speed, 0)))
        return particle.transform(DataMatrixFree(matrix_list))

    return inner
