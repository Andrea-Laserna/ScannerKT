import java.util.Scanner

class TokenScanner (val source: String) {
    // to store Token objects
    val tokens: MutableList<Token> = mutableListOf()
    var start = 0
    var current  = 0
    var line = 1

    // list of tokens to generate
    fun scanTokens(): List<Token> {
        while (!isAtEnd()) {
            start = current
            scanToken()
        }

        tokens.add(Token(EOF, "", null, line))
        return tokens
    }

    // tells if all characters have been scanned
    private fun isAtEnd(): Boolean {
        return current >= source.length()
    }

    private fun scanToken() {
        val c = advance()
        when (c) {
            // Single Character
            '(' -> addToken(LEFT_PAREN)
            ')' -> addToken(RIGHT_PAREN)
            '{' -> addToken(LEFT_BRACE)
            '}' -> addToken(RIGHT_BRACE)
            ',' -> addToken(COMMA)
            '.' -> addToken(DOT)
            '-' -> addToken(MINUS)
            '+' -> addToken(PLUS)
            ';' -> addToken(SEMICOLON)
            '*' -> addToken(STAR)
            // Operators
            '!' -> addToken(if (match('=')) BANG_EQUAL else BANG)
            '=' -> addToken(if (match('=')) EQUAL_EQUAL else EQUAL)
            '<' -> addToken(if (match('=')) LESS_EQUAL else LESS)
            '>' -> addToken(if (match('=')) GREATER_EQUAL else GREATER)
            // Longer Lexemes
            '/' -> if (match('/')) {
                while (peek() != '\n' && !isAtEnd()) advance();
            } else {
                addToken(SLASH)
            }
            // Error
            else -> throw IllegalArgumentException("Unexpected character: $c at line $line")
        }
    }

    // return next character from source string
    private fun advance(): Char {
        return source[current++]    
    }

    private fun addToken(type: TokenType) {
        addToken(type, null)
    }

    private fun addToken(TokenType type, Object literal) {
        val text = source.substring(start, current)
        tokens.add(new Token(type, text, literal, line))
    }
    
    private fun match(expected: Char): Boolean {
        if (isAtEnd()) return false
        if (source[current] != expected) return false

        current++
        return true
    }
    
    private fun peek(): Char {
        if (isAtEnd()) return '\u0000'
        return source[current]
    }
}

fun main() {
    val sc = Scanner(System.`in`)
    println("Enter your code: ")

    val userInput = sc.nextLine()

    // Perform Scanning on userInput
}
