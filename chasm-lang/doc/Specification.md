# Chasm Language Specification

## Introduction

`chasm-lang` is the file format used by Chasm to represent transformers.
Note that while `chasm-lang` is in principle a complete programming language,
it is specifically designed as a file format for transformers.
More details about transformers can be found in the Chasm RFC (which will be linked here once available).

## File Format

`chasm-lang` files are encoded using `utf-8`.
The default extension is `.chasm`, though it isn't enforced or assumed at any point.
The file must contain the tokens representing exactly one expression and no other characters.

## Tokens

A `chasm-lang` file is made up of a list of tokens.
This section describes all valid tokens.

### Whitespace

Allowed whitespaces are spaces (U+0020), horizontal tabs (U+0009), carriage returns (U+000D), and newlines (U+000A).
All those whitespaces are valid tokens, but are discarded during parsing.

### Identifiers

Identifiers are a sequence of characters that match the regex `[_a-zA-Z][_a-zA-Z0-9]*`.
All such identifiers are valid tokens.

### Separators and Operators

Many tokens aren't explicitely named but still valid.
This applies mostly to seperators such as the comma (U+002C) and operators such as the plus (U+002B).
This document will mark these tokens by enclosing them in quotation marks, e.g. `"+"`.
All such tokens mentioned in this document are valid tokens.

### Integer Literals

Integer literals represent integer constants.
They represent signed 64-bit constants, meaning they can range from `-2^63` to `2^63 - 1` inclusive.
They must be in decimal representation and match the regex `[+-]?[0-9]+`.
All such integer literals are valid tokens.

### Floating Point Literals

Floating point literals represent floating point constants.
They represent constants in the `IEEE 754 - 2019` binary64 format (doubles).
They must be in decimal representation and match the regex `[+-]?[0-9]+\.[0-9+](e[+-][0-9]+)?`.
All such floating point literals are valid tokens.

<details>
  <summary>Examples</summary>

  ```javascript
    1.0

    0.05432

    -2.3

    -3.5e+5

    7.2e-43
  ```
</details>

### String Literals

String literals represent a constant sequence of characters.
They must be a sequence of characters enclosed between quotation marks.
Any characters may be enclosed, except for qutotation marks and backslashes.
Quotation marks and backslashes must instead be escaped, that means prefixed with a backslash.
More specifically, string literals must match the regex `"([^"\\]|\\\\|\\")*"`.

<details>
  <summary>Examples</summary>

  ```javascript
    "Example"

    "Example String"

    "Example
     Multiline
     String"

    "Example escaped \" quotes"

    "Example escaped \\ backslash"
  ```
</details>

### Boolean Literals

Boolean literals represent boolean constants.
They can only have two possible values: `true` or `false`.
All such boolean literals are valid tokens.

<details>
  <summary>Examples</summary>

  ```javascript
    true

    false
  ```
</details>

### None Literals

None literals represent empty constants.
They are represented by the literal `none`.
All such none literals are valid tokens.

<details>
  <summary>Examples</summary>

  ```javascript
    none
  ```
</details>

## Expressions

Certain combinations of tokens form expressions.
A `chasm-lang` file consists of exactly one such expression.
This section describes all valid expressions.

### Literal Expression

A literal expression is of the form `<literal token>`.
It describes a literal constant with the value of the literal token.
It evaluates to the value descibed by the literal token.

### Unary Expression

A unary expression is of the form `<unary operator> <expression>`
The unary operator is a token which describes what kind of operation is performed on the expression.
The following table lists all defined unary operators:

| Token | Description
| ----- | -----------
| -     | Negates the given float or integer.
| !     | Inverts the given boolean.
| ~     | Inverts the bits of the given integer.

<details>
  <summary>Examples</summary>

  ```
    !true

    -(2 + 5)

    ~4
  ```
</details>

### Binary Expression

A binary expression is of the form `<expression> <binary operator> <expression>`.
The binary operator is a token which describes what kind of operation is performed on the expressions.
The following table lists all defined binary operators and describes their behaviour:

