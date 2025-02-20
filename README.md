# MyCompiler: Custom Lexer and Tokenizer

## Overview

This project implements a lexical analyzer (lexer) for a custom programming language. The lexer uses Non-Deterministic Finite Automata (NFA) and Deterministic Finite Automata (DFA) to recognize different token types, including integers, decimals, identifiers, booleans, operators, and delimiters.

## Features

- Converts NFAs to DFAs for efficient token recognition.
- Supports multiple token types with priority-based classification.
- Can process input code and classify tokens accordingly.

## Installation

### Prerequisites

- Java Development Kit (JDK 11 or later)
- Any Java IDE (e.g., IntelliJ IDEA, Eclipse, or VS Code)

### Steps to Run

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/your-repo.git
   cd your-repo
   ```
2. Compile and run the project:
   ```bash
   javac -d out src/**/*.java
   java -cp out main.Main
   ```

## Token Rules

The lexer follows the following rules to classify tokens:

### 1. Identifiers

- Must start with a lowercase letter (`a-z`).
- Can contain only lowercase letters.
- **Example:** `varname`, `hello`, `testvar`

### 2. Integers

- Consist of one or more digits (`0-9`).
- **Example:** `42`, `12345`

### 3. Decimals

- Consist of an integer part followed by a `.` and a fractional part (up to 5 digits).
- **Example:** `3.14`, `42.001`

### 4. Booleans

- Recognized keywords: `true` and `false`.
- **Example:** `true`, `false`

### 5. Operators

- Supported: `+`, `-`, `*`, `/`, `%`, `^`.
- **Example:** `a + b`, `x * y`

### 6. Assignment Operator

- Single equal sign (`=`) for assignment.
- **Example:** `x = 10;`

### 7. Delimiters

- Supported: `;`, `,`, `(`, `)`, `{`, `}`, `[`, `]`.
- **Example:** `if (true) {}`

## Example Code

```txt
x = 10;
y = 3.14159;
z = x + y;
if (true) { z = z * 2; }
```

The lexer will classify the tokens accordingly and output their types.

## License

This project is licensed under the MIT License. See the LICENSE file for details.


