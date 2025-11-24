// move through tokens one by one
package parser
import main.Bridge

class TokenStream<Token>(tokens: List<Token>) {

    private val iterator = tokens.iterator()

    // token consumed
    var current: Token? = null
        private set
    // next token to read
    var nextToken: Token? = if (iterator.hasNext()) iterator.next() else null
        private set

    // move forward by one token
    fun advance() {
        current = nextToken
        nextToken = if (iterator.hasNext()) iterator.next() else null
    }

    fun hasNext(): Boolean = nextToken != null


}
