# Part IV : Code Generation

The goal of part IV is to write the code generator.
In order to simplify the development of the code generator, we are going to use version 4.2 of the ASM Java ByteCode manipulation library.
We will restrict our-self to the use of the Core API and it is **forbidden** to use the Tree API (do not use any of the classes in the org.objectweb.asm.tree.* packages).