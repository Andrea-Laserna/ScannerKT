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
    COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR,

    // One or two character tokens
    BANG, BANG_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,

    // Literals
    IDENTIFIER, STRING, NUMBER,

    // Keywords
    AND, CLASS, ELSE, FALSE, FUN, FOR, IF, NIL, OR,
    PRINT, RETURN, SUPER, THIS, TRUE, VAR, WHILE,

    EOF
}

class Token (val type: TokenType, val lexeme: String, val literal: Any?, val line: Int) {
    override public fun toString(): String { // print readable token instead of memory address
        return "$type $lexeme $literal $line"
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
        addToken(TokenType.EOF, null)
        return tokens
    }

    private fun isAtEnd(): Boolean {
        return current >= source.length
    }

    // adds token types to a token list 
    private fun scanToken() {
        var c: Char = advance()
        when (c) {
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
            // insert operators

            // insert longer lexemes: division, new lines, white space

            // insert string literals
            
            // insert number literals

            // insert reserved words and identifiers

            else -> Alpha.error(line, "Unexpected character: $c")
        }

    }

    // get the current character to scan
    private fun advance(): Char {
        return source[current++]
    }

    private fun addToken(type: TokenType, literal: Any?) {
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
        var tokens: List<Token> = scanner.scanTokens()
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
