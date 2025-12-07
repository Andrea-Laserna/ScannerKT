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
    IDENTIFIER, COMMENT, STRING, INT, FLOAT, NIL, // Consolidated types for literal values
    
    // Built-in Values
    TRUE, FALSE, 
    BOOL, // Type keyword for boolean

    // Keywords/Control Flow/Statements
    IMPORT, WHEN, DEFAULT, LOOP, TRY, CATCH, FINALLY, RETURN,
    VAR, ASK, TO, PRINT,

    // Functions & declarations
    FUNC, RET, BYREF, CALL, VOID,

    // Data types and containers
    INT_TYPE, STR_TYPE, FLOAT_TYPE, BOOL_TYPE, CMP_TYPE,
    RES_INT, RES_STR, RES_FLOAT, RES_BOOL, RES_CMP,
    ARR, MAP,

    // Operations and intrinsics
    MOVE, ADD_OP, SUB_OP, MUL_OP, DIV_OP, MOD_OP, EXP_OP,
    // String ops
    LEN, CHAR, SETCHAR, SUBSTR,
    CONCAT,
    INC, DEC, RAND, CMP, 

    // Compare predicates (enum-like keywords)
    EQUAL_KW, NOTEQUAL_KW, LESS_KW, GREATER_KW, LESSEQ_KW, GREATEREQ_KW,

    // Result-tagged error handling
    RES, OK, ERR, VALUE, ERROR,
    RAISE,

    // Other Keywords (compat / reserved)
    AND, CLASS, FOR, FUN, IF, OR, SUPER, NOT, WHILE,

    // End of File
    EOF
}