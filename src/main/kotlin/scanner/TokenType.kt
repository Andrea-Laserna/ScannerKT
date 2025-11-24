package scanner

import main.Bridge

// Fixed master list of language's alphabet for type safety
enum class TokenType {

    // Single-character tokens
    LEFT_PAREN, RIGHT_PAREN,
    LEFT_BRACE, RIGHT_BRACE,
    COMMA, DOT, MINUS, PLUS,
    SEMICOLON, SLASH, STAR,
    CARET, COMMENT, COLON,

    // Compound tokens
    ARROW,

    // One or two character tokens
    BANG, BANG_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,


    // Literals
    IDENTIFIER, STRING, INT, FLOAT, BOOL,

    // Keywords
    IMPORT, WHEN, LOOP,
    TRY, CATCH, ASK, TO,
    AND, CLASS, ELSE, FALSE,
    FOR, FUN, IF, OR,
    PRINT, RETURN, SUPER, THIS,
    TRUE, VAR, NOT, WHILE,

    EOF
}
