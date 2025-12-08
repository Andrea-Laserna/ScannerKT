High Level Assembly for Learners
BRIDGE LANGUAGE MANUAL — Version 1.0
The Official Programming Language Specification for Bridge

Table of Contents

Abstract

Introduction


Language Overview


Lexical Structure


Variables & Typing


Expressions & Operators


Input & Output


Conditional Statements (WHEN, ->, DEFAULT)


Loops (loop)


Functions


Error Handling (try, catch)


Standard Library


Full Example Programs


Appendix A — Grammar Specification


Appendix B — Reserved Keywords



1. Introduction
Bridge is a high-level, assembly-inspired language for learners and systems-minded developers. It emphasizes explicit data flow and predictable control constructs while keeping syntax approachable.

What it's about:
- Assembly flavor: operations are explicit (e.g., `MOVE`, `ADD`, `CMP`), control is driven by predicates, and loops use a tail predicate like a do–while.
- Clarity-first: one unified loop (`LOOP`), one conditional form (`WHEN` + `DEFAULT`), and explicit mutation (`INC`/`DEC`, BYREF) keep programs readable and debuggable.
- Practical effects: functions can be pure or impure; BYREF enables in-place updates where performance or simplicity matters.

Purpose:
- Teach core computing ideas (data movement, comparison, branching) without low-level ceremony.
- Provide a bridge between imperative/assembly thinking and modern structured programming.

Strengths:
- Predictable control flow via `WHEN` predicates and `LOOP` tail conditions.
- Simple, expressive function model with `FUNC`/`RET`/`CALL` and `BYREF`.
- Assembly-style string ops (LEN/CHAR/SUBSTR/SETCHAR) for hands-on manipulation.
- Tagged-result error handling integrates naturally with `WHEN`.

Weak points:
- Minimal type system (INT/STR/CMP) by design; advanced types are out of scope.
- No implicit exceptions; error handling prefers explicit results.
- Arrays/maps are conceptual; rich collections depend on runtime support.

Use cases:
- Teaching and labs focused on control flow, functions, and data movement.
- Algorithm practice (searching, numeric sequences, parsing) with transparent states.
- Scripting small CLI tasks where predictability and simplicity matter.

Design highlights:
- `WHEN` with `DEFAULT` as the single conditional construct.
- `LOOP { ... } tail-predicate` unifies while/for/do–while.
- Functions: `FUNC`, `RET`, `CALL`, with `BYREF` for in-place updates.
- Explicit ops: `ADD/SUB/MUL/DIV/MOD/EXP`, `INC/DEC`, `CMP` with predicates `EQUAL/LESS/GREATER/...`.

Bridge uses curly braces { } for all blocks.
Abstract
Bridge is a concise, assembly-inspired language that centers on explicit data movement (`MOVE`), predicate-driven control (`WHEN`, `CMP`), and a unified looping model (`LOOP` with tail predicates). It balances readability with low-level clarity: arithmetic and string operations are explicit; functions support pass-by-value and `BYREF` for in-place mutation; error handling favors tagged results that compose with `WHEN`. The language targets learning environments and small utilities where predictable semantics, minimal surface area, and approachable syntax are paramount.

2. Language Overview
Bridge code example (from `examples/bridge_fibonacci.txt` style):
FUNC fib (INT n) {
    WHEN LESS CMP n, 2 {
        EQUAL   -> RET n
        DEFAULT -> RET ADD fib(SUB n, 1), fib(SUB n, 2)
    }
}

RES_INT i
MOVE i, 0
RES_INT end
MOVE end, 8
LOOP {
    WHEN LESS CMP i, end {
        PRINT fib(i)
        INC i, 1
    }
} LESS CMP i, end


3. Lexical Structure
3.1 Comments
# Single line comment

##[
Multiline Comment
]##
3.2 Identifiers
Letters, numbers, underscores, but cannot start with a digit:
x
value_1
myVariable
3.3 Whitespace
Whitespace is ignored except inside strings.
3.4 Blocks
All code blocks use braces:
{ ... }

4. Variables & Typing
Bridge supports implicit and explicit typing.
4.1 Implicit
MOVE x, 10
MOVE name, "Bridge"
4.2 Explicit (Optional)
RES_INT a
MOVE a, 10
RES_STR msg
MOVE msg, "Hello"

