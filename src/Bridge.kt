/* run via:
kotlinc (Get-ChildItem -Filter *.kt | ForEach-Object { $_.FullName }) -include-runtime -d Bridge.jar
java -jar .Bridge.jar
*/
import scanner.Token
import scanner.TokenScanner
import java.io.File
import java.io.InputStream
import kotlin.system.exitProcess


// entry point
object Bridge {
    var errorExists: Boolean = false

    fun start(args: Array<String>) {
        // java -jar updated.jar one two three
        if (args.size > 1) {
            println("Usage: .Bridge [script]")
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
// TODO:
fun main(args: Array<String>) {
    Bridge.start(args)
}



//future TODOs
// kulang ng naghahandle ng \ and '
// ang ang lexeme ng EOF is ang lexeme ng prev toker.
// hindi nag eerror if walang closing parenthesis
// siguro yung rules para sa syntax ng functions?
// ano pa ba yun plng naisip ko

// data types
