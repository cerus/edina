# edina/eddoc

EdDoc (**Ed**ina **Doc**umentation) is a simple (and messy) documentation generator. EdDoc scans your script(s) for annotated routines and generates
matching docs in Markdown.

## Example

Check out the stdlib docs in the wiki to see how EdDoc docs look like.

## EdDoc routine annotation

EdDoc uses the `eddoc` parameter in routine annotations to generate docs. Example:

```r
[ eddoc={
    # Describe the routine here
    desc="This is the place where you would put a description of what the routien does."
    
    # Describe how this routine modifies the stack here
    stack={
        in="Param 1 $int, Param 2 $int"
        out="Result $int"
    }
} ]
rt my_cool_routine
  over + *
end
```

### desc

This parameter should be used to describe what your routine does.

### stack

This parameter should be used to tell users how your routine modifies the stack. This is very important because users need to know which parameters
your routine needs and what result they can expect.

Each entry in the `in` and `out` parameter starts with its name and ends with a `$` + its type (eg `My param $int`). Multiple entries can be specified
by separating them with a comma.

**in**: The items this routine will take from the stack\
**out**: The items this routine will put onto the stack at the end

## Used libraries

| Library                                            | License                                                                                       |
|----------------------------------------------------|-----------------------------------------------------------------------------------------------|
| [JCommander](https://github.com/cbeust/jcommander) | [Apache License 2.0](https://raw.githubusercontent.com/cbeust/jcommander/master/license.txt)  |