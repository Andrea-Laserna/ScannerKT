package scanner

class Token (val type: TokenType, val lexeme: String, val literal: Any?, val line: Int) {
    override fun toString(): String { // print readable token instead of memory address
        return ("Type=${type}, Lexeme=${lexeme}, Literal=${literal}, Line=${line}")
    }
}