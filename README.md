# OpenParticle

A minecraft fabric mod to summon particle in minecraft.

The particle files loaded by this mod are stored in binary format, which can greatly accelerate the export and import
speed of particle files.

The particle kernel is made by C++ and has ultimate performance.

## Command

load particle file:

```mcfunction
par load <path>
```

run particle:

```mcfunction
par run [isSingleThread]
```

load particle file and run it:

```mcfunction
par loadAndRun <isSingleThread> <path>
```

## params

`path` - path to your particle file

`isSingleThread` - if it prepares VBO data in single thread when rendering

## examples

```mcfunction
par load D:\PyCharm\project\OpenParticleAPI-py\output\1.par
par run
par run true
par run false
par loadAndRun false D:\PyCharm\project\OpenParticleAPI-py\output\1.par
par loadAndRun true D:\PyCharm\project\OpenParticleAPI-py\output\1.par
```

## How to create a particle file

[OpenParticle Java API](https://github.com/Yancey2023/OpenParticleAPI)

[OpenParticle Python API](https://github.com/Yancey2023/OpenParticleAPI-py)

Have fun!!!