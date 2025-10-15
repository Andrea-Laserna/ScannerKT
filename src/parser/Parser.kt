package parser

import scanner.Token
import scanner.TokenType
import kotlin.math.exp

// ps. ga base lng ko sng algo sa LEC4 slide ni ma'am ara

class Parser(val tokens: TokenStream<Token>) {

    //entry
    private fun expr(): Expression{
        return equality()
    }
    private fun equality(): Expression{
        var expr = comparison()
        while (tokens.nextToken?.type == TokenType.EQUAL_EQUAL ||
            tokens.nextToken?.type == TokenType.BANG_EQUAL){
            val operation = tokens.current
            tokens.advance()
            val right = comparison()
            expr = Expression.Binary(expr, operation, right)
        }
        return expr
    }

    private fun comparison(): Expression{
        var expr = term()
        while (tokens.nextToken?.type == TokenType.GREATER ||
            tokens.nextToken?.type == TokenType.GREATER_EQUAL ||
            tokens.nextToken?.type == TokenType.LESS ||
            tokens.nextToken?.type == TokenType.LESS_EQUAL) {
            tokens.advance()
            val operation = tokens.current
            val right = term()
            expr = Expression.Binary(expr, operation, right)
        }
        return expr
    }
    private fun term(): Expression{
        factor()
        while (tokens.nextToken?.type == TokenType.PLUS ||
            tokens.nextToken?.type == TokenType.MINUS) {
            tokens.advance()
            factor()
        }
    }
    private fun factor(): Expression{
        exponent()
        while (tokens.nextToken?.type == TokenType.STAR ||
            tokens.nextToken?.type == TokenType.SLASH) {
            tokens.advance()
            exponent()
        }
    }
    private fun exponent(): Expression{
        unary()
        if (tokens.nextToken?.type == TokenType.CARET) {        // CARET IS YUNG ^
            tokens.advance()
            exponent()                                          // recursive call here sa exponent instead sa unary makes it right-associative
                                                                // e.g. 2 ^ 3 ^ 2 should start sa (3^2) then 2^(8)
        }
    }
    private fun unary(): Expression{
        if (tokens.nextToken?.type == TokenType.BANG ||
            tokens.nextToken?.type == TokenType.MINUS ||
            tokens.nextToken?.type == TokenType.PLUS) {
            tokens.advance()
            unary()
        } else {
            primary()
        }
    }
    private fun primary(): Expression{
        when (tokens.nextToken?.type) {
            TokenType.IDENTIFIER,
            TokenType.INT,
            TokenType.FLOAT,
            TokenType.BOOL -> {
                tokens.advance()
                //build here sa tree
            }

            TokenType.LEFT_PAREN -> {
                tokens.advance()
                expr()
                if (tokens.current?.type == TokenType.RIGHT_PAREN){
                    tokens.advance()
                }
                else{
                    Bridge.error(1, "no closing parenthesis")
                }
            }

            else -> {
                Bridge.error(1, "no primary")                       //temporary lng to dito and line, not implemented properly yet
            }
        }
    }



}

//every internal node is labelled with a nonterminal symbol ------- <assign> , <expr> , <id>
//every leaf is labelled with a terminal symbol  --------------  A, B, C, =, +, (, ), *

