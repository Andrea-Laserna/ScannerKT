package parser

import scanner.Token // Import your actual Token class

// Remove <Token> from the class name to avoid shadowing the real Token class
class TokenStream(tokens: List<Token>) {

    private val iterator = tokens.iterator()

    // token consumed
    var current: Token? = null
        private set

    // next token to read
    var nextToken: Token? = if (iterator.hasNext()) iterator.next() else null
        private set

    // FIXED: Return type is now Token?, and it returns the consumed token
    fun advance(): Token? {
        current = nextToken
        nextToken = if (iterator.hasNext()) iterator.next() else null
        return current
    }

    fun hasNext(): Boolean = nextToken != null
}