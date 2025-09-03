import java.io.File
import java.io.InputStream
import kotlin.system.exitProcess

// entry point
object Alpha {
    @JvmStatic // static method that converts it into jvm bytecode
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
        println(inputString)
        // run(inputString))
    }

    private fun runPrompt() {
        while(true){
            print(">")
            // if enter, set line to the string. if ctrl D EOF, exit
            val line = readLine() ?: break
            println("You typed: $line")
            // run(line)
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
}
