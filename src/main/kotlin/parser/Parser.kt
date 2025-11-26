package parser

import main.Bridge
import scanner.Token
import scanner.TokenType
import kotlin.math.exp

class Parser(val tokens: TokenStream) {

    // Main entry point for the parser to convert the list of tokens into the Abstract Syntax Tree AST
    fun parse(): List<Statement> {
        val statements = mutableListOf<Statement>()
        // TODO: Handle try-catch for errors here to allow parsing to continue
        
        // Run as long as there are tokens to process
        while (tokens.nextToken?.type != TokenType.EOF) {
            // Function to parse the next complete unit of code like print, var, ask
            // Once statement() successfully consumes all tokens, return Statement obj
            statements.add(statement())
        }
        // List of statement objects
        return statements
    }

    // Handle blocks
    private fun block(): Statement.Block {
        val statements = mutableListOf<Statement>()

        // Keep parsing statements until we hit the closing brace '}' or EOF
        while (tokens.nextToken?.type != TokenType.RIGHT_BRACE && tokens.nextToken?.type != TokenType.EOF) {
            statements.add(statement()) // Recursively parse the statements inside the block
        }

        consume(TokenType.RIGHT_BRACE, "Expected '}' after block.")
        return Statement.Block(statements)
    }
    // Handles any type of statement (declaration, print, loop, etc.)
    private fun statement(): Statement {
        // NEW CHECK: If we see '{', parse a block
        if (match(TokenType.LEFT_BRACE)) return block()
        
        // If we see 'print', parse a print statement
        if (match(TokenType.PRINT)) return printStatement()
        
        // FIX: Only check for VAR here.
        // NOTE: Type-based declarations (INT, STRING, etc.) are temporarily disabled 
        // to prevent ambiguity with literal tokens (TokenType.INT, TokenType.STRING). 
        // This will be fixed properly later by separating literal types (e.g., INT_LITERAL)
        // from type keywords (INT).
        if (match(TokenType.VAR)) {
            return declaration()
        }

        // If we see 'ask', parse an input statement
        if (match(TokenType.ASK)) return askStatement()

        // Default: Treat it as a standard expression followed by a semicolon
        return expressionStatement()
    }

    // ===== STATEMENTS =====

    private fun declaration(): Statement {
        // Assume type/var token has already been consumed by match in statement()
        // The current token holds the keyword (VAR, INT, etc.) that triggered the match.
        val type = tokens.current!! 

        // Handle multiple identifiers: a, b, c (Simplified for now, only parses first)
        val names = mutableListOf<Token>()
        // First name is mandatory
        // Verifies that the next token is a variable name and consumes it
        var name = consume(TokenType.IDENTIFIER, "Expected identifier after type or 'var'.")
        names.add(name)
        // Multiple names chaining
        while (match(TokenType.COMMA)) {
            name = consume(TokenType.IDENTIFIER, "Expected identifier after ','.")
            names.add(name)
        }

        // Var statements are optional: You can declare a variable without giving it a starting value
        var initializer: Expression? = null

        // Check for initializer (e.g., = 10)
        if (match(TokenType.EQUAL)) {
            // Assignment expression starts from logical_or
            initializer = logical_or() 
        }

        // Termination Consistency Rule: All top-level statements require a semicolon
        consume(TokenType.SEMICOLON, "Expected ';' after variable declaration.")
        
        // Returning the first declared variable for now (as the AST supports single Var declaration)
        // TODO: Multi-declaration syntax is partially supported for now but not fully utilized in the AST structure yet
        return Statement.Var(names.first(), initializer)
    }

    private fun printStatement(): Statement.Print {
        // print keyword already consumed
        val value = logical_or() // Expression to be printed
        consume(TokenType.SEMICOLON, "Expected ';' after 'print' value.")
        return Statement.Print(value)
    }

    private fun askStatement(): Statement.Ask {
        // ask keyword already consumed
        val name = consume(TokenType.IDENTIFIER, "Expected identifier for input storage.")
        val prompt = logical_or() // The prompt expression (usually a string literal)
        consume(TokenType.SEMICOLON, "Expected ';' after 'ask' statement.")
        return Statement.Ask(name, prompt)
    }

    private fun expressionStatement(): Statement.ExpressionStatement {
        val expr = logical_or() // Parse the expression
        // expression is followed by a semicolon
        consume(TokenType.SEMICOLON, "Expected ';' after expression statement.")
        return Statement.ExpressionStatement(expr)
    }

    // ===== EXPRESSIONS (Precedence Rules) =====

    // entry point starting at highest level rule: logical_or (lowest precedence)
    fun expr(): Expression{
        return logical_or()
    }
    