5. Expressions & Operators
5.1 Arithmetic
ADD, SUB, MUL, DIV, MOD, EXP
5.2 Comparison
==, !=, <, <=, >, >=
EQUAL, NOTEQUAL, LESS, GREATER, LESSEQ, GREATEREQ

MOVE c, CMP.EQUAL a, b
5.3 Logical
&&, ||, !

5.4 Inline Conditions (->)
condition -> expression

6. Input & Output
6.1 Input
RES_INT guess
MOVE guess, ASK "Guess:"
6.2 Output
PRINT "Hello"

String Concatenation
# Build: greeting + ", " + name + "!"
MOVE msg, CONCAT greeting, CONCAT ", ", CONCAT name, "!"
PRINT msg

# If your PRINT supports expressions directly, you can also do:
# PRINT CONCAT greeting, CONCAT ", ", CONCAT name, "!"

String Operations (assembly-style)
- LEN s: length of string `s`
- CHAR s, i: character at index `i`
- SUBSTR s, start, end: substring from `start` (inclusive) to `end` (exclusive)
- SETCHAR s, i, ch: new string with character at `i` replaced by `ch`

Example: turn "string" into "skring"
RES_STR s
MOVE s, "string"
RES_INT n
MOVE n, LEN s
RES_INT i
MOVE i, 0
LOOP {
    WHEN LESS CMP i, n {
        RES_STR ch
        MOVE ch, CHAR s, i
        WHEN EQUAL CMP i, 0 {
            EQUAL   -> MOVE s, SETCHAR s, 0, "s"
            DEFAULT -> MOVE s, SETCHAR s, i, ch
        }
        INC i, 1
    }
} LESS CMP i, n
PRINT s

7. Conditional Statements
Bridge uses WHEN blocks with optional DEFAULT.
WHEN selector {
    predicate -> action
    ...
    DEFAULT -> action
}
Example:
MOVE c, CMP x, 10
WHEN c {
    EQUAL   -> PRINT "Ten"
    LESS    -> PRINT "Small"
    DEFAULT -> PRINT "Large"
}

8. Loops (loop:)
Bridge has one unified loop construct that becomes a:
while loop


for loop


do-while loop


depending on the format.

8.1 While Loop
Additional Control Flow and Functions (Lox-style subset for lab):

- statement: exprStmt | printStmt | varDecl | block | ifStmt | whileStmt | forStmt | returnStmt | funDecl | whenStmt | loopStmt | moveStmt | reserveDecl
- ifStmt: `if ( expression ) statement ( else statement )?`
- whileStmt: `while ( expression ) statement`
- forStmt: `for ( ( varDecl | exprStmt | ';' ) expression? ';' expression? ) statement` (desugars to while)
- returnStmt: `return expression? ';'`
- funDecl: `fun IDENTIFIER ( parameters? ) block`
- parameters: `IDENTIFIER ( ',' IDENTIFIER )*`
- call: `primary ( '(' arguments? ')' )*`
- arguments: `expression ( ',' expression )*`
- logicOr: `logicAnd ( 'or' logicAnd )*` (also supports `||`)
- logicAnd: `equality ( 'and' equality )*` (also supports `&&`)

Examples available in `examples/` directory: `bridge_fibonacci.txt`, `bridge_closures.txt`, `string_loop.txt`.


LOOP (condition) {
    	body
}
Example

RES_INT secret
MOVE secret, RAND 1 to 100

# Declare CompareResult outside for use in the do-while tail
RES_CMP c

LOOP NOTEQUAL c {
    RES_INT guess
    MOVE guess, ASK "Guess:"

    # Compute CompareResult once per iteration
    MOVE c, CMP guess, secret

    WHEN c {
        EQUAL   -> PRINT "Correct!"
        LESS    -> { PRINT "Too low!" }
        GREATER -> { PRINT "Too high!" }
    }
} 



MOVE c, CMP guess, secret
LOOP NOTEQUAL c {
    MOVE guess, ASK "Guess:"
}

