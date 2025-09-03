import java.io.File
import java.io.InputStream
import kotlin.system.exitProcess

// entry point
object Alpha {
    var errorExists: Boolean = false

    fun main(args: Array<String>) {
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
        if (errorExists) exitProcess(65)
    }

    // read code from terminal
    private fun runPrompt() {
        while(true){
            print(">")
            // if enter, set line to the string. if ctrl D or C EOF, exit
            val line = readLine() ?: break
            run(line)
            errorExists = false
        }
    }

    private fun run(source: String) {
        // scan the string
        Scanner scanner = new Scanner(source)
        // scan for tokens in the string
        var tokens: MutableList<Token> = scanner.scanTokens()
        // print tokens for now
        println("Running: $source")
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
