import math

import numpy as np

from openparticle.api.math.vec3 import Vec3
from numpy.typing import NDArray


def offset(xyz: Vec3) -> NDArray[np.float32]:
    return np.array(
        [[1, 0, 0, xyz.x],
         [0, 1, 0, xyz.y],
         [0, 0, 1, xyz.z],
         [0, 0, 0, 1]],
        dtype=np.float32)


def rotate_x(rx: float) -> NDArray[np.float32]:
    sin_rx = math.sin(rx)
    cos_rx = math.cos(rx)
    return np.array(
        [[1, 0, 0, 0],
         [0, cos_rx, -sin_rx, 0],
         [0, sin_rx, cos_rx, 0],
         [0, 0, 0, 1]],
        dtype=np.float32)


def rotate_y(ry: float) -> NDArray[np.float32]:
    sin_ry = math.sin(ry)
    cos_ry = math.cos(ry)
    return np.array(
        [[cos_ry, 0, -sin_ry, 0],
         [0, 1, 0, 0],
         [sin_ry, 0, cos_ry, 0],
         [0, 0, 0, 1]],
        dtype=np.float32)


def rotate_z(ry: float) -> NDArray[np.float32]:
    sin_ry = math.sin(ry)
    cos_ry = math.cos(ry)
    return np.array(
        [[cos_ry, -sin_ry, 0, 0],
         [sin_ry, cos_ry, 0, 0],
         [0, 0, 1, 0],
         [0, 0, 0, 1]],
        dtype=np.float32)


def rotateXYZ(rotate: Vec3) -> NDArray[np.float32]:
    return np.dot(rotate_x(rotate.x), np.dot(rotate_y(rotate.y), rotate_z(rotate.z)))


def apply(matrix: NDArray[np.float32], vec3: Vec3) -> Vec3:
    result = np.dot(matrix, np.array([[vec3.x], [vec3.y], [vec3.z], [1]], dtype=np.float32))
    return Vec3(float(result[0, 1]), float(result[1, 1]), float(result[2, 1]))
