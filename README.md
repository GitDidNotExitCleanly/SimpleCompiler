## Compiler
This compiler translate a subset of C language (see "rules" directory for more details)
into Java bytecode, which then can be run on the JVM.
The output .class file is in the "out" directory

## Building project
In order to build the project you must have Ant installed. If you are using an IDE, then you can import the build file. Otherwise, you can build the project from the commandline by typing:
```
$ ant build
```
This command outputs your compiler in a directory called `bin` within the project structure. Thereafter, you can run your compiler from the commandline by typing:
```
$ java -cp bin Main
```
The parameter `cp` instructs the Java Runtime to include the local directory `bin` when it looks for class files.

You can clean the `bin` directory by typing:
```
$ ant clean
```
This command effectively deletes the `bin` directory.

* The test codes provided need IO class file for IO functions
* Therefore, copy the IO class file (in the lib/) to out/ directory
* when running the compiler
