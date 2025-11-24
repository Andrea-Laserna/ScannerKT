package parser

import main.Bridge
import scanner.Token
import scanner.TokenType
import kotlin.math.exp

// ps. ga base lng ko sng algo sa LEC4 slide ni ma'am ara

class Parser(val tokens: TokenStream) {

    //entry point starting at highest level rule: equality
    fun expr(): Expression{
        return equality()
    }

    // ===== EQUALITY =====

    private fun equality(): Expression{
        // parse a comparison
        var expr = comparison()

        while (tokens.nextToken?.type == TokenType.EQUAL_EQUAL ||
            tokens.nextToken?.type == TokenType.BANG_EQUAL){
            // when == or != is seen, parse another comparison
            val operation = tokens.advance()!!
            val right = comparison()
            // write both sides in a Binary node
            expr = Expression.Binary(expr, operation, right)
        }
        return expr
    }

    // ===== COMPARISON =====

    private fun comparison(): Expression{
        // parse higher-precedence rule first - term is higher than comparison
        var expr = term()

        // next token is one of its operators
        while (tokens.nextToken?.type == TokenType.GREATER ||
            tokens.nextToken?.type == TokenType.GREATER_EQUAL ||
            tokens.nextToken?.type == TokenType.LESS ||
            tokens.nextToken?.type == TokenType.LESS_EQUAL) {
            val operation = tokens.advance()!!
            val right = term()
            // combine into binary expression
            expr = Expression.Binary(expr, operation, right)
        }
        return expr
    }

    // ===== TERM =====

    private fun term(): Expression{
        // parse factor - higher than term
        var expr = factor()

        while (tokens.nextToken?.type == TokenType.PLUS ||
            tokens.nextToken?.type == TokenType.MINUS) {
            val operation = tokens.advance()!!
            val right = factor()
            expr = Expression.Binary(expr, operation, right)
        }
        return expr
    }

    // ===== FACTOR =====

    private fun factor(): Expression{
        // parse exponent - higher than factor
        var expr = exponent()

        while (tokens.nextToken?.type == TokenType.STAR ||
            tokens.nextToken?.type == TokenType.SLASH) {
            val operation = tokens.advance()!!
            val right = exponent()
            expr = Expression.Binary(expr, operation, right)
        }
        return expr
    }

    // ===== EXPONENT =====

    private fun exponent(): Expression {
        val expr = unary()

        if (tokens.nextToken?.type == TokenType.CARET) {
            val operation = tokens.advance()!!
            val right = exponent()
            return Expression.Binary(expr, operation, right)
        }
        return expr
    }

    // ===== UNARY =====

    private fun unary(): Expression{
        // sign
        if (tokens.nextToken?.type == TokenType.BANG ||
            tokens.nextToken?.type == TokenType.MINUS ||
            tokens.nextToken?.type == TokenType.PLUS) {
            val operation = tokens.advance()!!
            val right = unary()
            return Expression.Unary(operation, right)
        }
        return primary()
    }

    // ===== PRIMARY =====

    private fun primary(): Expression{
        // handles leaf nodes - identifiers, literals, group expr
        return when (tokens.nextToken?.type) {

            // ===== Literals =====

            TokenType.INT -> {
                val token = tokens.advance()!!
                Expression.Literal(token.lexeme.toInt())
            }

            TokenType.FLOAT -> {
                val token = tokens.advance()!!
                Expression.Literal(token.lexeme.toDouble())
            }

            TokenType.BOOL -> {
                val token = tokens.advance()!!
                Expression.Literal(token.lexeme.toBoolean())
            }

            TokenType.STRING -> {
                val token = tokens.advance()!!
                Expression.Literal(token.literal)
            }

            // ===== IDENTIFIERS =====

            TokenType.IDENTIFIER -> {
                val token = tokens.advance()!!
                Expression.Identifier(token)
            }

            // ===== GROUP =====

            TokenType.LEFT_PAREN -> {
                tokens.advance()
                // start parsing at expr
                val expr = expr()
                if (tokens.nextToken?.type != TokenType.RIGHT_PAREN) {
                    Bridge.error(1, "expected ')' after expression")
                } else {
                    tokens.advance()
                }
                Expression.Group(expr)
            }

                else -> {
                    Bridge.error(1, "no primary expression found")                       //temporary lng to dito and line, not implemented properly yet
                    Expression.Literal(null)
            }
        }
    }



}

//every internal node is labelled with a nonterminal symbol ------- <assign> , <expr> , <id>
//every leaf is labelled with a terminal symbol  --------------  A, B, C, =, +, (, ), *

