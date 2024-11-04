import os
import shutil

modId = 'openparticle'
modVersion = '2.10'
mcVersions = [
    "1.16-fabric",
    "1.16.1-fabric",
    "1.16.2-fabric",
    "1.16.3-fabric",
    "1.16.4-fabric",
    "1.16.5-fabric",
    "1.17-fabric",
    "1.17.1-fabric",
    "1.18-fabric",
    "1.18.1-fabric",
    "1.18.2-fabric",
    "1.19-fabric",
    "1.19.1-fabric",
    "1.19.2-fabric",
    "1.19.3-fabric",
    "1.19.4-fabric",
    "1.20-fabric",
    "1.20.1-fabric",
    "1.20.2-fabric",
    "1.20.3-fabric",
    "1.20.4-fabric",
    "1.20.5-fabric",
    "1.20.6-fabric",
    "1.21-fabric",
    "1.21.1-fabric"
    "1.21.2-fabric"
    "1.21.3-fabric"
]

release_dest = "./build/libs/release"
sources_dest = "./build/libs/sources"

if not os.path.exists(release_dest):
    os.makedirs(release_dest)
if not os.path.exists(sources_dest):
    os.makedirs(sources_dest)

for mcVersion in mcVersions:
    shutil.copyfile(
        f"./versions/{mcVersion}/build/libs/{modId}-{mcVersion}-{modVersion}.jar",
        f"./build/libs/release/{modId}-{mcVersion}-{modVersion}.jar"
    )
    shutil.copyfile(
         f"./versions/{mcVersion}/build/libs/{modId}-{mcVersion}-{modVersion}-sources.jar",
         f"./build/libs/sources/{modId}-{mcVersion}-{modVersion}-sources.jar"
    )
