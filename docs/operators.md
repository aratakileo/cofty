# Operator Precedence and Classification

### Overview

This document describes the operator precedence rules and classification for the language. Operators are categorized into three types based on their usage context and operand requirements.

### Operator Categories

#### 1. Unary Operators

Operators that take a single operand and appear before or after it.

| Operator | Precedence | Associativity | Description | Notes           |
|----------|------------|---------------|-------------|-----------------|
| ~        | 13         | Right         | Bitwise NOT | Prefix operator |
| +        | 13         | Right         | Unary plus  | Prefix operator |
| \-       | 13         | Right         | Unary minus | Prefix operator |
| not      | 3          | Right         | Logical NOT | Prefix operator |

#### 2. Binary Operators

Operators that take two operands and appear between them.

| Operator   | Precedence | Associativity | Description              | Type Requirements               |
|------------|------------|---------------|--------------------------|---------------------------------|
| \*\*       | 14         | Right         | Exponentiation           | Numeric types only              |
| \*         | 12         | Left          | Multiplication           | Numeric types                   |
| /          | 12         | Left          | Division                 | Numeric types                   |
| %          | 12         | Left          | Modulo                   | Numeric types                   |
| +          | 11         | Left          | Addition                 | Numeric or string concatenation |
| \-         | 11         | Left          | Subtraction              | Numeric types                   |
| <<         | 10         | Left          | Left shift               | Integer types                   |
| \>>        | 10         | Left          | Right shift              | Integer types                   |
| &          | 9          | Left          | Bitwise AND              | Integer types                   |
| ^          | 8          | Left          | Bitwise XOR              | Integer types                   |
| \|         | 7          | Left          | Bitwise OR               | Integer types                   |
| in         | 6          | Left          | Membership test          | Right operand must be iterable  |
| not in     | 6          | Left          | Negative membership test | Right operand must be iterable  |
| is         | 6          | Left          | Identity test            | Any types                       |
| is not     | 6          | Left          | Negative identity test   | Any types                       |
| isinstance | 6          | Left          | Type checking            | Right operand must be a type    |
| <          | 5          | Left          | Less than                | Comparable types                |
| <=         | 5          | Left          | Less than or equal       | Comparable types                |
| \>         | 5          | Left          | Greater than             | Comparable types                |
| \>=        | 5          | Left          | Greater than or equal    | Comparable types                |
| \==        | 4          | Left          | Equality                 | Any types                       |
| !=         | 4          | Left          | Inequality               | Any types                       |
| and        | 2          | Left          | Logical AND              | Boolean context                 |
| or         | 1          | Left          | Logical OR               | Boolean context                 |

#### 3. Context-Sensitive Operators

Operators that can function as both unary and binary depending on context.

| Operator | Unary Context                        | Binary Context                      |
|----------|--------------------------------------|-------------------------------------|
| +        | Precedence 13  <br>Right-associative | Precedence 11  <br>Left-associative |
| \-       | Precedence 13  <br>Right-associative | Precedence 11  <br>Left-associative |

### Precedence Hierarchy

The following table shows the complete operator precedence from highest to lowest:

| Level | Operators                                | Category | Associativity |
|-------|------------------------------------------|----------|---------------|
| 14    | `**`                                     | Binary   | Right         |
| 13    | `~` `+` `-`                              | Unary    | Right         |
| 12    | `*` `/` `%`                              | Binary   | Left          |
| 11    | `+` `-`                                  | Binary   | Left          |
| 10    | `<<` `>>`                                | Binary   | Left          |
| 9     | `&`                                      | Binary   | Left          |
| 8     | `^`                                      | Binary   | Left          |
| 7     | `\|`                                     | Binary   | Left          |
| 6     | `in not` `in` `is` `is not` `isinstance` | Binary   | Left          |
| 5     | `<` `<=` `>` `>=`                        | Binary   | Left          |
| 4     | `==` `!=`                                | Binary   | Left          |
| 3     | `not`                                    | Unary    | Right         |
| 2     | `and`                                    | Binary   | Left          |
| 1     | `or`                                     | Binary   | Left          |

### Key Design Decisions

**Precedence Rules**
- Higher precedence level indicates tighter binding
- Unary operators have higher precedence than most binary operators
- Logical operators follow mathematical convention with NOT having the highest precedence
- Comparison operators have equal precedence and are non-associative

**Associativity Rules**
- Most binary operators are left-associative
- Exponentiation (`**`) and unary operators are right-associative
- Comparison operators are non-associative to prevent ambiguous expressions

**Context-Sensitive Operators**
- The parser must distinguish between unary and binary usage of `+` and `-`
- Context is determined by position in expression and surrounding tokens
- Unary variants have higher precedence than binary variants

**Compound Operators**
- Multi-word operators (`is not`, `not in`) are treated as single lexical units
- These operators have equal precedence within their category
- The lexer must recognize these as compound tokens