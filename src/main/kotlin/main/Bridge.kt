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
import kotlin.system.exitProcess
import java.io.File
import java.io.InputStream
import interpreter.Interpreter
import interpreter.RuntimeError


// entry point
object Bridge {
    var errorExists: Boolean = false
    // NEW: Create a single interpreter instance to maintain state (Environment)
    val interpreter = Interpreter()

    fun start(args: Array<String>) {
        if (args.size > 1) {
            println("Usage: Bridge [script]")
            exitProcess(64)
        } else if (args.size == 1) {
            runFile(args[0])
        } else {
            runPrompt()
        }
    }

    // read code from another file
    private fun runFile(path: String) {
        val inputStream: InputStream = File(path).inputStream()
        val inputString = inputStream.reader().readText()
        run(inputString)
        if (errorExists) exitProcess(65)
    }

    // Interactive REPL
    private fun runPrompt() {
        while(true) {
            print("> ") // print prompt
            val line = readLine() ?: break
            run(line)
            errorExists = false
        }
    }

    // Reworked 'run' function to handle multiple statements
    private fun run(source: String) {
        // ===========
        // Scanner
        // ===========
        val scanner = TokenScanner(source)
        val tokens: List<Token> = scanner.scanTokens()

        // for (token in tokens) {
        //     // Uncomment the next line to see the scanned tokens
        //     println(token)
        // }

        // ==========
        // Parser
        // ==========
        val stream = TokenStream(tokens)
        val parser = Parser(stream)
        // Parse the whole file/input as a list of statements (Program tree)
        val program = parser.parse()

        // Stop if there was a syntax error during parsing
        if (errorExists) return

        // Print the parsed program using AstPrinter for visibility
        println("[Parser] Parse completed. AST:")
        val printer = AstPrinter()
        printer.printProgram(program)

        // Run the interpreter on the parsed program tree
        interpreter.interpret(program)

        // Interpreter (keep commented unless ready to execute Program trees)
        // interpreter.interpretProgram(program)
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
    
    // NEW: Handler for Runtime Errors
    fun runtimeError (error: RuntimeError) {
        // Use error.message which already contains the context (e.g., "Undefined variable 'x'.")
        System.err.println("${error.message}\n[line ${error.token.line}]")
        errorExists = true
    }
}

fun main(args: Array<String>) {
    Bridge.start(args)
}