# README #

## What is this repository for? ##

* Quick summary

This is a Java version of the lox interpreter from the book [Crafting Interpreters](http://www.craftinginterpreters.com/introduction.html)

* [Learn Markdown](https://bitbucket.org/tutorials/markdowndemo)

## How do I get set up? ##

### Running a Lox program ###

Lox has 2 modes it can run in:

1. REPL mode: Run the main method in the LoxMain class with exactly 1 program argument, the path to the Lox script you want to run.

2. Script mode: Run the main method in the LoxMain class with no program arguments.


### Generating the AST ###

The Expression class and its subtypes are generated by the GenerateAbstractSyntaxTree class. 

GenerateAbstractSyntaxTree has a main method which accepts exactly 1 program argument, the output directory of the generated Expression class.

E.g.: /Users/geertguldentops/IdeaProjects/lox/src/main/java/be/guldentops/geert/lox/grammar

## Lox Grammar ##

    program         → declaration* EOF ;
    declaration     → varDecl
                    | statement ;
    varDecl         → "var" IDENTIFIER ( "=" expression )? ";" ;
    statement       → exprStmt
                    | forStmt
                    | ifStmt
                    | printStmt
                    | whileStmt
                    | block ;
    forStmt         → "for" "(" ( varDecl | exprStmt | ";" )
                                expression? ";"
                                expression? ")" statement ;
    ifStmt          → "if" "(" expression ")" statement ( "else" statement )? ;
    block           → "{" declaration* "}" ;
    exprStmt        → expression ";" ;
    printStmt       → "print" expression ";" ; 
    expression      → assignment ;
    assignment      → identifier "=" assignment
                    | logic_or ;
    logic_or        → logic_and ( "or" logic_and )* ;
    logic_and       → equality ( "and" equality )* ;
    equality        → comparison ( ( "!=" | "==" ) comparison )* ;
    comparison      → addition ( ( ">" | ">=" | "<" | "<=" ) addition )* ;
    addition        → multiplication ( ( "-" | "+" ) multiplication )* ;
    multiplication  → unary ( ( "/" | "*" ) unary )* ;
    unary           → ( "!" | "-" ) unary
                    | primary ;
    primary         → "true" | "false" | "nil" | "this"
                    | NUMBER | STRING
                    | "(" expression ")"
                    | IDENTIFIER ;
                
## Lox Precedence Rules ##

Name            |       Operators       | Associates
:--------------:|:---------------------:|:---------:
Unary           |   `!` `-`             | Right
Multiplication  |   `/` `*`             | Left
Addition        |   `-` `+`             | Left
Comparison      |   `>` `>=` `<` `<=`   | Left
Equality        |   `==` `!=`           | Left