import os.path
import time
from abc import ABCMeta, abstractmethod

from openparticle.api.math.vec3 import Vec3
from openparticle.api.data.data_particle import DataParticleManager
from openparticle.api.particle.particle import Particle


class Activity:
    __metaclass__ = ABCMeta

    def __init__(self):
        self.particle_list: list[Particle] = list()

    @abstractmethod
    def get_path(self) -> str:
        pass

    @abstractmethod
    def get_position(self) -> Vec3:
        pass

    @abstractmethod
    def create_particle(self) -> None:
        pass

    def add_particle(self, particle: Particle) -> None:
        self.particle_list.append(particle)

    def run(self) -> None:
        print(f'-----{self.__class__.__name__}-----')
        # 获取粒子数据
        time_start = time.time()
        self.create_particle()
        if len(self.particle_list) == 0:
            print('粒子为空，你是不是忘记调用addParticle()')
            return
        particle = Particle.compound(self.particle_list).offset_static(self.get_position())
        # 计算粒子数据
        data_particle = particle.run()
        manager = DataParticleManager(data_particle)
        # 写入粒子数据
        path = self.get_path()
        manager.output(path)
        time_end = time.time()
        # 在终端输出文本信息
        print(f'运行耗时: {time_end - time_start:.4f}s')
        print(f'粒子数量: {data_particle.get_particle_count()}')
        size = os.path.getsize(path)
        if size < 1024:
            size_str = f'{size:.0f}B'
        elif size < 1024 * 1024:
            size_str = f'{size / 1024:.0f}KB'
        elif size < 1024 * 1024 * 1024:
            size_str = f'{size / 1024 / 1024:.0f}MB'
        else:
            size_str = f'{size / 1024 / 1024 / 1024:.0f}GB'
        print(f'文件大小: {size_str}')


class ActivityManager:

    def __init__(self):
        self.activity_list: list[Activity] = list()

    def add(self, activity: Activity):
        self.activity_list.append(activity)

    def run(self):
        for activity in self.activity_list:
            activity.run()
