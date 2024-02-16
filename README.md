# OpenParticle

A minecraft fabric mod to summon particle in minecraft.

The particle files loaded by this mod are stored in binary format, which can greatly accelerate the export and import
speed of particle files.

The powerful particle combination can store a large number of particles with minimal file size and memory usage.

Multithreading particle VBO filling can greatly improve your particle rendering speed.

## Command

load particle in file:

```mcfunction
par load path_to_your_file
```

run particle:

```mcfunction
par run
```

load particle in file and run it:

```mcfunction
par loadAndRun path_to_your_file
```

## How to create a particle file

[OpenParticle Java API](https://github.com/Yancey2023/OpenParticleAPI)

[OpenParticle Python API](https://github.com/Yancey2023/OpenParticleAPI-py)

if you want to use other languages, you can find the basic java api in this package: `yancey.openparticle.api.common`. You can implement the same logic in whatever language you like.

Have fun!!!