8.2 For Loop
LOOP (condition) {
	cmp condition result

exit condition

  	body

	increment
}
Example
loop:
(i = 0, i < 10, i = i + 1) {
    print i
}

LOOP CMP i, 10 {
	cmp condition result

# exit condition
CMP.GREATER -> 

  	body

	increment
}

; Condition Check (i < 10)
   cmp ecx, ebx
   jge loop_exit   ; If ecx >= ebx, jump to loop_exit

   ; Loop Body (code to be executed)
   ; ... perform operations ...

   ; Increment
   inc ecx         ; Increment loop counter (i++)

   ; Unconditional Jump
   jmp loop_start



# Simulating a for-loop using do-while + predicates
# Target: for (i = 0; i < 10; i++) { body }

RES_INT start
RES_INT end
RES_INT step
RES_INT i

MOVE start, 0
MOVE end,   10
MOVE step,  1
MOVE i,     start

LOOP {
    # Guard the body so the first iteration is skipped if i >= end
    WHEN LESS CMP i, end {
        # ---- body ----
        PRINT "i:"
        PRINT i

        # increment
        INC i, step
    }
} LESS CMP i, end


# Variant: for (i = 0; i <= 10; i++)
# Change both the guard and tail predicate to LEQ (less-or-equal).
RES_INT i2
MOVE i2, 0
LOOP {
    WHEN LEQ CMP i2, 10 {
        PRINT "i2:"
        PRINT i2
        INC i2, 1
    }
} LEQ CMP i2, 10


# Variant: descending loop: for (i = 10; i > 0; i--)
RES_INT j
MOVE j, 10
LOOP {
    WHEN GREATER CMP j, 0 {
        PRINT "j:"
        PRINT j
        DEC j, 1
    }
} GREATER CMP j, 0


# Notes:
# - CMP is pure; predicates (LESS, LEQ, GREATER, NOTEQUAL, etc.) test its CompareResult.
# - The WHEN guard prevents executing the body when the initial condition is already false,
#   which matches for-loop semantics despite the do-while structure.
# - Tail predicate repeats while the loop condition holds.
# - ADD/SUB are expression functions that return the computed value; MOVE assigns it.




8.3 Do–While Loop
LOOP {
    body
} condition
Example
RES_INT secret
MOVE secret, RAND 1 to 100

# Declare CompareResult outside for use in the do-while tail
RES_CMP c

LOOP {
    RES_INT guess
    MOVE guess, ASK "Guess:"

    # Compute CompareResult once per iteration
    MOVE c, CMP guess, secret

    WHEN c {
        EQUAL   -> PRINT "Correct!"
        LESS    -> { PRINT "Too low!" }
        GREATER -> { PRINT "Too high!" }
    }
} NOTEQUAL c


9. Functions
readme
# Pass-by-Value and Pass-by-Reference Only

Design goals:
- Keep it simple: only functions (no procedures).
- Two passing mechanisms:
  - Pass-by-value (default): the callee receives a copy; mutations do not affect the caller.
  - Pass-by-reference (BYREF): the callee receives a reference to the caller’s storage; mutations in the callee are observed by the caller.
- Functions can return values (including tuples) and may also mutate BYREF parameters.

Syntax overview:
- Declaration: `FUNC name (params...) RET Type { ... }`
- Parameter: `[BYREF] Type ident` (BYVAL is the default if `BYREF` is omitted)
- Call (expression): `MOVE target, name(args...)`
- Return: `RETURN expr`
- BYREF arguments at call sites must be variables (addressable targets), not expressions.

Guidelines:
- Use pass-by-value for ordinary inputs and pure computations.
- Use BYREF when you need to:
  - Avoid copying large data structures (arrays, strings, maps).
  - Perform in-place updates (e.g., `swap`, `sort`, `push`).
  - Return multiple outputs via mutation of provided targets (optional, but allowed).
- Functions are allowed to be impure in this model (they can mutate BYREF arguments and perform effects such as printing). If you want purity, document it per function.

Examples:
- `FUNC add (INT a, INT b) RET INT` — pass-by-value, pure.
- `FUNC swap (BYREF INT a, BYREF INT b) RET VOID` — in-place swap via BYREF.
- `FUNC split2 (STR s) RET (STR head, STR tail)` — tuple return, avoids BYREF outputs.
- `FUNC push (BYREF ARR xs, INT v) RET VOID` — in-place append via BYREF.

