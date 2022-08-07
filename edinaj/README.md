# edina/edinaj

EdinaJ is a JVM compiler for Edina. The compiler is not trying to optimize any code for now, but that's one of the future goals for this project.

## Usage

```
java -jar edinaj.jar [options]

  Options:
  * --sourcefile, -F
      The source file that should be compiled
  * --outputfile, -O
      The name of the final Jar
    --package, -P
      The package name that should be used
      Default: dev.cerus.edinalang.compiledscript
    --include, -I
      Directories that will be used for imports
      Default: []
    --debug, -D
      Enables debug printing in the final Jar
      Default: false
    --quiet, -Q
      Suppresses all stdout output if enabled
      Default: false
    --run, -R
      Runs the Jar after compilation
      Default: false
      
Example: 'java -jar edinaj/target/edinaj.jar -P dev.cerus.script -F script.edina -O script.jar -D'
```

## Used libraries

| Library                                            | License                                                                                       |
|----------------------------------------------------|-----------------------------------------------------------------------------------------------|
| [ASM](https://gitlab.ow2.org/asm/asm)              | [3-Clause BSD](https://gitlab.ow2.org/asm/asm/-/raw/master/LICENSE.txt)                       |
| [JCommander](https://github.com/cbeust/jcommander) | [Apache License 2.0](https://raw.githubusercontent.com/cbeust/jcommander/master/license.txt)  |