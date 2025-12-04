package parser

import main.Bridge
import scanner.Token
import scanner.TokenType // Ensure TokenType is imported
import kotlin.math.exp

// ps. ga base lng ko sng algo sa LEC4 slide ni ma'am ara

class Parser(val tokens: TokenStream) {
    // ... (rest of the file remains the same until declaration())

    // --- Statement Parsing ---

    /**
     * The main parsing function. It returns the single, recursive Program tree root.
     * [MODIFIED] Now initiates the construction of the pure recursive Program structure,
     * avoiding the use of an intermediate List collection.
     */
    fun parse(): Program { 
        return programSequence()
    }

    /**
     * Builds the Program tree iteratively by parsing statements and prepending them
     * to the sequence structure. Since prepending reverses the order, the resulting
     * structure must be reversed again before returning.
     */
    private fun programSequence(): Program {
        var reversedSequence: Program = Program.Empty
        
        // 1. Build the tree structure backwards (Last statement becomes the head of this sequence)
        while (tokens.nextToken?.type != TokenType.EOF) {
            val statement = declaration()
            // Prepend the new statement to the current structure
            reversedSequence = Program.Sequence(statement, reversedSequence)
        }

        // 2. Reverse the sequence tree to restore the correct program order
        return reverseProgramSequence(reversedSequence)
    }

    /**
     * Traverses a Program sequence structure and reverses the order of the statements
     * to ensure proper execution flow.
     */
    private fun reverseProgramSequence(program: Program): Program {
        var reversed: Program = Program.Empty
        var current: Program = program

        // Traverse the input sequence and prepend each head to the new `reversed` sequence
        while (current != Program.Empty) {
            when (val seq = current) {
                is Program.Sequence -> {
                    reversed = Program.Sequence(seq.head, reversed)
                    current = seq.tail
                }
                Program.Empty -> break
            }
        }
        return reversed
    }

    private fun declaration(): Statement {
        try {
            // NEW: Skip any leading COMMENT tokens before trying to parse a statement.
            while (match(TokenType.COMMENT)) { /* skip */ }

            if (match(TokenType.VAR)) return varDeclaration()
            // NEW: Add parsing for the 'ask' keyword
            if (match(TokenType.ASK)) return askStatement() 
            return statement()
        } catch (error: ParseError) {
            synchronize()
            // FIXED: Use the fully qualified name Statement.ExpressionStatement
            return Statement.ExpressionStatement(Expression.Literal(null)) // Return a dummy statement to continue parsing
        }
    }
    
    private fun varDeclaration(): Statement {
        val name = consume(TokenType.IDENTIFIER, "Expect variable name.")
        
        var initializer: Expression? = null
        if (match(TokenType.EQUAL)) {
            initializer = expr() // Parse the initializer expression
        }
        
        consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.")
        return Statement.Var(name, initializer)
    }

    private fun askStatement(): Statement {
        val name = consume(TokenType.IDENTIFIER, "Expect variable name after 'ask'.")
        val prompt = expr() // The prompt must be an expression (usually a string literal)
        consume(TokenType.SEMICOLON, "Expect ';' after 'ask' statement.")
        return Statement.Ask(name, prompt)
    }

    private fun statement(): Statement {
        if (match(TokenType.PRINT)) {
            return printStatement()
        }
        if (match(TokenType.LEFT_BRACE)) {
            return Statement.Block(block())
        }
        return expressionStatement()
    }
    
    private fun printStatement(): Statement {
        val value = expr()
        consume(TokenType.SEMICOLON, "Expect ';' after value.")
        return Statement.Print(value)
    }

    private fun expressionStatement(): Statement {
        val expression = expr()
        consume(TokenType.SEMICOLON, "Expect ';' after expression.")
        return Statement.ExpressionStatement(expression)
    }

    private fun block(): List<Statement> {
        val statements = mutableListOf<Statement>()

        // Recursively skip comments inside the block before parsing declarations
        while (tokens.nextToken?.type != TokenType.RIGHT_BRACE && tokens.nextToken?.type != TokenType.EOF) {
            statements.add(declaration())
        }

        consume(TokenType.RIGHT_BRACE, "Expect '}' after block.")
        return statements
    }


    // --- Expression Logic Updates: Assignment ---

    // Entry point now calls assignment()
    fun expr(): Expression{
        return assignment()
    }

    /**
     * assignment -> IDENTIFIER "=" assignment | equality ;
     * Assignment is right-associative and is the highest-precedence expression type.
     */
    private fun assignment(): Expression {
        // Parse the next lower-precedence level (equality)
        val expr = equality() 

        // Check if we have an assignment operator
        if (match(TokenType.EQUAL)) {
            val equals = tokens.current!! 
            
            // The right-hand side is also an assignment (right-associativity: a = b = c)
            val value = assignment() 

            // Check if the left-hand side is a valid assignment target (must be an identifier)
            if (expr is Expression.Identifier) {
                val name = expr.name // The token for the identifier
                return Expression.Assignment(name, value)
            } 
            
            // If it's not an identifier (e.g., (a + b) = 5), report an error
            Bridge.error(equals.line, "Invalid assignment target.")
        }

        // If no '=' was found, return the equality expression parsed earlier
        return expr
    }

