package main
/* run via:
# Collect all .kt files recursively
$files = Get-ChildItem -Recurse -Filter *.kt | ForEach-Object { $_.FullName }

# Compile all files into Bridge.jar including Kotlin runtime
kotlinc $files -include-runtime -d Bridge.jar

# Run
java -jar Bridge.jar code.txt
*/
import scanner.Token
import scanner.TokenScanner
import parser.Parser
import parser.TokenStream
import parser.AstPrinter
import parser.Expression
import parser.Statement // Import Statement sealed class
import kotlin.system.exitProcess
import java.io.File
import java.io.InputStream
import interpreter.Interpreter
import interpreter.RuntimeError
import parser.Parser.ParseError

// entry point
object Bridge {
    var errorExists: Boolean = false

    fun start(args: Array<String>) {
        // java -jar updated.jar one two three
        if (args.size > 1) {
            println("Usage: Bridge [script]")
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
        val inputString = inputStream.reader().readText()
        run(inputString)
        if (errorExists) exitProcess(65)
    }

    // REPL
    private fun runPrompt() {
        while (true) {
            print(">") // print the string. if ctrl D or C EOF, exit
            val line = readLine() ?: break
            run(line)
            // dont kill the REPL
            errorExists = false
        }
    }

    // private fun run(source: String) {
    //     // Scanner
    //     val scanner = TokenScanner(source)
    //     // scan for tokens in the string
    //     val tokens: List<Token> = scanner.scanTokens()
    //     // print tokens for now
    //     // for (token in tokens) {
    //     //     println(token)
    //     // }

    //     // Parser
    //     val stream = TokenStream(tokens)
    //     val parser = Parser(stream)
        
    //     // Correctly parse a list of statements
    //     val statements = parser.parse()

    //     // Print AST
    //     if (!errorExists) {
    //         val printer = AstPrinter()
            
    //         // >>> CRITICAL FIX: Iterate over statements and print them <<<
    //         for (statement in statements) {
    //             printer.printStatement(statement)
    //         }
    //     }
    //     println()
    // }

    private fun run(source: String) {
        // Scanner
        val scanner = TokenScanner(source)
        // scan for tokens in the string
        // The previous error was here. Ensure all imports are correct.
        val tokens: List<Token> = scanner.scanTokens() // Line 94 in the error log

        // Parser
        val stream = TokenStream(tokens)
        val parser = Parser(stream)
        
        // --- NEW INTERPRETATION LOGIC ---
        
        // This attempts to parse a single expression, catching ParseError.
        val expression: Expression? = try {
            parser.expr()
        } catch (e: ParseError) { // <--- ENSURE ParseError IS NOW VISIBLE
            // Error already reported by Bridge.error() inside parser
            null
        }

        // Interpreter/Evaluation
        if (!errorExists && expression != null) {
            val interpreter = Interpreter()
            interpreter.interpret(expression)
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

    // NEW: Add a specific function to report runtime errors
    fun runtimeError(error: RuntimeError) {
        System.err.println("[line ${error.token.line}] Runtime error: ${error.message}")
        errorExists = true
    }
}

fun main(args: Array<String>) {
    Bridge.start(args)
}