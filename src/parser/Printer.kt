package parser
import main.Bridge

class AstPrinter {
    // take an expression node and return its string represention
    fun print(expr: Expression): String {
        return when (expr) {
            // subclass of expression we are dealing with
            is Expression.Binary -> parenthesize(expr.operation?.lexeme ?: "?", expr.left, expr.right) // parenthesis around, get operator symbol..uses ? if it's null
            is Expression.Unary -> parenthesize(expr.op.lexeme, expr.right)                            // parenthesis around, get operator, get right expression
            is Expression.Group -> parenthesize("group", expr.expr)                                    // adds group before the expression
            is Expression.Identifier -> expr.name.lexeme                                                // variable name
            is Expression.Literal -> expr.value.toString()                                              // print actual value in string                                  
        }
    }

    // helper function: wraps operator (name) and operands in parentheses
    // pass any number of expressions
    private fun parenthesize(name: String, vararg exprs: Expression): String {
        // build string piece by piece
        val builder = StringBuilder() 
        builder.append("(").append(name)
        // for each child expression, add a space and call print recursively
        for (expr in exprs) {
            builder.append(" ").append(print(expr))
        }
        builder.append(")")
        return builder.toString()
    }
}
