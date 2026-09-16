import math

from openparticle.api.activity.activity import Activity
from openparticle.api.function import motion
from openparticle.api.math.vec3 import Vec3
from openparticle.api.particle import particle_type
from openparticle.api.particle.particle import Particle
from openparticle.api.shape import shape


class TestActivity2(Activity):

    def get_path(self) -> str:
        # 文件路径
        return '.\\output\\2.par'

    def get_position(self) -> Vec3:
        # 可以将粒子偏移到实际要生成粒子的位置
        return Vec3(-2, -56, 0)

    def create_particle(self) -> None:
        self.add_particle(
            Particle.create(particle_type.end_rod, 40)
            .shape(shape.fourier_transform(1, 7 * math.pi / 180, 3, -4 * math.pi / 180, 360))
            .color_static(0x00FF00FF)
            .apply(motion.offset_speed(Vec3(0, 0.05, 0)))
            .apply(motion.rotate_speed(Vec3(0, 0.1, 0)))
        )

# 更多功能

# 单个粒子: Particle.create(identifier, age)
# 合并粒子: Particle.compound(list[Particle]) 或者 .shape(Callable[[Particle], list[Particle]])
# 改变时间: .tick(int)
# 改变位置: .offset_static(DataVec3) 和 .offset_free(list[DataVec3])
# 改变角度: .rotate_static(DataVec3) 和 .rotate_free(list[DataVec3])
# 改变颜色: .color_static(DataColor) 和 .color_free(DataColor)

# DataMatrix 矩阵映射支持两种模式（4*4的矩阵）
# 1. DataMatrixStatic 固定的矩阵
# 2. DataMatrixFree 为每一个tick设置矩阵

# DataColor 颜色支持两种模式
# 1. DataColorStatic 固定颜色
# 2. DataColorFree 为每一个tick设置颜色
