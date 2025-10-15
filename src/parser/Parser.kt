package parser

import scanner.Token
import scanner.TokenType
// ps. ga base lng ko sng algo sa LEC4 slide ni ma'am ara

class Parser(val tokens: TokenStream<Token>) {

    private fun expr(){
        equality()
    }
    private fun equality(){
        comparison()
        while (tokens.nextToken?.type == TokenType.EQUAL_EQUAL ||
            tokens.nextToken?.type == TokenType.BANG_EQUAL){
            tokens.advance()
            comparison()
        }
    }
    private fun comparison(){
        term()
        while (tokens.nextToken?.type == TokenType.GREATER ||
            tokens.nextToken?.type == TokenType.GREATER_EQUAL ||
            tokens.nextToken?.type == TokenType.LESS ||
            tokens.nextToken?.type == TokenType.LESS_EQUAL) {
            tokens.advance()
            term()
        }
    }
    private fun term(){
        factor()
        while (tokens.nextToken?.type == TokenType.PLUS ||
            tokens.nextToken?.type == TokenType.MINUS) {
            tokens.advance()
            factor()
        }
    }
    private fun factor(){
        exponent()
        while (tokens.nextToken?.type == TokenType.STAR ||
            tokens.nextToken?.type == TokenType.SLASH) {
            tokens.advance()
            exponent()
        }
    }
    private fun exponent(){
        unary()
        if (tokens.nextToken?.type == TokenType.CARET) {        // CARET IS YUNG ^
            tokens.advance()
            exponent()                                          // recursive call here sa exponent instead sa unary makes it right-associative
                                                                // e.g. 2 ^ 3 ^ 2 should start sa (3^2) then 2^(8)
        }
    }
    private fun unary(){
        if (tokens.nextToken?.type == TokenType.BANG ||
            tokens.nextToken?.type == TokenType.MINUS ||
            tokens.nextToken?.type == TokenType.PLUS) {
            tokens.advance()
            unary()
        } else {
            primary()
        }
    }
    private fun primary(){
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

