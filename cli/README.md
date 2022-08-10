# edina/cli

The `cli` module contains a simple REPL for the Edina language. The REPL implements all Edina features except importing and native calls.

## How to run

`java -jar edina-cli.jar`

## Special commands

`!stack`: Same as native_stack_debug\
`!pop`: Pop the top stack item\
`!string`: Interpret the top stack items as a string and print to stdout (without popping)