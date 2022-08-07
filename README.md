<img src="https://cerus.dev/img/edina_lang_logo.png" alt="Logo" align="right" width=140 height=140 />
<h1 align="left">edina 📚</h1>

Edina is a simple multi-paradigm programming language. It currently features a JVM compiler, a REPL and an ever expanding
standard library.

Edina is mostly a hobby project. Due to its stack-oriented design it's a little restrictive and hard to program in, but that's what makes it fun in my
opinion.

Paradigms: [imperative](https://en.wikipedia.org/wiki/Imperative_programming), [concatenative](https://en.wikipedia.org/wiki/Concatenative_programming_language), [stack-oriented](https://en.wikipedia.org/wiki/Stack-oriented_programming)

<p align="center">
  <img width="400" src="https://cerus.dev/img/hello_world_edina2.png?" alt="&quot;Hello World&quot; program" />
</p>

## Contents

1. [Try it out](#try-it-out)
2. [Specification](#specification)
3. [Running the JVM compiler](#running-the-jvm-compiler)
4. [Building](#building)
5. [Included scripts](#included-scripts)
6. [EdinaJ development](#edinaj-development)
7. [Credit](#credit)

## Try it out

To try out Edina follow these steps:
1. Clone the repository
2. Build the project (see [Building](#building))
3. Create `script.edina`
4. Copy the contents of `examples/hello_world.edina` into `script.edina`
5. Compile the script (`java -jar edinaj/target/edinaj.jar -P my.script -F script.edina -O script.jar`)
6. Run the compiled Jar (`java -jar script.jar`)

## Specification

Edina programs are usually called scripts, even though they are compiled. A script contains many commands. Edina is not whitespace sensitive, and you
do not have to terminate your statements. The only place where a terminator command is needed is in routine, loop or if blocks.

Edina features a small set of commands which, when combined, can be used to create powerful and complex programs.

```r
pop         # Drop the topmost element from the stack     [a] -> []
dup         # Duplicate the topmost item of the stack     [a] -> [a, a]
swap        # Swap the two topmost items of the stack     [a, b] -> [b, a]
over        # Duplicate the 2nd topmost item to the top   [a, b] -> [b, a, b]
lroll       # Roll X items N times to the left            [X(3), N(1), a, b, c] -> [b, c, a]
rroll       # Roll X items N times to the right           [X(3), N(1), a, b, c] -> [c, a, b]
end         # End the code block / Only used in routines, ifs and loops
+           # Pop top two items and push sum              [a, b] -> [b+a]
-           # Pop top two items and push difference       [a, b] -> [b-a]
+           # Pop top two items and push product          [a, b] -> [b*a]
/           # Pop top two items and push quotient         [a, b] -> [b/a]
```

In addition to these commands, any signed integer (limited to 64 bits) and string (enclosed by two quotation marks) will act as a push command and
will be pushed to the stack.

```r
123         # [] -> [123]
-123        # [] -> [-123]
"abc"       # Len + chars as bytes / [] -> [3, 99, 98, 97]
            # Strings are naturally reversed due to the stack
```

Edina also supports routines (aka functions, methods, sub-routines, ...), a few branching commands, loops and importing other scripts.

```r
# ------ Imports ------
# Import statements have to be placed at the beginning of the script
import "stdlib/io/std"
# You can assign custom names to imported scripts
import "stdlib/math/ints" as custom_import_name
# You can call the declared global routines of the imported scripts
"Hi" :std.println_out               # -> import "stdlib/io/std"
123 :custom_import_name.int_to_str  # -> import "stdlib/math/ints" as custom_import_name

# ------ Routines ------
# Declaring a routine called my_routine
# Routines whose name starts with an underscore are internal and can not be called by
# other scripts
rt my_routine
  # Insert routine code here
end
.my_routine  # Calling my_routine

# ------ Branching ------
# if (peek_top_stack_item() != 0)
ifn
  # Code
end
# if (peek_top_stack_item() == 0)
ifz
  # Code
end
# if (peek_top_stack_item() <= 0)
iflt
  # Code
end
# if (peek_top_stack_item() >= 0)
ifgt
  # Code
end
# Chained ifs
ifn
  # Code
else ifz
  # Else if code
else
  # Else code
end

# ------ Loops ------
# while (peek_top_stack_item() != 0)
while
  # Code
end
# while (peek_top_stack_item() == 0)
until
  # Code
end
```

In order to interact with the host system, Edina features a few so-called "native calls". These native calls are heavily inspired by Linux syscalls.

```r
native_stack_debug      # Print the stack contents to stdout    [] -> []
native_open             # Open a file                           [FLAGS, PATH] -> [FD]
native_write            # Write data to file descriptor         [FD, DATA] -> [RES]
native_read             # Read data from file descriptor        [FD, AMT] -> [RES]
native_close            # Close an opened file descriptor       [FD] -> [RES]
native_time             # Get current time in seconds           [] -> [TIME]
```

## Running the JVM compiler

Please refer to the [EdinaJ README](edinaj/README.md) to see the possible command line arguments.

EdinaJ requires at least Java 17. The generated Jars require at least Java 8.

## Building

To build Edina you will need Java 17, Git and Maven.

1. `git clone https://github.com/cerus/edina.git`
2. `cd edina`
3. `mvn clean package` or [`./scripts/full_build.sh`](#included-scripts)

Final EdinaJ Jar: `edinaj/target/edinaj.jar`\
Final CLI / REPL Jar: `cli/target/edina-cli.jar`\
Final EdDoc Jar: `eddoc/target/eddoc.jar`

## Included scripts

This repository contains a few scripts in the `scripts/` directory that make Edina development easier.

`generate_asm.sh`: Generate OW2 ASM for the EdinaJ Stack, Natives and Launcher classes\
`cleanup.sh`: Delete any `asm_*.txt` files (generated by generate_asm.sh)\
`asm_to_code.sh`: Integrate generated ASM files into the respective compiler steps\
`full_build.sh`: `generate_asm.sh && asm_to_code.sh && cleanup.sh`

## EdinaJ development

Developing the JVM compiler is pretty straight forward. The `asm` package contains "templates" for the Launcher, Stack and Natives classes. These
templates can be converted to OW2 ASM code and inserted into their respective compiler steps.

Most of the compilation process is split up into `CompilerStep`s. This is an attempt to keep the code clean and organized. Each command has their own
compiler step for example.

See the [compiler step package](edinaj/src/main/java/dev/cerus/edina/edinaj/compiler/step)
or [Compiler class](edinaj/src/main/java/dev/cerus/edina/edinaj/compiler/Compiler.java) for more details.

## Credit

Inspired by Forth & Porth by Daily Tsoding

Logo was generated by DALL·E mini / craiyon.com

'hello_world.edina' banner was created with carbon.now.sh

Dedicated to Edina Of Coboldcastle (*2008-†2020)