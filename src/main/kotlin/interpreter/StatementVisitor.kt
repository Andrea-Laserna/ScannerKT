package interpreter

import parser.Statement

/**
 * Defines the interface for all statement visitors.
 * R is the return type of the visit methods (e.g., Unit for Interpreter, String for AstPrinter).
 */
interface StatementVisitor<R> {
    fun visitExpressionStatement(stmt: Statement.ExpressionStatement): R
    fun visitPrintStatement(stmt: Statement.Print): R
    fun visitVarStatement(stmt: Statement.Var): R
    fun visitBlockStatement(stmt: Statement.Block): R
    fun visitAskStatement(stmt: Statement.Ask): R
    fun visitReserveStatement(stmt: Statement.Reserve): R
    fun visitMoveStatement(stmt: Statement.Move): R
    fun visitWhenStatement(stmt: Statement.When): R
    fun visitLoopStatement(stmt: Statement.Loop): R
}