    // ===== LOGICAL OR (Level 1: or) =====
    
    private fun logical_or(): Expression {
        var expr = logical_and()

        while (match(TokenType.OR)) {
            val operation = tokens.current!!
            val right = logical_and()
            expr = Expression.Binary(expr, operation, right)
        }
        return expr
    }

    // ===== LOGICAL AND (Level 2: and) =====
    
    private fun logical_and(): Expression {
        var expr = equality()

        while (match(TokenType.AND)) {
            val operation = tokens.current!!
            val right = equality()
            expr = Expression.Binary(expr, operation, right)
        }
        return expr
    }
    
    // ===== EQUALITY (Level 3: ==, !=) =====

    private fun equality(): Expression{
        var expr = comparison()

        while (match(TokenType.EQUAL_EQUAL, TokenType.BANG_EQUAL)){
            val operation = tokens.current!! 
            val right = comparison()
            expr = Expression.Binary(expr, operation, right)
        }
        return expr
    }

    // ===== COMPARISON (Level 4: <, <=, >, >=) =====

    private fun comparison(): Expression{
        var expr = term()

        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            val operation = tokens.current!!
            val right = term()
            expr = Expression.Binary(expr, operation, right)
        }
        return expr
    }

    // ===== TERM (Level 5: Addition/Subtraction) =====

    private fun term(): Expression{
        var expr = factor()

        while (match(TokenType.PLUS, TokenType.MINUS)) {
            val operation = tokens.current!!
            val right = factor()
            expr = Expression.Binary(expr, operation, right)
        }
        return expr
    }

    // ===== FACTOR (Level 6: Multiplication/Division) =====

    private fun factor(): Expression{
        var expr = exponent()

        while (match(TokenType.STAR, TokenType.SLASH)) {
            val operation = tokens.current!!
            val right = exponent()
            expr = Expression.Binary(expr, operation, right)
        }
        return expr
    }

    // ===== EXPONENT (Level 7: ^) =====

    private fun exponent(): Expression {
        var expr = unary()

        // *** FIX: Checking for CARET (^) as requested ***
        if (match(TokenType.CARET)) {
            val operation = tokens.current!!
            // Right recursion: Exponentiations are right-associative
            val right = exponent() 
            return Expression.Binary(expr, operation, right)
        }
        return expr
    }

    // ===== UNARY (Level 8: -, +, !) =====

    private fun unary(): Expression{
        if (match(TokenType.BANG, TokenType.MINUS, TokenType.PLUS)) {
            val operation = tokens.current!!
            val right = unary()
            return Expression.Unary(operation, right)
        }
        return primary()
    }

    // ===== PRIMARY (Level 9: Leaf Nodes) =====

    private fun primary(): Expression{
        val nextType = tokens.nextToken?.type
        return when (nextType) {

            // ===== Literals =====
            TokenType.TRUE, TokenType.FALSE -> {
                val token = tokens.advance()!!
                Expression.Literal(token.literal)
            }
            TokenType.INT, TokenType.FLOAT, TokenType.STRING -> {
                val token = tokens.advance()!!
                Expression.Literal(token.literal) 
            }

            // ===== IDENTIFIERS =====
            TokenType.IDENTIFIER -> {
                val token = tokens.advance()!!
                Expression.Identifier(token)
            }

            // ===== GROUP =====
            TokenType.LEFT_PAREN -> {
                tokens.advance() // Consume '('
                val expr = logical_or() // Use the highest precedence expression rule
                consume(TokenType.RIGHT_PAREN, "Expected ')' after expression.")
                Expression.Group(expr)
            }

            else -> {
                val token = tokens.nextToken // Use the next token for error context
                error(token?.line ?: 0, "Expected expression (literal, identifier, or '('), but found '${token?.lexeme ?: "EOF"}'")                       
                // Synchronization: Advance past the offending token to try and resume parsing.
                tokens.advance() 
                throw ParseError() 
            }
        }
    }

    // ===== UTILITY FUNCTIONS =====

    // Attempts to advance and check the type, returning true if it matches one of the provided types.
    private fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (tokens.nextToken?.type == type) {
                tokens.advance()
                return true
            }
        }
        return false
    }
    
    // Checks if the next token is of the expected type, consumes it, and returns it.
    private fun consume(type: TokenType, message: String): Token {
        if (tokens.nextToken?.type == type) {
            return tokens.advance()!!
        }
        val errorToken = tokens.nextToken ?: Token(TokenType.EOF, "", null, 0)
        error(errorToken.line, message)
        throw ParseError() // Stop the current rule parsing on error
    }

    private fun error(line: Int, message: String) {
        Bridge.error(line, message)
    }

    private class ParseError : RuntimeException()
}