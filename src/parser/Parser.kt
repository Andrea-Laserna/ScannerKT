package parser

import main.Bridge
import scanner.Token
import scanner.TokenType
import kotlin.math.exp

// ps. ga base lng ko sng algo sa LEC4 slide ni ma'am ara

class Parser(val tokens: TokenStream<Token>) {

    //entry point starting at highest level rule: equality
    fun expr(): Expression{
        return equality()
    }
    // equality
    private fun equality(): Expression{
        // parse a comparison
        var expr = comparison()
        while (tokens.nextToken?.type == TokenType.EQUAL_EQUAL ||
            tokens.nextToken?.type == TokenType.BANG_EQUAL){
            // when == or != is seen, parse another comparison
            val operation = tokens.current
            tokens.advance()
            val right = comparison()
            // write both sides in a Binary node
            expr = Expression.Binary(expr, operation!!, right)
        }
        return expr
    }

// --- FUNCTIONS ---
    private fun comparison(): Expression{
        // parse higher-precedence rule first - term is higher than comparison
        var expr = term()
        // next token is one of its operators
        while (tokens.nextToken?.type == TokenType.GREATER ||
            tokens.nextToken?.type == TokenType.GREATER_EQUAL ||
            tokens.nextToken?.type == TokenType.LESS ||
            tokens.nextToken?.type == TokenType.LESS_EQUAL) {
            tokens.advance()
            val operation = tokens.current
            val right = term()
            // combine into binary expression
            expr = Expression.Binary(expr, operation!!, right)
        }
        return expr
    }
    private fun term(): Expression{
        // parse factor - higher than term
        var expr = factor()
        while (tokens.nextToken?.type == TokenType.PLUS ||
            tokens.nextToken?.type == TokenType.MINUS) {
            tokens.advance()
            val operation = tokens.current
            val right = factor()
            expr = Expression.Binary(expr, operation!!, right)
        }
        return expr
    }

    private fun factor(): Expression{
        // parse exponent - higher than factor
        var expr = exponent()
        while (tokens.nextToken?.type == TokenType.STAR ||
            tokens.nextToken?.type == TokenType.SLASH) {
            tokens.advance()
            val operation = tokens.current
            val right = exponent()
            expr = Expression.Binary(expr, operation!!, right)
        }
        return expr
    }
    private fun exponent(): Expression {
        val expr = unary()
        if (tokens.nextToken?.type == TokenType.CARET) {
            tokens.advance()
            val operation = tokens.current!! // store ^ token
            val right = exponent()           // recursive call
            return Expression.Binary(expr, operation, right)
        }
        return expr
    }


    private fun unary(): Expression{
        // sign
        if (tokens.nextToken?.type == TokenType.BANG ||
            tokens.nextToken?.type == TokenType.MINUS ||
            tokens.nextToken?.type == TokenType.PLUS) {
            tokens.advance()
            val operation = tokens.current!!
            val right = unary()
            return Expression.Unary(operation, right)
        } else {
            // parse primary - higher than unary
            return primary()
        }
    }
    private fun primary(): Expression{
        // handles leaf nodes - identifiers, literals, group expr
        return when (tokens.nextToken?.type) {
            TokenType.IDENTIFIER,
            TokenType.INT,
            TokenType.FLOAT,
            TokenType.BOOL -> {
                tokens.advance()
                //build here sa tree
                Expression.Identifier(tokens.current!!)
            }

            TokenType.LEFT_PAREN -> {
                tokens.advance()
                // start parsing at expr
                val expr = expr()
                if (tokens.nextToken?.type == TokenType.RIGHT_PAREN){
                    tokens.advance()
                    return Expression.Group(expr)
                }
                else{
                    Bridge.error(1, "no closing parenthesis")
                    return Expression.Group(expr)
                }
            }

            else -> {
                Bridge.error(1, "no primary")                       //temporary lng to dito and line, not implemented properly yet
                Expression.Identifier(scanner.Token(scanner.TokenType.IDENTIFIER, "error", null, 1)) // placeholder
            }
        }
    }



}

//every internal node is labelled with a nonterminal symbol ------- <assign> , <expr> , <id>
//every leaf is labelled with a terminal symbol  --------------  A, B, C, =, +, (, ), *

