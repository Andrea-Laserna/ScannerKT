package scanner

object Keywords{
    val keywords: Map<String, TokenType> = mapOf(
        "and" to TokenType.AND,
        "station" to TokenType.CLASS,
        "elsewise" to TokenType.ELSE,
        "untrue" to TokenType.FALSE,
        "concerning" to TokenType.FOR,
        "affair" to TokenType.FUN,
        "perchance" to TokenType.IF,
        "or" to TokenType.OR,
        "unfold" to TokenType.PRINT,
        "hasten" to TokenType.RETURN,
        "wondrous" to TokenType.SUPER,
        "this" to TokenType.THIS,
        "true" to TokenType.TRUE,
        "allow" to TokenType.VAR,
        "not" to TokenType.NOT,
        "whilst" to TokenType.WHILE
    )
}