package parser

import scanner.Token
import kotlin.collections.List

// NOTE: This class no longer uses the ExpressionVisitor or StatementVisitor interfaces,
// but rather uses direct recursion via printStatement/printTree.
class AstPrinter {

    // --- Public Entry Points ---

    // Prints a list of statements (for running a script)
    fun print(statements: List<Statement>) {
        statements.forEach { printStatement(it) }
    }

    // Prints a single expression (for REPL or expression debugging)
    fun print(expression: Expression) {
        printTree(expression)
        println()
    }

    // --- Statement Printing ---

    fun printStatement(statement: Statement) {
        when (statement) {
            is Statement.Print -> {
                print("(print ")
                printTree(statement.expression)
                print(");")
            }
            is Statement.ExpressionStatement -> {
                print("(expr-stmt ")
                printTree(statement.expression)
                print(");")
            }
            is Statement.Var -> {
                print("(var ${statement.name.lexeme} ")
                if (statement.initializer != null) {
                    printTree(statement.initializer)
                } else {
                    print("none") // For uninitialized variables (var x;)
                }
                print(");")
            }
            is Statement.Block -> {
                print("{\n")
                // Recursively print all statements inside the block
                statement.statements.forEach { s ->
                    printStatement(s)
                }
                print("}\n")
            }
            // FIXED: Correctly uses Statement.Ask and its properties
            is Statement.Ask -> { 
                print("(ask ${statement.name.lexeme} ")
                printTree(statement.prompt)
                print(");")
            }
        }
    }
    
    // --- Expression Printing ---
    
    fun printTree(expression: Expression) {
        when (expression) {
            is Expression.Assignment -> {
                // Assignment is right-associative, format as (= identifier value)
                parenthesize("=", Expression.Identifier(expression.name), expression.value)
            }
            is Expression.Binary -> {
                parenthesize(expression.operation.lexeme, expression.left, expression.right)
            }
            is Expression.Unary -> {
                parenthesize(expression.op.lexeme, expression.right)
            }
            is Expression.Group -> {
                parenthesize("group", expression.expr)
            }
            is Expression.Identifier -> {
                print(expression.name.lexeme)
            }
            is Expression.Literal -> {
                val value = expression.value
                // Ensure nil/null is printed correctly, otherwise print value
                print(value?.toString() ?: "nil")
            }
        }
    }

    // --- Utility Function ---

    // Helper function to format the tree nodes recursively
    private fun parenthesize(name: String, vararg expressions: Expression) {
        print("($name")
        for (expr in expressions) {
            print(" ")
            printTree(expr)
        }
        print(")")
    }
}