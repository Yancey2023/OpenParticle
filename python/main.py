from example.TestActivity1 import TestActivity1
from example.TestActivity2 import TestActivity2
from example.TestActivity3 import TestActivity3
from example.TestActivity4 import TestActivity4
from example.TestActivity5 import TestActivity5
from example.TestActivity6 import TestActivity6
from openparticle.api.activity.activity import ActivityManager

if __name__ == '__main__':
    manager = ActivityManager()
    manager.add(TestActivity1())
    manager.add(TestActivity2())
    manager.add(TestActivity3())
    manager.add(TestActivity4())
    manager.add(TestActivity5())
    manager.add(TestActivity6())
    manager.run()
