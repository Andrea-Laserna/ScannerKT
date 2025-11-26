package scanner

// This enumeration defines ALL unique token types used throughout the scanner and parser.
enum class TokenType {
    // Single-character tokens
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR, COLON,
    CARET, // ^ (Exponentiation)

    // One or two character operators
    BANG, BANG_EQUAL, // ! and !=
    EQUAL, EQUAL_EQUAL, // = and ==
    GREATER, GREATER_EQUAL, // > and >=
    LESS, LESS_EQUAL, // < and <=

    // Logical Operators
    AND_AND, // && (Resolves 'AND_AND' error)
    OR_OR,   // || (Resolves 'OR_OR' error)
    AMPERSAND, // & (Resolves 'AMPERSAND' error from scanner fallback)
    PIPE,    // | (Resolves 'PIPE' error from scanner fallback)

    // Bridge specific syntax
    ARROW, // ->

    // Literals & Identifiers
    IDENTIFIER, COMMENT, STRING, INT, FLOAT, // Consolidated types for literal values
    
    // Built-in Values
    TRUE, FALSE, 
    BOOL, // Type keyword for boolean

    // Keywords/Control Flow/Statements
    IMPORT, WHEN, ELSE, LOOP, TRY, CATCH, RETURN, 
    VAR, ASK, TO, PRINT, // PRINT, VAR, ASK are now explicitly defined
    
    // Other Keywords (from your Keywords.kt snippet)
    AND, CLASS, FOR, FUN, IF, OR, SUPER, NOT, WHILE,

    // End of File
    EOF
}