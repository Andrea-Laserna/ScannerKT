package interpreter

import main.Bridge
import parser.Expression
import scanner.Token
import scanner.TokenType
import kotlin.math.pow

// Custom exception for runtime errors (used to unwind the stack)
class RuntimeError(val token: Token, message: String) : RuntimeException(message)

class Interpreter : ExpressionVisitor<Any?> {

    // --- Core Interpreter Entry Point (used by Bridge.kt) ---
    fun interpret(expression: Expression) {
        try {
            val value = evaluate(expression)
            println(stringify(value))
        } catch (error: RuntimeError) {
            Bridge.runtimeError(error)
        }
    }

    private fun evaluate(expr: Expression): Any? {
        return expr.accept(this)
    }

    // --- Expression Visitor Implementations ---

    override fun visitLiteralExpression(expr: Expression.Literal): Any? {
        return expr.value
    }

    override fun visitGroupExpression(expr: Expression.Group): Any? {
        return evaluate(expr.expr)
    }

    override fun visitUnaryExpression(expr: Expression.Unary): Any? {
        val right = evaluate(expr.right)

        return when (expr.op.type) {
            TokenType.MINUS -> {
                checkNumberOperand(expr.op, right)
                -(right as Number).toDouble()
            }
            // Assume '!' is for logical NOT
            TokenType.BANG -> !isTruthy(right)
            
            // Assume '+' is identity operator for numbers
            TokenType.PLUS -> {
                checkNumberOperand(expr.op, right)
                right
            }
            else -> null // Should not happen
        }
    }

    override fun visitBinaryExpression(expr: Expression.Binary): Any? {
        val left = evaluate(expr.left)
        val right = evaluate(expr.right)

        return when (expr.operation.type) {
            // Equality
            TokenType.BANG_EQUAL -> {
                // Explicitly check for Number types and compare values
                if (left is Number && right is Number) {
                    left.toDouble() != right.toDouble()
                } else {
                    // Fallback for comparing other types (string, boolean, nil)
                    left != right
                }
            }
            TokenType.EQUAL_EQUAL -> {
                // Explicitly check for Number types and compare values
                if (left is Number && right is Number) {
                    left.toDouble() == right.toDouble()
                } else {
                    // Fallback for comparing other types (string, boolean, nil)
                    left == right
                }
            }

            // Comparison
            TokenType.GREATER -> {
                checkNumberOperands(expr.operation, left, right)
                (left as Number).toDouble() > (right as Number).toDouble()
            }
            TokenType.GREATER_EQUAL -> {
                checkNumberOperands(expr.operation, left, right)
                (left as Number).toDouble() >= (right as Number).toDouble()
            }
            TokenType.LESS -> {
                checkNumberOperands(expr.operation, left, right)
                (left as Number).toDouble() < (right as Number).toDouble()
            }
            TokenType.LESS_EQUAL -> {
                checkNumberOperands(expr.operation, left, right)
                (left as Number).toDouble() <= (right as Number).toDouble()
            }
            
            // Arithmetic
            TokenType.MINUS -> {
                checkNumberOperands(expr.operation, left, right)
                (left as Number).toDouble() - (right as Number).toDouble()
            }
            
            // '+' is for addition OR string concatenation
            TokenType.PLUS -> {
                if (left is String && right is String) {
                    left + right
                } else if (left is Number && right is Number) {
                    (left as Number).toDouble() + (right as Number).toDouble()
                } else {
                    throw RuntimeError(expr.operation, "Operands must be two numbers or two strings for '+'.")
                }
            }
            TokenType.SLASH -> {
                checkNumberOperands(expr.operation, left, right)
                if ((right as Number).toDouble() == 0.0) {
                    throw RuntimeError(expr.operation, "Division by zero.")
                }
                (left as Number).toDouble() / right.toDouble()
            }
            TokenType.STAR -> {
                checkNumberOperands(expr.operation, left, right)
                (left as Number).toDouble() * (right as Number).toDouble()
            }
            TokenType.CARET -> {
                checkNumberOperands(expr.operation, left, right)
                (left as Number).toDouble().pow((right as Number).toDouble())
            }

            else -> null // Should not happen
        }
    }

    override fun visitIdentifierExpression(expr: Expression.Identifier): Any? {
        // Since we haven't implemented Environment/variable storage yet, this should error.
        throw RuntimeError(expr.name, "Attempted to use an identifier '${expr.name.lexeme}' before variable storage is implemented.")
    }

    // --- Utility Methods ---
    
    private fun isTruthy(obj: Any?): Boolean {
        if (obj == null) return false
        if (obj is Boolean) return obj
        // Every other value (numbers, strings, etc.) is truthy
        return true 
    }

    private fun checkNumberOperand(operator: Token, operand: Any?) {
        if (operand is Number) return
        throw RuntimeError(operator, "Operand for '${operator.lexeme}' must be a number.")
    }

    private fun checkNumberOperands(operator: Token, left: Any?, right: Any?) {
        if (left is Number && right is Number) return
        throw RuntimeError(operator, "Operands for '${operator.lexeme}' must be numbers.")
    }

    private fun stringify(obj: Any?): String {
        if (obj == null) return "nil"
        
        // Handle Kotlin's number representation (e.g., 5.0 -> 5)
        if (obj is Double) {
            var text = obj.toString()
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length - 2)
            }
            return text
        }

        return obj.toString()
    }
}