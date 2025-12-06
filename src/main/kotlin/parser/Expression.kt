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

    // Concatenation of multiple expressions (for string building via CONCAT)
    data class Concat(val parts: List<Expression>) : Expression() {
        // Interpreter's ExpressionVisitor doesn't yet define a dedicated Concat handler.
        // To keep compilation working, route to a literal visit (no-op semantics for now).
            override fun <R> accept(visitor: ExpressionVisitor<R>): R = visitor.visitConcatExpression(this)
    }

    // Predicate call like: LESS <expr>, OK <res>, NOTEQUAL <expr>
    data class PredCall(val name: Token, val args: List<Expression>) : Expression() {
           override fun <R> accept(visitor: ExpressionVisitor<R>): R = visitor.visitPredCallExpression(this)
    }

    // Operation/intrinsic call: ADD_OP a, b | INC x, 1 | CMP a, b
    data class OpCall(val name: Token, val args: List<Expression>) : Expression() {
            override fun <R> accept(visitor: ExpressionVisitor<R>): R = visitor.visitOpCallExpression(this)
    }

    // Function call expression: callee(args)
    data class Call(val callee: Expression, val paren: Token, val arguments: List<Expression>) : Expression() {
        override fun <R> accept(visitor: ExpressionVisitor<R>): R = visitor.visitCallExpression(this)
    }
}