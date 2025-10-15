package parser

class TokenStream<Token>(tokens: List<Token>) {

    private val iterator = tokens.iterator()

    var current: Token? = null
        private set
    var nextToken: Token? = if (iterator.hasNext()) iterator.next() else null
        private set

    fun advance() {
        current = nextToken
        nextToken = if (iterator.hasNext()) iterator.next() else null
    }

    fun hasNext(): Boolean = nextToken != null


}