Error handling:
- Return status codes (e.g., `RET INT` with 0/1) or tagged results (tuple `(BOOL ok, T value)`).
- Alternatively, mutate a BYREF error parameter (simple, but couples call site to storage).

EBNF sketch:
```
func_decl   := "FUNC" ident "(" param_list? ")" "RET" type "{" statements "}"
param_list  := param ("," param)*
param       := pass? type ident
pass        := "BYREF"                 # default is BYVAL if omitted
type        := "INT" | "STR" | "CMP" | "ARR" type | "MAP" "<"type","type">" | "(" type ("," type)+ ")"
return_stmt := "RETURN" expr
expr_call   := ident "(" arg_list? ")"
arg_list    := expr ("," expr)*
```

Notes:
- BYREF parameters are lvalues in the callee; you can `MOVE`, `INC`, `DEC` them.
- Tuple returns enable multiple outputs without BYREF targets.
- Predicates (EQUAL, LESS, GREATER, NOTEQUAL) operate on `CMP` values as before.



Sample

# Functions-only model with pass-by-value (default) and pass-by-reference (BYREF)

# 1) Pass-by-value, pure computation
FUNC add (INT a, INT b) {
    RET ADD a, b
}

RES_INT x
RES_INT y
RES_INT s
MOVE x, 2
MOVE y, 3
MOVE s, add(x, y)
PRINT s  # 5

# 2) Compare (three-way), pass-by-value inputs, returns a CMP result
FUNC compare (INT lhs, INT rhs) {
    RET CMP lhs, rhs
}

RES_CMP c
MOVE c, compare(s, 5)
WHEN c {
    LESS    -> PRINT "sum < 5"
    EQUAL   -> PRINT "sum == 5"
    GREATER -> PRINT "sum > 5"
}

# 3) In-place swap via BYREF
FUNC swap (BYREF INT a, BYREF INT b) {
    RES_INT tmp
    MOVE tmp, a
    MOVE a, b
    MOVE b, tmp
    RET
}

RES_INT a
RES_INT b
MOVE a, 10
MOVE b, 20
MOVE s, add(a, b)
PRINT s          # 30
MOVE s, add(a, b)
CALL swap(a, b)
PRINT a          # 20
PRINT b          # 10

# 4) In-place increment via BYREF (uses INC)
FUNC inc_inplace (BYREF INT x) {
    INC x, 1
    RET
}

RES_INT i
MOVE i, 41
CALL inc_inplace(i)
PRINT i          # 42

# 5) Append into an array via BYREF
# Assume ARR INT represents a resizable array of ints.
FUNC push (BYREF ARR INT xs, INT v) {
    # Implementation detail depends on runtime; conceptually:
    # xs.append(v)
    # For assembly flavor, imagine storing at xs[LEN xs] and bumping length.
    RET
}

# 6) Multiple outputs via tuple return (no BYREF outputs)
FUNC split2 (STR s) {
    RES_INT pos
    MOVE pos, FIND s, " "
    WHEN CMP pos, -1 {
        EQUAL    -> RET (s, "")
        NOTEQUAL -> RET (SUBSTR s, 0, pos, SUBSTR s, ADD pos, 1, LEN s)
    }
}

RES_STR h
RES_STR t
MOVE (h, t), split2("foo bar")
PRINT h          # "foo"
PRINT t          # "bar"

# 7) For-loop using compare and INC (functions-only worldview)
RES_INT end
MOVE i,   0
MOVE end, 5

LOOP {
    WHEN LESS CMP i, end {
        # body
        RES_INT r
        MOVE r, add(i, 10)
        PRINT r

        # increment via BYREF function
        CALL inc_inplace(i)
    }
} LESS CMP i, end

# Notes:
# - BYREF arguments at call sites must be variables, not expressions.
# - Functions can mutate BYREF parameters and may perform other effects (printing), since we removed procedures.
# - Prefer pass-by-value and tuple returns for pure computations; use BYREF for in-place updates or performance.




