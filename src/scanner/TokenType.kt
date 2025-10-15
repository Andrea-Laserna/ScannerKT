package scanner

// fixed master list of language's alphabet for type safety
enum class TokenType {
    // Single-character tokens
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR, COMMENT, CARET,

    // One or two character tokens
    BANG, BANG_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,

    // Literals
    IDENTIFIER, STRING, INT, FLOAT, BOOL,

    // Keywords
    AND, CLASS, ELSE, FALSE, FUN, FOR, IF, NONE, OR,
    PRINT, RETURN, SUPER, THIS, TRUE, VAR, WHILE, NOT,

    EOF
}

