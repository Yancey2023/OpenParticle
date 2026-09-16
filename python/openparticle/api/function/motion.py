import math
from typing import Callable

import numpy as np

from openparticle.api.math.vec3 import Vec3
from openparticle.api.particle.particle import Particle
from openparticle.api.shape import shape


def offset_speed(speed: Vec3) -> Callable[[Particle], Particle]:
    def inner(particle: Particle) -> Particle:
        return particle.offset_free([
            Vec3(speed.x * age, speed.y * age, speed.z * age)
            for age in range(particle.max_age)])

    return inner


def rotate_speed(speed: Vec3) -> Callable[[Particle], Particle]:
    def inner(particle: Particle) -> Particle:
        return particle.rotate_free([
            Vec3(speed.x * age, speed.y * age, speed.z * age)
            for age in range(particle.max_age)])

    return inner


def butterfly(particle_count: int) -> Callable[[Particle], Particle]:
    def inner(particle: Particle) -> Particle:
        particle_list: list[Particle] = list()
        for theta in np.arange(0, 2 * math.pi, 2 * math.pi / particle_count):
            r = 2 * math.fabs(math.sin(2 * theta)) + math.fabs(math.sin(4 * theta))
            x = r * math.sin(theta)
            y = r * math.cos(theta)
            particle_list.append(particle.offset_free([
                Vec3(x, math.fabs(y) * math.sin(0.5 * age), y)
                for age in range(particle.max_age)]))
        return Particle.compound(particle_list)

    return inner


def anime(shape1: list[Vec3],
          shape2: list[Vec3],
          time_transform: int,
          time_shape1: int = 0,
          time_shape2: int = 0
          ) -> Callable[[Particle], Particle]:
    def inner(particle: Particle) -> Particle:
        particle_list: list[Particle] = list()
        count = max(len(shape1), len(shape2))
        if count == 0:
            raise RuntimeError('shape is unavailable')
        for i in range(count):
            start_pos = shape1[min(i, len(shape1) - 1)]
            end_pos = shape2[min(i, len(shape2) - 1)]
            positions: list[Vec3] = list()
            for j in range(time_shape1):
                positions.append(Vec3(start_pos.x, start_pos.y, start_pos.z))
            transform_position_list = shape.line(
                start_pos.x, start_pos.y, start_pos.z,
                end_pos.x, end_pos.y, end_pos.z,
                time_transform
            )
            for position in transform_position_list:
                positions.append(position)
            for j in range(time_shape2):
                positions.append(Vec3(end_pos.x, end_pos.y, end_pos.z))
            particle_list.append(particle.offset_free(positions))
        return Particle.compound(particle_list)

    return inner


def parabolic_motion(initial_speed: Vec3, g: float, f: float) -> Callable[[Particle], Particle]:
    def inner(particle: Particle) -> Particle:
        position_list: list[Vec3] = list()
        position = Vec3.zero()
        speed = initial_speed
        for age in range(particle.max_age):
            position = position.add(speed)
            speed = speed.multiply_num(f).subtract(Vec3(0, g, 0))
            position_list.append(position)
        return particle.offset_free(position_list)

    return inner


def rotate(rotate: Vec3, center: Vec3) -> Callable[[Particle], Particle]:
    def inner(particle: Particle) -> Particle:
        return (particle.offset_static(center.multiply_num(-1))
                .apply(rotate_speed(rotate.multiply_num(1 / particle.max_age)))
                .offset_static(center))

    return inner


def rotate_speed_in_time_range(speed: Vec3, start: float, end: float) -> Callable[[Particle], Particle]:
    def inner(particle: Particle) -> Particle:
        rotate_list: list[Vec3] = list()
        for age in range(particle.max_age):
            if start > age:
                rotate_list.append(Vec3.zero())
            elif age > end:
                rotate_list.append(Vec3(speed.x * (end - start), speed.y * (end - start), speed.z * (end - start)))
            else:
                rotate_list.append(Vec3(speed.x * (age - start), speed.y * (age - start), speed.z * (age - start)))
        return particle.rotate_free(rotate_list)

    return inner


def rotate_in_time_range(rotate: Vec3, center: Vec3, start: float, end: float) -> Callable[[Particle], Particle]:
    def inner(particle: Particle) -> Particle:
        return (particle.offset_static(center.multiply_num(-1))
                .apply(rotate_speed_in_time_range(rotate.multiply_num(1 / (end - start)), start, end))
                .offset_static(center))

    return inner
