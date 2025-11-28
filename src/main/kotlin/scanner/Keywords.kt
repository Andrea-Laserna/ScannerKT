package scanner

import main.Bridge
import scanner.TokenType 

object Keywords{
    val keywords: Map<String, TokenType> = mapOf(
        "import" to TokenType.IMPORT,
        "when" to TokenType.WHEN,
        "loop" to TokenType.LOOP,
        "try" to TokenType.TRY,
        "catch" to TokenType.CATCH,
        "ask" to TokenType.ASK,
        "to" to TokenType.TO,
        "and" to TokenType.AND,
        "class" to TokenType.CLASS,
        "else" to TokenType.ELSE,
        "false" to TokenType.FALSE,
        "for" to TokenType.FOR,
        "fun" to TokenType.FUN,
        "if" to TokenType.IF,
        "or" to TokenType.OR,
        "print" to TokenType.PRINT,
        "return" to TokenType.RETURN,
        "super" to TokenType.SUPER,
        "true" to TokenType.TRUE,
        "var" to TokenType.VAR,
        "not" to TokenType.NOT,
        "string" to TokenType.STRING,
        "float" to TokenType.FLOAT,
        "int" to TokenType.INT,
        "bool" to TokenType.BOOL,
        "nil" to TokenType.NIL
    )
}