| Token   | Description
| ------- | -----------
| +       | Adds the right expression to the left expression and returns the result.
| -       | Subtracts the right expression from the left expression and returns the result.
| *       | Multiplies the left expression with the right expression and returns the result.
| /       | Divides the left expression by the right expression and returns the result.
| =       | Returns `true` if the left expression is equal to the right expression, otherwise returns `false`.
| !=      | Returns `true` if the left expression is not equal to the right expression, otherwise returns `false`.
| &#62;   | Returns `true` if the left expression is bigger than the right expression, otherwise returns `false`.
| &#60;   | Returns `true` if the left expression is smaller than the right expression, otherwise returns `false`.
| &#62;=  | Returns `true` if the left expression is bigger than or equal to the right expression, otherwise returns `false`.
| &#60;=  | Returns `true` if the left expression is smaller than or equal to the right expression, otherwise returns `false`.
| &&      | Returns `true` if both the left and right expression return true, otherwise returns `false`.
| &#124;&#124; | Returns `true` if either the left or the right expression returns true, otherwise returns `false`.

<details>
  <summary>Examples</summary>

  ```
    2 + 5

    7 == 4

    true && false
  ```
</details>

### Ternary Expression

A ternary expression is of the form `<expression> ? <expression> : <expression>`.
Contrary to unary and binary expressions, there is only one ternary operator `?:`.
The evaluation result of the ternary operator depends on the evaluation result of first expression:
If the result is `true` it evaluates to second expression, if it evaluates to `false` it returns the third expression.

<details>
  <summary>Examples</summary>

  ```
    2 + 5

    7 == 4

    true && false
  ```
</details>

### List Expression

A list expression is of the form `[ <expression> , <expression> , <expression> ]`.
It may contain an arbitrary number of expressions separated by a comma.
It evaluates to to an ordered list containing the evaluated expressions in the same order as specified.

<details>
  <summary>Examples</summary>

  ```
    []

    [1]

    [1, 2, 3]

    [
      [1, 2, 3],
      [true, false,],
    ]
  ```
</details>

### Map Expression
A map expression is of the form `{ <identifier> : <expression> , <identifier> : <expression> }`.
It may contain an arbitrary number of identifier-expression pairs, separated by commas.
It evaluates to to an unordered map containing the specified pairs with the identifiers as keys and the evaluated expressions as values.

<details>
  <summary>Examples</summary>

  ```
    {}

    { key: "value" }

    {
      name: "john",
      age: "73"
    }

    {
      name: "methodA",
      returns: "I",
      parameters: ["I", "Z"]
    }
  ```
</details>

### Index Expression

An index expression is of the form `<expression> . [ <expression> ]`.
The first expression must evaluate to a list or map and the second expression to an integer or string respectively.
It evaluates to the entry associated with the corresponding index or key.

Note: The list or map expression itself doesn't get fully evaluated to avoid infinite recursion.


  ```
    list.[7]

    map.["name"]

    list.[base + 8]

    {
      name: "john",
      age: "73"
    }.["name"]
  ```

### Member Expression

A member expression is of the form `<expression> . <identifier>`.
The first expression must evaluate to a map.
This is an alias for the index expression `<expression> . [ "<identifier" ]`.

### Lambda Expression

A lambda expression is of the form `<identifier> -> <expression>`.
It evaluates to a function with the identifier as the single argument.

### Call Expression

A call expression is of the form `<expression> ( <expression> )`.
The first expression must evaluate to a function.
It evaluates to the result of calling the function with the specified argument.

### Filter Expression

A filter expression is of the form `<expression> . < <expression> >`.
The first expression must evaluate to a list and the second expression to a function.
It evaluates to a list containing only those elements of the original list where the function evaluates to `true`
when passed the element as the argument.

### Reference Expression

A reference expression is of the form `<identifier>`.
When evaluating this expression, the following steps are taken in the order specified.

1.  If the expression is the element of a map and the map contains a key equal to the identifier,
    it resolves to the value corresponding to that key.
2.  If the expression is the body of a lambda and the argument of the lambda is equal to the identifier,
    it resolved to the function argument when the function is called.
3.  If the expression has a parent, attempt name resolution again, relative to that parent starting at step 1.
4.  If the expression is the root expression of the file and there is a global defined with an equal identifier,
    it is resolved to the value of that global.
5.  Otherwise, the expression evaluates to `none`.

### Group Expression

A group expression is of the form `( <expression> )`.
It evalutes to the expression.
This expression can be used to override precedence rules.