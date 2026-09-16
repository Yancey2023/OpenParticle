import math

import numpy as np

from openparticle.api.math.vec3 import Vec3


def line(x1, y1, z1, x2, y2, z2, count) -> list[Vec3]:
    x_add = (x2 - x1) / (count - 1)
    y_add = (y2 - y1) / (count - 1)
    z_add = (z2 - z1) / (count - 1)
    result: list[Vec3] = list()
    for i in range(count):
        result.append(Vec3(x1 + x_add * i, y1 + y_add * i, z1 + z_add * i))
    return result


def fourier_transform(radius1: float, angular_velocity1: float,
                      radius2: float, angular_velocity2: float,
                      times: int) -> list[Vec3]:
    return [Vec3(
        radius1 * math.sin(angular_velocity1 * i) + radius2 * math.sin(angular_velocity2 * i),
        0,
        radius1 * math.cos(angular_velocity1 * i) + radius2 * math.cos(angular_velocity2 * i)
    ) for i in range(times)]


def compound(shapes: list[list[Vec3]]) -> list[Vec3]:
    result: list[Vec3] = list()
    for shape in shapes:
        for i in shape:
            result.append(i)
    return result


def cuboid(length: float, width: float, height: float, interval: float) -> list[Vec3]:
    result: list[Vec3] = list()
    for x in np.arange(-length / 2, length / 2, interval):
        for y in np.arange(-width / 2, width / 2, interval):
            for z in np.arange(-height / 2, height / 2, interval):
                result.append(Vec3(x, y, z))
    return result