add(a, b) -> {
    return a + b
}


Recursive:
factorial(n) -> {
    when: {
        n <= 1 -> return 1
        else   -> return n * factorial(n - 1)
    }
}

10. Error Handling
Bridge uses simplified try -> { } and catch blocks.

Readme

# Error Handling in WhenLang

Goal: Keep the language assembly-flavored and simple. Provide a minimal, predictable error model that works with predicates and `WHEN`.

Recommended baseline (no mandatory try/catch):
- Pure functions return values; when an operation can fail, return a tagged result (status + payload).
- Use `WHEN` to branch on status, just like `CMP` results.
- BYREF functions can signal errors via returned status without exceptions.

Optional: Add `TRY/CATCH/FINALLY` later for ergonomic handling around multiple operations, but keep the core usable without it.

## Option A: Tagged Result (Recommended)

Introduce a `RES` type family (Result) with status variants:
- `OK` — success
- `ERR` — failure with an error code/value

Syntax:
- A function returning a result: `RET RES<INT>` or `RET RES<(STR head, STR tail)>`
- Constructors: `OK value`, `ERR code`
- Predicates: `OK r`, `ERR r` for branching
- Accessors: `VALUE r` (only valid when `OK r`), `ERROR r` (valid when `ERR r`)

Example:
```
FUNC parse_int (STR s) RET RES<INT> {
    RES_INT n
    # Imagine PARSE returns NOTEQUAL on failure; otherwise moves value to n
    RES_CMP c
    MOVE c, PARSE s, n
    WHEN c {
        EQUAL    -> RET OK n
        NOTEQUAL -> RET ERR "invalid_int"
    }
}
```

Usage:
```
RES res  # RES<INT>, but you can allow implicit generic RES if your type system is simple
MOVE res, parse_int("123")

WHEN res {
    OK  -> {
        RES_INT x
        MOVE x, VALUE res
        PRINT x
    }
    ERR -> {
        RES_STR code
        MOVE code, ERROR res
        PRINT "parse failed:"
        PRINT code
    }
}
```

## Option B: Status Codes Only (Simplest)

Use an `INT` or `CMP`-style status:
- Return `INT` status (0 = OK, nonzero = error code) and pass outputs via BYREF.
- Or return `CMP`-like enum: `SUCCESS`, `FAIL`.

Example:
```
FUNC read_file (STR path, BYREF STR out) RET INT {
    # 0 on success, non-zero error codes
    # On success, MOVE out, contents
    RET 0
}

RES_INT status
RES_STR text
MOVE status, read_file("note.txt", text)
WHEN CMP status, 0 {
    EQUAL    -> PRINT text
    NOTEQUAL -> PRINT "error reading file"
}
```

## Optional: TRY/CATCH/FINALLY (Ergonomic)

If you want structured error handling without status plumbing, add:
```
TRY {
    # statements (calls that may fail)
}
CATCH (ERR e) {
    # handle error
}
FINALLY {
    # always run
}
```

Semantics:
- Within `TRY`, any statement that raises an `ERR` short-circuits to `CATCH`.
- `ERR` can carry a code/message. Inside `CATCH`, `e` is bound.
- `FINALLY` always executes (optional).
- Pure functions should not raise errors; they return tagged results.
- BYREF mutating functions may raise errors; but prefer Option A for consistency.

## Guidance

- Start with Option A (Tagged Results). It integrates naturally with `WHEN` and your predicate style.
- Keep Option B available for very low-level routines or when performance dictates minimal overhead.
- Add `TRY/CATCH` later only if you find many call sites doing repetitive error branching; it’s syntactic sugar.

## EBNF Sketch

```
result_type    := "RES" "<" type ">"
result_value   := "OK" expr | "ERR" expr
pred_ok        := "OK" expr     # predicate over a result value
pred_err       := "ERR" expr
value_access   := "VALUE" expr  # extract value from OK result
error_access   := "ERROR" expr  # extract error code from ERR result

try_stmt       := "TRY" "{" statements "}" catch_part finally_part?
catch_part     := "CATCH" "(" "ERR" ident ")" "{" statements "}"
finally_part   := "FINALLY" "{" statements "}"
```

