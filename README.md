> NOTE: This project is still in its early development.
> There's guaranteed bugs and missing functionality.

# Chasm - Collision Handling ASM

## What is Chasm?
Chasm is a Java bytecode transformation tool.
In its base functionality, it's similar to [ASM](https://asm.ow2.io/).
However, ASM is intended for single programs transforming some bytecode,
whereas the goal of Chasm is to allow multiple users to transform the same code.
This is useful in modding Java based games, where multiple mods might want to change
the same code.

## Why use Chasm?
There are other options for transforming bytecode.
Two popular ones are [Mixins](https://github.com/SpongePowered/Mixin) and access-wideners.
While they both perform well in their respective tasks, they are both limiting:
Access wideners do one thing only and Mixin tries its best to be safe,
preventing many useful transformations like changing control flow.

Chasm aims to provide the full power of ASM without restrictions.
This means that using Chasm directly might be tricky, but the goal of Chasm is to
allow reimplementing Mixin and access wideners on top of Chasm.

This greatly simplifies the toolchain, since only one bytecode transformation
library needs to be applied.
Additionally, if Mixin and AW are insufficient, people can implement
another "frontend" for Chasm without requiring special toolchain support.
