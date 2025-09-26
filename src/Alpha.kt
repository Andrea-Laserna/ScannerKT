/* run via:
kotlinc Alpha.kt -include-runtime -d Alpha.jar
java -jar Alpha.jar
*/

import java.io.File
import java.io.InputStream
import kotlin.system.exitProcess

// fixed master list of language's alphabet for type safety
enum class TokenType {
    // Single-character tokens
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR, COMMENT,

    // One or two character tokens
    BANG, BANG_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,

    // Literals
    IDENTIFIER, STRING, NUMBER, // sa number nang isahon ta nalang ang mga int,double, float?

    // Keywords
    AND, CLASS, ELSE, FALSE, FUN, FOR, IF, NONE, OR,
    PRINT, RETURN, SUPER, THIS, TRUE, VAR, WHILE, NOT,

    EOF
}

class Token (val type: TokenType, val lexeme: String, val literal: Any?, val line: Int) {
    override public fun toString(): String { // print readable token instead of memory address
        return ("Type=${type}, Lexeme=${lexeme}, Literal=${literal}, Line=${line}")
    }
}

class TokenScanner(val source: String) {
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
            Alpha.error(line, "Unterminated string.")
            return
        }

        // closing "
        advance()

        val stringLiteral = source.substring(start + 1, current - 1) //value or start is index of ("). so remove
        val cleaned = stringLiteral.replace(("\r\n"), "")
        addToken(TokenType.STRING, cleaned)
    }

    private fun comment() {
        while(!isAtEnd() && showCurrent() != '\n'){
            advance()
        }
        val comment = source.substring(start+1, current) // start after #
        addToken(TokenType.COMMENT, comment)
    }

    private  fun showCurrent(): Char {
        return if(isAtEnd())'\u0000' else source[current]
    }

    private fun showNext(): Char {
        return if(current + 1 > source.length) '\u0000' else source[current + 1]
    }

    // barebones lng ni muna nang, sa next gru pwede pa malagyan ng look ahead
    private fun numeral(){
        //value or start is index of first digit
        while(showCurrent().isDigit()) advance()

        if (showCurrent() == '.' && showNext().isDigit()){ // to allow only a decimal followed by a digit, 1.A di pwede
            advance()
            while(showCurrent().isDigit()) advance() // for digits after the dot.
        }

        val value = source.substring(start, current).toDouble()
        addToken(TokenType.NUMBER, value)
    }

    private fun identifier(){
        while(showCurrent().isLetter() || showCurrent().isDigit() || showCurrent() == '_') advance()
        val text = source.substring(start, current)
        val type = keywords[text] ?: TokenType.IDENTIFIER
        addToken(type)
    }

    companion object {
        val keywords: Map<String, TokenType> = mapOf(
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
            "this" to TokenType.THIS,
            "true" to TokenType.TRUE,
            "var" to TokenType.VAR,
            "while" to TokenType.WHILE
        )
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
            '-' -> addToken(TokenType.MINUS, null)
            '+' -> addToken(TokenType.PLUS, null)
            ';' -> addToken(TokenType.SEMICOLON, null)
            '*' -> addToken(TokenType.STAR, null)
            '#' -> comment()

            //either division or comment
            '/' -> if(nextIs('/')) {
                while (showCurrent() != '\n' && !isAtEnd()) advance()
            }
            else if (nextIs('*')){
                while (!isAtEnd() && showCurrent() != '*' && showNext() != '/') advance()
                advance()
                advance()
            }
            else {
                addToken(TokenType.SLASH, null)
            }

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
                    else -> Alpha.error(line, "Unexpected character: $c")
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



// entry point
object Alpha {
    var errorExists: Boolean = false

    fun start(args: Array<String>) {
        // java -jar updated.jar one two three
        if (args.size > 1) {
            println("Usage: alpha [script]")
            exitProcess(64)
        // java -jar updated.jar code.txt
        } else if (args.size == 1) {
            runFile(args[0])
        // java -jar updated.jar
        } else {
            runPrompt()
        }
    }

    // read code from another file
    private fun runFile(path: String) {
        val inputStream: InputStream = File(path).inputStream() // open the file for reading: create a file object and open it as a stream of bytes
        val inputString = inputStream.reader().use {it.readText()} // reader() to wrap byte stream for reading, adding .use to close stream after use, it.readText() for read file as a single string
        run(inputString)
        // kill the file
        if (errorExists) exitProcess(65)
    }

    // read code from terminal
    private fun runPrompt() {
        while(true){
            print(">")
            // if enter, set line to the string. if ctrl D or C EOF, exit
            val line = readLine() ?: break
            run(line)
            // dont kill the REPL
            errorExists = false
        }
    }

    private fun run(source: String) {
        // build the scanner
        val scanner = TokenScanner(source)
        // scan for tokens in the string
        val tokens: List<Token> = scanner.scanTokens()
        // print tokens for now
        for (token in tokens) {
            println(token)
        }
    }

    // error handling
    // shortcut for most errors
    fun error (line: Int, message: String) {
        report(line, "", message)
    }

    // engine for errors
    private fun report (line: Int, where: String, message: String) {
        System.err.println("[line $line] Error at $where: $message")
        errorExists = true
    }
}

fun main(args: Array<String>) {
    Alpha.start(args)
}



//future TODOs
// kulang ng naghahandle ng \ and '
// ang ang lexeme ng EOF is ang lexeme ng prev toker.
// hindi nag eerror if walang closing parenthesis
// siguro yung rules para sa syntax ng functions?
// ano pa ba yun plng naisip ko

// data types
