package parser

import scanner.Token
import interpreter.StatementVisitor 

// Sealed class representing all possible types of statements
sealed class Statement {
    // Abstract method for the StatementVisitor pattern
    abstract fun <R> accept(visitor: StatementVisitor<R>) : R

    // Statement to print a value: `print 1 + 1;`
    data class Print(val expression: Expression) : Statement() {
        override fun <R> accept(visitor: StatementVisitor<R>): R = visitor.visitPrintStatement(this)
    }

    // Statement that is just a side-effect expression (e.g., `x = 10;` or `1 + 1;`)
    data class ExpressionStatement(val expression: Expression) : Statement() {
        override fun <R> accept(visitor: StatementVisitor<R>): R = visitor.visitExpressionStatement(this)
    }

    // Variable declaration: `var x = 5;` or `var y;`
    data class Var(val name: Token, val initializer: Expression?) : Statement() {
        override fun <R> accept(visitor: StatementVisitor<R>): R = visitor.visitVarStatement(this)
    }

    // Reservation declarations: RES_INT x, RES_STR name, etc.
    data class Reserve(val typeToken: Token, val name: Token) : Statement() {
        override fun <R> accept(visitor: StatementVisitor<R>): R = visitor.visitReserveStatement(this)
    }

    // An entire block of code: `{ statement1; statement2; }`
    data class Block(val statements: List<Statement>) : Statement() {
        override fun <R> accept(visitor: StatementVisitor<R>): R = visitor.visitBlockStatement(this)
    }
    
    // NEW: Statement to prompt the user for input and store it in a variable: `ask x "What is your name?";`
    data class Ask(val name: Token, val prompt: Expression) : Statement() {
        override fun <R> accept(visitor: StatementVisitor<R>): R = visitor.visitAskStatement(this)
    }

    // WHEN selector { predicate -> action ... [ELSE -> action] }
    data class When(
        val selector: Expression,
        val branches: List<WhenBranch>,
        val elseBranch: List<Statement>?
    ) : Statement() {
        override fun <R> accept(visitor: StatementVisitor<R>): R = visitor.visitWhenStatement(this)
    }

    data class WhenBranch(val predicate: Token, val action: List<Statement>)

    // LOOP { body } tailPredicate?
    data class Loop(
        val body: List<Statement>,
        val tailPredicate: Expression?
    ) : Statement() {
        override fun <R> accept(visitor: StatementVisitor<R>): R = visitor.visitLoopStatement(this)
    }

    // MOVE target, expr
    data class Move(val target: Token, val value: Expression) : Statement() {
        override fun <R> accept(visitor: StatementVisitor<R>): R = visitor.visitMoveStatement(this)
    }

}

// Root of statements
sealed interface Program {
    // Represents a non-empty sequence of statements.
    data class Sequence(val head: Statement, val tail: Program) : Program
    // Represents the end of the program (base case for recursion)
    data object Empty : Program
}