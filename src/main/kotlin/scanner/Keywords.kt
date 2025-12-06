package scanner

import main.Bridge
import scanner.TokenType 

object Keywords{
    // The map is normalized to UPPERCASE keys to allow case-insensitive keyword matching.
    val keywords: Map<String, TokenType> = mapOf(
        // Core control flow
        "IMPORT" to TokenType.IMPORT,
        "WHEN" to TokenType.WHEN,
        "ELSE" to TokenType.ELSE,
        "LOOP" to TokenType.LOOP,
        "TRY" to TokenType.TRY,
        "CATCH" to TokenType.CATCH,
        "FINALLY" to TokenType.FINALLY,

        // I/O and utility
        "ASK" to TokenType.ASK,
        "TO" to TokenType.TO,
        "PRINT" to TokenType.PRINT,

        // Functions & declarations
        "FUNC" to TokenType.FUNC,
        "RET" to TokenType.RET,
        "BYREF" to TokenType.BYREF,
        "CALL" to TokenType.CALL,
        "RETURN" to TokenType.RETURN,
        "VOID" to TokenType.VOID,

        // Types
        "INT" to TokenType.INT_TYPE,
        "STR" to TokenType.STR_TYPE,
        "FLOAT" to TokenType.FLOAT_TYPE,
        "BOOL" to TokenType.BOOL_TYPE,
        "CMP" to TokenType.CMP_TYPE,
        "ARR" to TokenType.ARR,
        "MAP" to TokenType.MAP,

        // Compact reservation keywords
        "RES_INT" to TokenType.RES_INT,
        "RES_STR" to TokenType.RES_STR,
        "RES_FLOAT" to TokenType.RES_FLOAT,
        "RES_BOOL" to TokenType.RES_BOOL,
        "RES_CMP" to TokenType.RES_CMP,

        // Values
        "TRUE" to TokenType.TRUE,
        "FALSE" to TokenType.FALSE,
        "NIL" to TokenType.NIL,

        // Operations and intrinsics
        "MOVE" to TokenType.MOVE,
        "ADD" to TokenType.ADD_OP,
        "SUB" to TokenType.SUB_OP,
        "MUL" to TokenType.MUL_OP,
        "DIV" to TokenType.DIV_OP,
        "MOD" to TokenType.MOD_OP,
        "EXP" to TokenType.EXP_OP,
        "CONCAT" to TokenType.CONCAT,
        "INC" to TokenType.INC,
        "DEC" to TokenType.DEC,
        "RAND" to TokenType.RAND,
        "CMP" to TokenType.CMP,

        // Compare predicates
        "EQUAL" to TokenType.EQUAL_KW,
        "NOTEQUAL" to TokenType.NOTEQUAL_KW,
        "LESS" to TokenType.LESS_KW,
        "GREATER" to TokenType.GREATER_KW,
        "LESSEQ" to TokenType.LESSEQ_KW,
        "GREATEREQ" to TokenType.GREATEREQ_KW,

        // Result-tagged error handling
        "RES" to TokenType.RES,
        "OK" to TokenType.OK,
        "ERR" to TokenType.ERR,
        "VALUE" to TokenType.VALUE,
        "ERROR" to TokenType.ERROR,
        "RAISE" to TokenType.RAISE,

        // Compatibility / additional reserved words
        "AND" to TokenType.AND,
        "CLASS" to TokenType.CLASS,
        "FOR" to TokenType.FOR,
        "FUN" to TokenType.FUN,
        "IF" to TokenType.IF,
        "OR" to TokenType.OR,
        "SUPER" to TokenType.SUPER,
        "NOT" to TokenType.NOT,
        "WHILE" to TokenType.WHILE,
        "VAR" to TokenType.VAR
    )
}