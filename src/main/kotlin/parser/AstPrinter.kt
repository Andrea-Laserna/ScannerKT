package parser

class AstPrinter {
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
                print(expression.value?.toString() ?: "none")
            }
        }
    }
}
