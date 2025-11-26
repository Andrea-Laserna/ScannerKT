package parser

import scanner.Token
// This will define the structure that we can execute

// Sealed class representing all possible types of statements in Bridge
sealed class Statement {
    // Statement to print a value: `print 1 + 1;`
    // Whatever follows the print keyword is parsed and stored as this expression
    data class Print(val expression: Expression) : Statement()

    // Statement that is just a side-effect expression (e.g., `1 + 1;`)
    // Allows a standalone expression followed by a ; to be a valid statement
    data class ExpressionStatement(val expression: Expression) : Statement()

    // Placeholder for variable declarations: `var x = 5;` or `int x = 5;`
    // TODO: Implement logic later
    data class Var(val name: Token, val initializer: Expression?) : Statement()

    // Placeholder for variable assignment: `x = 10;`
    // To change the value of an existing variable
    data class Assignment(val name: Token, val value: Expression) : Statement()

    // Placeholder for input: `guess = ask "Guess:";`
    data class Ask(val name: Token, val prompt: Expression) : Statement()

    // Placeholder for an entire block of code: `{ statement1; statement2; }`
    data class Block(val statements: List<Statement>) : Statement()
}