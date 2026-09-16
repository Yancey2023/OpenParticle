from __future__ import annotations
import struct
from random import random
from typing import BinaryIO


class Vec3:
    def __init__(self, x: float, y: float, z: float):
        self.x = x
        self.y = y
        self.z = z

    def write_to_file(self, fp: BinaryIO) -> None:
        fp.write(struct.pack('>f', self.x))
        fp.write(struct.pack('>f', self.y))
        fp.write(struct.pack('>f', self.z))

    @staticmethod
    def random(range: float) -> Vec3:
        return Vec3((random() * 2 - 1) * range, (random() * 2 - 1) * range, (random() * 2 - 1) * range)

    @staticmethod
    def zero() -> Vec3:
        return Vec3(0, 0, 0)

    def add(self, other: Vec3) -> Vec3:
        return Vec3(self.x + other.x, self.y + other.y, self.z + other.z)

    def subtract(self, other: Vec3) -> Vec3:
        return Vec3(self.x - other.x, self.y - other.y, self.z - other.z)

    def multiply(self, other: Vec3) -> Vec3:
        return Vec3(self.x * other.x, self.y * other.y, self.z * other.z)

    def multiply_num(self, num: float) -> Vec3:
        return Vec3(self.x * num, self.y * num, self.z * num)

    def divide(self, other: Vec3) -> Vec3:
        return Vec3(self.x / other.x, self.y / other.y, self.z / other.z)
