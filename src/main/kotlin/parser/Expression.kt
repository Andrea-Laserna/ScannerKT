// // defines the abstract syntax tree node types
// package parser
// import main.Bridge
// import scanner.Token

// sealed class Expression {
//     data class Binary(val left: Expression, val operation: Token, val right: Expression): Expression()
//     data class Unary(val op: Token, val right: Expression) : Expression()
//     data class Group(val expr: Expression) : Expression()
//     data class Identifier(val name: Token) : Expression()
//     data class Literal(val value: Any?) : Expression()
// }

// defines the abstract syntax tree node types
package parser
import main.Bridge
import scanner.Token
import interpreter.ExpressionVisitor // Import the new Visitor interface

sealed class Expression {
    // New abstract method for the Visitor Pattern
    abstract fun <R> accept(visitor: ExpressionVisitor<R>): R 

    data class Binary(val left: Expression, val operation: Token, val right: Expression): Expression() {
        override fun <R> accept(visitor: ExpressionVisitor<R>): R = visitor.visitBinaryExpression(this)
    }
    
    data class Unary(val op: Token, val right: Expression) : Expression() {
        override fun <R> accept(visitor: ExpressionVisitor<R>): R = visitor.visitUnaryExpression(this)
    }
    
    data class Group(val expr: Expression) : Expression() {
        override fun <R> accept(visitor: ExpressionVisitor<R>): R = visitor.visitGroupExpression(this)
    }
    
    data class Identifier(val name: Token) : Expression() {
        override fun <R> accept(visitor: ExpressionVisitor<R>): R = visitor.visitIdentifierExpression(this)
    }
    
    data class Literal(val value: Any?) : Expression() {
        override fun <R> accept(visitor: ExpressionVisitor<R>): R = visitor.visitLiteralExpression(this)
    }
    
    data class Assignment(val name: Token, val value: Expression) : Expression() {
        override fun <R> accept(visitor: ExpressionVisitor<R>): R = visitor.visitAssignmentExpression(this)
    }
}