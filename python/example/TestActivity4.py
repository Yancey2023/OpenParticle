import math

from openparticle.api.activity.activity import Activity
from openparticle.api.function import motion
from openparticle.api.math.vec3 import Vec3
from openparticle.api.particle import particle_type
from openparticle.api.particle.particle import Particle


class TestActivity4(Activity):

    def get_path(self) -> str:
        # 文件路径
        return '.\\output\\4.par'

    def get_position(self) -> Vec3:
        # 可以将粒子偏移到实际要生成粒子的位置
        return Vec3(-6, -56, 0)

    def create_particle(self) -> None:
        self.add_particle(
            Particle.create(particle_type.end_rod, 40)
            # 对粒子进行操作，一般在有带有随机性的操作和让粒子运动的时候使用apply()
            .apply(motion.butterfly(200))
            # 让蝴蝶倾斜45度（绕z轴旋转45度），这里的角度使用弧度制，45度角用0.25pi表示
            .rotate_static(Vec3(0, 0, math.pi / 4))
            # 用多个当前粒子组成一个新粒子
            # .shape(shape.line(-50, 0, 0, 50, 0, 0, 10))
            # 改变颜色
            .color_static(0xFF00FF)
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
