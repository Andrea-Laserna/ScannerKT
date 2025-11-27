package interpreter

import parser.Expression

// R is the return type of the visit method (e.g., Any?)
// T is the argument type of the visit method (e.g., Environment, context object)
interface ExpressionVisitor<R> {
    fun visitBinaryExpression(expr: Expression.Binary): R
    fun visitGroupExpression(expr: Expression.Group): R
    fun visitLiteralExpression(expr: Expression.Literal): R
    fun visitUnaryExpression(expr: Expression.Unary): R
    fun visitIdentifierExpression(expr: Expression.Identifier): R
}