package parser

class AstPrinter {
    // New public function to start printing from a Statement node
    fun printStatement(statement: Statement) {
        when (statement) {
            is Statement.Print -> {
                print("(print ")
                printTree(statement.expression)
                print(")")
            }
            is Statement.ExpressionStatement -> {
                print("(expr-stmt ")
                printTree(statement.expression)
                print(")")
            }
            is Statement.Var -> {
                print("(var ${statement.name.lexeme} ")
                if (statement.initializer != null) {
                    printTree(statement.initializer)
                } else {
                    print("none")
                }
                print(")")
            }
            is Statement.Ask -> {
                print("(ask ${statement.name.lexeme} ")
                printTree(statement.prompt)
                print(")")
            }
            is Statement.Assignment -> {
                print("(= ${statement.name.lexeme} ")
                printTree(statement.value)
                print(")")
            }
            is Statement.Block -> {
                print("{") // Open the block
                // Recursively print all statements inside the block
                statement.statements.forEach { s ->
                    printStatement(s)
                }
                print("}") // Close the block
            }
        }
    }
    
    // Existing function to print Expression nodes (renamed/modified to be private/internal if possible)
    fun printTree(expression: Expression) {
        when (expression) {
            is Expression.Binary -> {
                print("(")
                print(expression.operation.lexeme + " ")
                printTree(expression.left)   // recurse
                print(" ")
                printTree(expression.right)  // recurse
                print(")")
            }

            is Expression.Unary -> {
                print("(")
                print(expression.op.lexeme + " ")
                printTree(expression.right)
                print(")")
            }

            is Expression.Group -> {
                print("(")
                printTree(expression.expr)
                print(")")
            }

            is Expression.Identifier -> {
                print(expression.name.lexeme)
            }
            
            is Expression.Literal -> {
                val value = expression.value
                // FIX: Check if the literal is a String and wrap it in quotes.
                if (value is String) {
                    print("\"$value\"")
                } else {
                    print(value?.toString() ?: "none")
                }
            }
        }
    }
}