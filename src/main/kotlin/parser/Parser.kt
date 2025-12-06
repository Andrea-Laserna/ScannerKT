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
            if (match(TokenType.FUN) || match(TokenType.FUNC)) return function("function")
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
        // Semicolon optional
        optionalSemicolon()
        return Statement.Var(name, initializer)
    }

    private fun askStatement(): Statement {
        val name = consume(TokenType.IDENTIFIER, "Expect variable name after 'ask'.")
        val prompt = expr() // The prompt must be an expression (usually a string literal)
        // Semicolon optional
        optionalSemicolon()
        return Statement.Ask(name, prompt)
    }

    private fun statement(): Statement {
        if (match(TokenType.PRINT)) {
            return printStatement()
        }
        if (match(TokenType.IF)) {
            return ifStatement()
        }
        if (match(TokenType.WHILE)) {
            return whileStatement()
        }
        if (match(TokenType.FOR)) {
            return forStatement()
        }
        if (match(TokenType.WHEN)) {
            return whenStatement()
        }
        if (match(TokenType.LOOP)) {
            return loopStatement()
        }
        if (match(TokenType.MOVE)) {
            return moveStatement()
        }
        if (match(TokenType.INC)) {
            return incStatement()
        }
        if (match(TokenType.DEC)) {
            return decStatement()
        }
        if (match(TokenType.RES_INT, TokenType.RES_STR, TokenType.RES_FLOAT, TokenType.RES_BOOL, TokenType.RES_CMP)) {
            return reserveDeclaration()
        }
        if (match(TokenType.RETURN) || match(TokenType.RET)) {
            return returnStatement()
        }
        if (match(TokenType.LEFT_BRACE)) {
            return Statement.Block(block())
        }
        return expressionStatement()
    }
    
    private fun printStatement(): Statement {
        val value = expr()
        // Semicolon optional
        optionalSemicolon()
        return Statement.Print(value)
    }

    private fun expressionStatement(): Statement {
        val expression = expr()
        // Semicolon optional
        optionalSemicolon()
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

    // funDecl → "fun" IDENTIFIER "(" parameters? ")" block
    private fun function(kind: String): Statement.Function {
        val name = consume(TokenType.IDENTIFIER, "Expect $kind name.")
        consume(TokenType.LEFT_PAREN, "Expect '(' after $kind name.")
        val parameters = mutableListOf<Token>()
        if (tokens.nextToken?.type != TokenType.RIGHT_PAREN) {
            do {
                if (parameters.size >= 255) {
                    error(tokens.nextToken!!, "Can't have more than 255 parameters.")
                }
                parameters.add(consume(TokenType.IDENTIFIER, "Expect parameter name."))
            } while (match(TokenType.COMMA))
        }
        consume(TokenType.RIGHT_PAREN, "Expect ')' after parameters.")
        consume(TokenType.LEFT_BRACE, "Expect '{' before $kind body.")
        val body = block()
        return Statement.Function(name, parameters, body)
    }

    private fun returnStatement(): Statement {
        val keyword = tokens.current!!
        val value = if (tokens.nextToken?.type != TokenType.SEMICOLON) expr() else null
        optionalSemicolon()
        return Statement.Return(keyword, value)
    }

    // ifStmt → "if" "(" expression ")" statement ( "else" statement )? ;
    private fun ifStatement(): Statement {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'if'.")
        val condition = expr()
        consume(TokenType.RIGHT_PAREN, "Expect ')' after if condition.")
        val thenBranch = statement()
        val elseBranch = if (match(TokenType.ELSE)) statement() else null
        return Statement.If(condition, thenBranch, elseBranch)
    }

    // whileStmt → "while" "(" expression ")" statement ;
    private fun whileStatement(): Statement {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'while'.")
        val condition = expr()
        consume(TokenType.RIGHT_PAREN, "Expect ')' after while condition.")
        val body = statement()
        return Statement.While(condition, body)
    }

    // forStmt → "for" "(" ( varDecl | exprStmt | ";" ) expression? ";" expression? ")" statement ;
    // Desugar to while
    private fun forStatement(): Statement {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'for'.")

        val initializer: Statement? = when {
            match(TokenType.SEMICOLON) -> null
            match(TokenType.VAR) -> varDeclaration()
            else -> expressionStatement()
        }

        var condition: Expression? = null
        if (!match(TokenType.SEMICOLON)) {
            condition = expr()
            consume(TokenType.SEMICOLON, "Expect ';' after loop condition.")
        }

        var increment: Expression? = null
        if (!match(TokenType.RIGHT_PAREN)) {
            increment = expr()
            consume(TokenType.RIGHT_PAREN, "Expect ')' after for clauses.")
        }

        var body = statement()

        if (increment != null) {
            body = Statement.Block(listOf(body, Statement.ExpressionStatement(increment)))
        }

        val whileCond = condition ?: Expression.Literal(true)
        body = Statement.While(whileCond, body)

        return if (initializer != null) {
            Statement.Block(listOf(initializer, body))
        } else body
    }

    // MOVE target, expr
    private fun moveStatement(): Statement {
        val target = consume(TokenType.IDENTIFIER, "Expect target after MOVE.")
        consume(TokenType.COMMA, "Expect ',' after target.")
        val value = expr()
        optionalSemicolon()
        return Statement.Move(target, value)
    }

    // INC target, step
    private fun incStatement(): Statement {
        val target = consume(TokenType.IDENTIFIER, "Expect target after INC.")
        consume(TokenType.COMMA, "Expect ',' after target.")
        val step = expr()
        optionalSemicolon()
        return Statement.Inc(target, step)
    }

    private fun decStatement(): Statement {
        val target = consume(TokenType.IDENTIFIER, "Expect target after DEC.")
        consume(TokenType.COMMA, "Expect ',' after target.")
        val step = expr()
        optionalSemicolon()
        return Statement.Dec(target, step)
    }

    // RES_* name
    private fun reserveDeclaration(): Statement {
        val typeTok = tokens.current!!
        val name = consume(TokenType.IDENTIFIER, "Expect name after reservation type.")
        optionalSemicolon()
        return Statement.Reserve(typeTok, name)
    }

    private fun optionalSemicolon() {
        // Consume a semicolon if present; skip otherwise
        if (tokens.nextToken?.type == TokenType.SEMICOLON) {
            tokens.advance()
        }
    }

    // WHEN selector { predicate -> action ... [ELSE -> action] }
    private fun whenStatement(): Statement {
        val selector = expr()
        consume(TokenType.LEFT_BRACE, "Expect '{' after WHEN selector.")
        val branches = mutableListOf<Statement.WhenBranch>()
        var elseBranch: List<Statement>? = null

        while (tokens.nextToken?.type != TokenType.RIGHT_BRACE && tokens.nextToken?.type != TokenType.EOF) {
            // ELSE branch
            if (match(TokenType.ELSE)) {
                consume(TokenType.ARROW, "Expect '->' after ELSE.")
                elseBranch = parseActionBlockOrSingle()
                continue
            }
            // Predicates
            val predToken = consumePredicateToken()
            consume(TokenType.ARROW, "Expect '->' after predicate.")
            val action = parseActionBlockOrSingle()
            branches.add(Statement.WhenBranch(predToken, action))
        }
        consume(TokenType.RIGHT_BRACE, "Expect '}' to close WHEN block.")
        return Statement.When(selector, branches, elseBranch)
    }

    private fun parseActionBlockOrSingle(): List<Statement> {
        return if (match(TokenType.LEFT_BRACE)) {
            block()
        } else {
            listOf(statement())
        }
    }

    private fun consumePredicateToken(): Token {
        val t = tokens.nextToken?.type
        val isPred = when (t) {
            TokenType.EQUAL_KW, TokenType.NOTEQUAL_KW, TokenType.LESS_KW, TokenType.GREATER_KW, TokenType.LESSEQ_KW, TokenType.GREATEREQ_KW,
            TokenType.OK, TokenType.ERR -> true
            else -> false
        }
        if (!isPred) throw error(tokens.nextToken!!, "Expect predicate keyword in WHEN.")
        return tokens.advance()!!
    }

    // LOOP { body } tailPredicate?
    private fun loopStatement(): Statement {
        consume(TokenType.LEFT_BRACE, "Expect '{' after LOOP.")
        val body = block()
        // Optional tail predicate: one of predicate keywords followed by an expression (loosely parsed)
        val tailPred: Expression? = if (isPredicateKeyword(tokens.nextToken?.type)) {
            val name = tokens.advance()!!
            // Optional argument expression after predicate; if not, use empty args
            val args = mutableListOf<Expression>()
            // If there's anything meaningful (not RIGHT_BRACE/EOF/SEMICOLON), parse one expression
            if (tokens.nextToken != null && tokens.nextToken?.type !in setOf(TokenType.EOF)) {
                // Be tolerant: parse until we cannot
                args.add(expr())
            }
            Expression.PredCall(name, args)
        } else null
        return Statement.Loop(body, tailPred)
    }

    private fun isPredicateKeyword(t: TokenType?): Boolean {
        return when (t) {
            TokenType.EQUAL_KW, TokenType.NOTEQUAL_KW, TokenType.LESS_KW, TokenType.GREATER_KW, TokenType.LESSEQ_KW, TokenType.GREATEREQ_KW,
            TokenType.OK, TokenType.ERR -> true
            else -> false
        }
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
        val expr = logicOr() 

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

    // ===== LOGICAL OR/AND, EQUALITY =====
    private fun logicOr(): Expression {
        var expr = logicAnd()
        while (match(TokenType.OR) || match(TokenType.OR_OR)) {
            val op = tokens.current!!
            val right = logicAnd()
            expr = Expression.Binary(expr, op, right)
        }
        return expr
    }

    private fun logicAnd(): Expression {
        var expr = equality()
        while (match(TokenType.AND) || match(TokenType.AND_AND)) {
            val op = tokens.current!!
            val right = equality()
            expr = Expression.Binary(expr, op, right)
        }
        return expr
    }

    // ===== EQUALITY =====
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
            // CONCAT is treated as a variadic prefix operator: CONCAT expr (',' [CONCAT]? expr)*
            if (match(TokenType.CONCAT)) {
                return concatExpr()
        }
        // Bridge-style CALL keyword for function call: CALL ident(args)
        if (match(TokenType.CALL)) {
            val calleeIdent = consume(TokenType.IDENTIFIER, "Expect function name after CALL.")
            consume(TokenType.LEFT_PAREN, "Expect '(' after function name.")
            val args = mutableListOf<Expression>()
            if (tokens.nextToken?.type != TokenType.RIGHT_PAREN) {
                do { args.add(expr()) } while (match(TokenType.COMMA))
            }
            val paren = consume(TokenType.RIGHT_PAREN, "Expect ')' after arguments.")
            return Expression.Call(Expression.Identifier(calleeIdent), paren, args)
        }
        // Operation calls like ADD_OP, SUB_OP, MUL_OP, DIV_OP, MOD_OP, EXP_OP, INC, DEC, RAND, CMP
        if (isOpKeyword(tokens.nextToken?.type)) {
            return opCall()
        }
        return call()
    }

    // call → primary ( "(" arguments? ")" )*
    private fun call(): Expression {
        var expr = primary()
        while (true) {
            if (match(TokenType.LEFT_PAREN)) {
                expr = finishCall(expr)
            } else {
                break
            }
        }
        return expr
    }

    private fun finishCall(callee: Expression): Expression {
        val args = mutableListOf<Expression>()
        if (tokens.nextToken?.type != TokenType.RIGHT_PAREN) {
            do {
                args.add(expr())
            } while (match(TokenType.COMMA))
        }
        val paren = consume(TokenType.RIGHT_PAREN, "Expect ')' after arguments.")
        return Expression.Call(callee, paren, args)
    }

    // Parse CONCAT sequences: CONCAT expr (',' CONCAT expr)*
    private fun concatExpr(): Expression {
        val parts = mutableListOf<Expression>()
        // We are here after first CONCAT matched by caller
            // Parse first part using assignment() to avoid re-entering CONCAT
            parts.add(assignment())
        // Additional parts: ',' [CONCAT]? <expr>
        while (match(TokenType.COMMA)) {
            if (tokens.nextToken?.type == TokenType.CONCAT) tokens.advance()
                parts.add(assignment())
        }
        return Expression.Concat(parts)
    }

    // Parse operation calls: NAME expr (',' expr)* where NAME is an op keyword
    private fun opCall(): Expression {
        val name = tokens.advance()!!
        val args = mutableListOf<Expression>()
        // Expect at least one argument
        args.add(unary())
        while (match(TokenType.COMMA)) {
            args.add(unary())
        }
        return Expression.OpCall(name, args)
    }

    private fun isOpKeyword(t: TokenType?): Boolean {
        return when (t) {
            TokenType.ADD_OP, TokenType.SUB_OP, TokenType.MUL_OP, TokenType.DIV_OP, TokenType.MOD_OP, TokenType.EXP_OP,
            TokenType.INC, TokenType.DEC, TokenType.RAND, TokenType.CMP -> true
            else -> false
        }
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