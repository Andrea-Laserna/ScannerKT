package parser

import scanner.Token

sealed class Expression {
    data class Binary(val left: Expression, val operation: Token?, val right: Expression): Expression()
    data class Unary(val op: Token, val right: Expression) : Expression()
    data class Group(val expr: Expression) : Expression()
    data class Identifier(val name: Token) : Expression()
}