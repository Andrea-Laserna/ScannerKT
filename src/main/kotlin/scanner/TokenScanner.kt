package scanner

import main.Bridge

data class TokenScanner(val source: String) {
    var current = 0 // to move until we find the end of the token
    var start = 0 // to know where the start of the token is
    var line = 1

    private val tokens = mutableListOf<Token>()

    fun scanTokens(): List<Token> {
        while(!isAtEnd()) {
            start = current
            scanToken()
        }

        // end the program
        tokens.add(Token(TokenType.EOF, "", null, line))
        return tokens
    }

    private fun isAtEnd(): Boolean {
        return current >= source.length
    }

    private fun nextIs(next: Char): Boolean{
        if (isAtEnd()) return false
        if (source[current] != next) return false
        current++ //na count na ang next
        return true
    }

    private fun string(){
        while(!isAtEnd() && showCurrent() != '"'){
            if (showCurrent() == '\n') line++
            advance()
        }

        if (isAtEnd()) {
            Bridge.error(line, "Unterminated string.")
            return
        }

        // closing "
        advance()

        val stringLiteral = source.substring(start + 1, current - 1) //value or start is index of ("). so remove
        val cleaned = stringLiteral.replace(("\r\n"), "")
        addToken(TokenType.STRING, cleaned)
    }

    private fun comment() {
        // Multi-line comment (##[ ... ##])
        if(nextIs('#')) {
            // Check for [ to confirm start of multi-line block
            if (nextIs('[')) {
                // Keep consuming until we find the closing ##]
                while (current + 2 < source.length && !(source[current] == '#' && source[current+1] == '#' && source[current+2] == ']')) {
                    if (showCurrent() == '\n') line++
                    advance()
                }

                if (current + 2 >= source.length) {
                    Bridge.error(line, "Unterminated multi-line comment. Expected '##]'.")
                    return
                }

                // Consume the closing ##]
                advance()
                advance()
                advance()
            } else {
                // Single line comment (## or #) is treated as a regular single line comment
                while(!isAtEnd() && showCurrent() != '\n'){
                    advance()
                }
            }

        }else{
            // Single line comment (#)
            while(!isAtEnd() && showCurrent() != '\n'){
                advance()
            }
        }

        // For tokenization purposes, we treat the entire comment as a single token.
        // However, comments are usually discarded immediately after scanning.
        val comment = source.substring(start, current) // Include delimiters for full representation
        addToken(TokenType.COMMENT, comment)
    }

    private  fun showCurrent(): Char {
        return if(isAtEnd())'\u0000' else source[current]
    }

    private fun showNext(): Char {
        return if(current + 1 >= source.length) '\u0000' else source[current + 1]
    }

    // barebones lng ni muna nang, sa next gru pwede pa malagyan ng look ahead
    private fun numeral(){
        //value or start is index of first digit
        while(showCurrent().isDigit()) advance()

        var isFloat = false

        if (showCurrent() == '.' && showNext().isDigit()){ // to allow only a decimal followed by a digit, 1.A di pwede
            isFloat = true
            advance()
            while(showCurrent().isDigit()) advance() // for digits after the dot.
        }

        val text = source.substring(start, current)
        val value:Number = if(isFloat) text.toDouble() else text.toInt()

        if (isFloat){
            addToken(TokenType.FLOAT, value)
        } else {
            addToken(TokenType.INT, value)
        }
    }

    private fun identifier(){
        while(showCurrent().isLetter() || showCurrent().isDigit() || showCurrent() == '_') advance()
        val text = source.substring(start, current)

        val type = if (Keywords.keywords.containsKey(text)) {
            Keywords.keywords[text] as TokenType
        } else {
            TokenType.IDENTIFIER
        }

        val literal = when (type) {
            TokenType.TRUE -> true
            TokenType.FALSE -> false
            else -> null
        }

        addToken(type, literal)
    }

    // adds token types to a token list
    private fun scanToken() {

        when (val c: Char = advance()) {
            //Single-character tokens
            '(' -> addToken(TokenType.LEFT_PAREN, null)
            ')' -> addToken(TokenType.RIGHT_PAREN, null)
            '{' -> addToken(TokenType.LEFT_BRACE, null)
            '}' -> addToken(TokenType.RIGHT_BRACE, null)
            ',' -> addToken(TokenType.COMMA, null)
            '.' -> addToken(TokenType.DOT, null)
            '-' -> addToken(if(nextIs('>')) TokenType.ARROW else TokenType.MINUS, null)
            '+' -> addToken(TokenType.PLUS, null)
            ';' -> addToken(TokenType.SEMICOLON, null)
            '^' -> addToken(TokenType.CARET, null) // Using ^ for Exponentiation
            '#' -> comment()
            ':' -> addToken(TokenType.COLON, null)

            // either division or comment or multi-line comment close
            '/' -> addToken(TokenType.SLASH, null)

            // Multiplication (*)
            '*' -> addToken(TokenType.STAR, null)

            // insert operators
            '!' -> addToken(if(nextIs('=')) TokenType.BANG_EQUAL else TokenType.BANG, null)
            '=' -> addToken(if(nextIs('=')) TokenType.EQUAL_EQUAL else TokenType.EQUAL, null)
            '<' -> addToken(if(nextIs('=')) TokenType.LESS_EQUAL else TokenType.LESS, null)
            '>' -> addToken(if(nextIs('=')) TokenType.GREATER_EQUAL else TokenType.GREATER, null)
            
            // insert longer lexemes: division, new lines, white space
            ' ', '\r', '\t' -> { /* padayun lang */ }
            '\n' -> line++
 
            // insert string literals
            '"' -> string()

            else -> {
                when {
                    c.isDigit() -> numeral()            // insert number literals
                    c.isLetter() || c == '_' -> identifier()        // insert reserved words and identifiers
                    else -> Bridge.error(line, "Unexpected character: $c")
                }
            }

        }

    }

    // get the current character to scan
    private fun advance(): Char {
        return source[current++]
    }

    private fun addToken(type: TokenType, literal: Any? = null) {
        // computing for the lexeme
        val text: String = source.substring(start, current)
        tokens.add(Token(type, text, literal, line))
    }
}