    // ===== EQUALITY (Existing, no change needed) =====
    private fun equality(): Expression{
        var expr = comparison()
        while (tokens.nextToken?.type == TokenType.EQUAL_EQUAL ||
            tokens.nextToken?.type == TokenType.BANG_EQUAL){
            val operation = tokens.advance()!!
            val right = comparison()
            expr = Expression.Binary(expr, operation, right)
        }
        return expr
    }

    // ===== COMPARISON, TERM, FACTOR, EXPONENT, UNARY, PRIMARY (Unchanged logic) =====
    private fun comparison(): Expression{
        var expr = term()
        while (tokens.nextToken?.type == TokenType.GREATER ||
            tokens.nextToken?.type == TokenType.GREATER_EQUAL ||
            tokens.nextToken?.type == TokenType.LESS ||
            tokens.nextToken?.type == TokenType.LESS_EQUAL) {
            val operation = tokens.advance()!!
            val right = term()
            expr = Expression.Binary(expr, operation, right)
        }
        return expr
    }

    private fun term(): Expression {
        var expr = factor()
        while (match(TokenType.PLUS, TokenType.MINUS)) {
            val operation = tokens.current!!
            val right = factor()
            expr = Expression.Binary(expr, operation, right)
        }
        return expr
    }

    private fun factor(): Expression {
        var expr = exponent()
        while (match(TokenType.STAR, TokenType.SLASH)) {
            val operation = tokens.current!!
            val right = exponent()
            expr = Expression.Binary(expr, operation, right)
        }
        return expr
    }

    private fun exponent(): Expression {
        var expr = unary()
        if (match(TokenType.CARET)) {
            val operation = tokens.current!!
            val right = exponent() 
            expr = Expression.Binary(expr, operation, right)
        }
        return expr
    }

    private fun unary(): Expression {
        if (match(TokenType.MINUS, TokenType.PLUS, TokenType.BANG)) {
            val op = tokens.current!!
            val right = unary()
            return Expression.Unary(op, right)
        }
        return primary()
    }

    private fun primary(): Expression{
        return when (tokens.nextToken?.type) {
            TokenType.FALSE -> { tokens.advance()!!; Expression.Literal(false) }
            TokenType.TRUE -> { tokens.advance()!!; Expression.Literal(true) }
            TokenType.NIL -> { tokens.advance()!!; Expression.Literal(null) }
            
            // FIXED: Use TokenType.INT and TokenType.FLOAT instead of the missing TokenType.NUMBER
            TokenType.INT, TokenType.FLOAT -> { 
                val token = tokens.advance()!!
                // Use the literal property which should hold the parsed number value (as Double)
                val value = token.literal as? Double ?: token.lexeme.toDouble()
                Expression.Literal(value) 
            }
            
            TokenType.BOOL -> { 
                val token = tokens.advance()!!
                Expression.Literal(token.lexeme.toBoolean()) 
            }
            TokenType.STRING -> { 
                val token = tokens.advance()!!
                Expression.Literal(token.literal) 
            }
            TokenType.IDENTIFIER -> { 
                val token = tokens.advance()!!
                Expression.Identifier(token) 
            }
            TokenType.LEFT_PAREN -> {
                tokens.advance()
                val expr = expr()
                if (tokens.nextToken?.type != TokenType.RIGHT_PAREN) {
                    Bridge.error(1, "expected ')' after expression")
                } else {
                    tokens.advance()
                }
                Expression.Group(expr)
            }
            else -> {
                val token = tokens.nextToken ?: tokens.current 
                throw error(token ?: Token(TokenType.EOF, "", null, 1), "Expect expression.")
            }
        }
    }


    // --- Utility Methods (Unchanged) ---
    private fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (tokens.nextToken?.type == type) {
                tokens.advance()
                return true
            }
        }
        return false
    }

    private fun consume(type: TokenType, message: String): Token {
        if (tokens.nextToken?.type == type) {
            return tokens.advance()!!
        }
        throw error(tokens.nextToken!!, message) 
    }

    private fun error(token: Token, message: String): ParseError {
        Bridge.error(token.line, " at '${token.lexeme}': $message")
        return ParseError()
    }

    class ParseError : RuntimeException() 

    private fun synchronize() {
        tokens.advance() 
        while (tokens.nextToken?.type != TokenType.EOF) {
            if (tokens.current?.type == TokenType.SEMICOLON) return
            when (tokens.nextToken?.type) {
                TokenType.CLASS, TokenType.FUN, TokenType.VAR, TokenType.FOR, TokenType.IF, 
                TokenType.WHILE, TokenType.PRINT, TokenType.RETURN, TokenType.ASK -> return // Added ASK
                else -> tokens.advance()
            }
        }
    }
}