Design principles:
- No hidden global error flags; all data flow is explicit.
- `WHEN` remains the central control construct for branching.
- Predicates apply to `CMP` and to `RES` uniformly.


Standard Form
# Error handling examples

# Tagged Result: parse integer
FUNC parse_int (STR s) RET RES<INT> {
    RES_INT n
    RES_CMP c
    MOVE c, PARSE s, n          # PARSE sets n on success and returns EQUAL; otherwise NOTEQUAL
    WHEN c {
        EQUAL    -> RETURN OK n
        NOTEQUAL -> RETURN ERR "invalid_int"
    }
}

# Using parse_int with WHEN predicates over results
RES res
MOVE res, parse_int("123")
WHEN res {
    OK  -> {
        RES_INT x
        MOVE x, VALUE res
        print "parsed:"
        print x
    }
    ERR -> {
        RES_STR code
        MOVE code, ERROR res
        print "parse failed:"
        print code
    }
}

# Status-only function (low level)
FUNC read_file (STR path, BYREF STR out) RET INT {
    # 0 on success, non-zero error
    # Suppose we fail here:
    RETURN 2
}

RES_INT status
RES_STR text
MOVE status, read_file("note.txt", text)
WHEN CMP status, 0 {
    EQUAL    -> { print "contents:"; print text }
    NOTEQUAL -> { print "read_file error"; print status }
}

# Optional TRY/CATCH usage (if enabled in the language)
TRY {
    # A function that may raise ERR instead of returning RES
    # For demo, we reuse parse_int by unwrapping its result and raising ERR on failure:
    RES res2
    MOVE res2, parse_int("abc")
    WHEN res2 {
        OK  -> {
            RES_INT v
            MOVE v, VALUE res2
            PRINT v
        }
        ERR -> {
            # raise error to outer TRY/CATCH
            RAISE res2
        }
    }
}
CATCH (ERR e) {
    RES_STR code
    MOVE code, ERROR e
    PRINT "Caught error:"
    PRINT code
}
FINALLY {
    PRINT "cleanup complete"
}

14. Appendix B — Reserved Keywords
WHEN, DEFAULT, LOOP, FUNC, RET, CALL, MOVE, PRINT, ASK,
RES_INT, RES_STR, RES_CMP,
ADD, SUB, MUL, DIV, MOD, EXP, INC, DEC,
CMP, EQUAL, NOTEQUAL, LESS, LESSEQ, GREATER, GREATEREQ,

LEN, CHAR, SUBSTR, SETCHAR


**Logical Operators**

Bridge supports both symbolic and word forms for logical operators:

- Symbolic: `&&`, `||`, `!`
- Word forms: `and`, `or`, `not` (word forms map to same semantics)

Precedence and behavior:

- Highest: `!` / `not` (negation)
- Next: `&&` / `and`
- Lowest: `||` / `or`
- Evaluation is left-to-right and short-circuiting:
    - For `A && B`, `B` is evaluated only if `A` is true.
    - For `A || B`, `B` is evaluated only if `A` is false.

Simple examples:

```bridge
# Symbolic forms
MOVE a, 5
MOVE b, 10
# true && false -> false
MOVE t, ( a < b ) && ( b < 5 )

# Word forms (equivalent)
MOVE u, ( a < b ) and ( b < 5 )

# Negation
MOVE v, ! ( a == b )

# Short-circuit example: second operand not evaluated when first decides result
# (illustrative; side-effect functions omitted)
```

Combining with predicates and tagged results:

```bridge
# Predicates (CMP) and RES predicates can be combined with logical operators
MOVE cmpRes, CMP a, b
WHEN ( LESS cmpRes ) && ( someFlag ) {
        PRINT "less and flag true"
}

MOVE res, parse_int("123")
WHEN OK res || ( someFallbackCondition ) {
        # If OK res is true, fallback is not evaluated
}
```

Guidelines:

- Use word forms (`and`/`or`/`not`) when readability is preferred; symbolic forms are concise.
- Prefer short-circuiting to avoid unnecessary side effects in right-hand operands.
- Combine predicates (CMP/RES) with boolean conditions by testing the predicate (e.g., `LESS cmpRes`) or using helper conversions as needed.


