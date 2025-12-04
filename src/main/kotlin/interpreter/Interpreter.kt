package interpreter

import parser.*
import scanner.Token
import main.Bridge
import kotlin.math.pow

/**
 * The main execution engine for the language.
 * Implements both ExpressionVisitor and StatementVisitor to execute the AST nodes.
 *
 * It uses the Environment class to manage variable storage and scopes.
 */
class Interpreter : ExpressionVisitor<Any?>, StatementVisitor<Unit> {
    // Global scope environment: The root of the scope chain.
    private val globals = Environment() 
    // Current environment reference: Points to the environment currently in use.
    private var environment = globals

    // --- Entry Point ---

    /**
     * Entry point for interpreting the entire program tree.
     * [MODIFIED] Traverses the Program recursively (Sequence/Empty) instead of iterating a list.
     */
    fun interpret(program: Program) {
        var currentProgram: Program = program
        try {
            // Optional: Set up the global environment (e.g., native functions) if necessary
            
            // Traverse the recursive Program structure until we hit the Empty node
            while (currentProgram != Program.Empty) {
                when (val seq = currentProgram) {
                    is Program.Sequence -> {
                        execute(seq.head) // Execute the current statement
                        currentProgram = seq.tail // Move to the next statement in the sequence
                    }
                    Program.Empty -> break // Should be caught by the loop condition, but safe here
                }
            }

        } catch (error: RuntimeError) {
            // Catches runtime errors and reports them using the Bridge
            Bridge.runtimeError(error)
        }
    }

    private fun execute(stmt: Statement) {
        stmt.accept(this)
    }

    private fun evaluate(expr: Expression): Any? {
        return expr.accept(this)
    }
    
    // --- Statement Visitor Implementations ---
    
    override fun visitExpressionStatement(stmt: Statement.ExpressionStatement) {
        // Execute the expression, discarding the result (e.g., '1 + 1;')
        evaluate(stmt.expression)
    }

    override fun visitPrintStatement(stmt: Statement.Print) {
        val value = evaluate(stmt.expression)
        println(stringify(value))
    }

    // Variable Declaration Logic (var x = value;)
    override fun visitVarStatement(stmt: Statement.Var) {
        var value: Any? = null
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer)
        }
        // Define the variable in the current scope
        environment.define(stmt.name.lexeme, value) 
    }

    // Block Scope Logic ({ statements })
    override fun visitBlockStatement(stmt: Statement.Block) {
        // Create a new scope (environment) linked to the current one
        // This execution still iterates over a list of statements within the block
        executeBlock(stmt.statements, Environment(environment))
    }
    
    // Ask Statement Logic (ask x "prompt";)
    override fun visitAskStatement(stmt: Statement.Ask) {
        // Evaluate the prompt expression (which should be a string literal)
        val prompt = stringify(evaluate(stmt.prompt))
        
        // Note: For a true console app, you would use readLine(). 
        // Here, we simulate the action and define the variable as null 
        // or a placeholder until input is handled externally.
        println("--- ASK: $prompt (Storing result in: ${stmt.name.lexeme}) ---")
        environment.define(stmt.name.lexeme, null) // Initialize the variable in the environment
    }
    
    /**
     * Executes a list of statements within a new, temporary environment (scope).
     */
    fun executeBlock(statements: List<Statement>, environment: Environment) {
        // Save the current environment reference
        val previous = this.environment 
        try {
            // Switch to the new environment for execution (entering the scope)
            this.environment = environment
            
            for (statement in statements) {
                execute(statement)
            }
        } finally {
            // Restore the previous environment when the block finishes (even if an error occurs)
            this.environment = previous // Exiting the scope
        }
    }


    // --- Expression Visitor Implementations ---

    // Variable Assignment Logic (x = value)
    override fun visitAssignmentExpression(expr: Expression.Assignment): Any? {
        val value = evaluate(expr.value)
        // Update the variable value in its defining scope (using assign to search up the chain)
        environment.assign(expr.name, value)
        return value
    }
    
    // Variable Lookup Logic (x)
    override fun visitIdentifierExpression(expr: Expression.Identifier): Any? {
        // Retrieve the variable value from the current or enclosing environments
        return environment.get(expr.name)
    }

    override fun visitLiteralExpression(expr: Expression.Literal): Any? {
        // Return the raw value (e.g., 5.0, "hello", true, nil)
        return expr.value
    }

    override fun visitGroupExpression(expr: Expression.Group): Any? {
        // Evaluate the expression inside the parentheses
        return evaluate(expr.expr)
    }

    override fun visitUnaryExpression(expr: Expression.Unary): Any? {
        val right = evaluate(expr.right)
        
        return when (expr.op.lexeme) {
            // Unary Minus: Negation, requires a number
            "-" -> checkNumberOperand(expr.op, right)?.let { -it }
            // Logical NOT: Reverses truthiness
            "!" -> !isTruthy(right)
            else -> null
        }
    }

    override fun visitBinaryExpression(expr: Expression.Binary): Any? {
        val left = evaluate(expr.left)
        val right = evaluate(expr.right)

        return when (expr.operation.lexeme) {
            // Arithmetic (using type checks for safety)
            "-" -> checkNumberOperands(expr.operation, left, right)?.let { (l, r) -> l - r }
            
            // '+' operator: Handles both numeric addition and string concatenation
            "+" -> when {
                left is Double && right is Double -> left + right
                // If either operand is a string, convert the other to string and concatenate.
                left is String || right is String -> stringify(left) + stringify(right)
                
                else -> throw RuntimeError(expr.operation, "Operands must be two numbers or two strings for '+'.")
            }
            "*" -> checkNumberOperands(expr.operation, left, right)?.let { (l, r) -> l * r }
            "/" -> checkNumberOperands(expr.operation, left, right)?.let { (l, r) -> l / r }
            "^" -> checkNumberOperands(expr.operation, left, right)?.let { (l, r) -> l.pow(r) }
            
            // Comparison
            ">" -> checkNumberOperands(expr.operation, left, right)?.let { (l, r) -> l > r }
            ">=" -> checkNumberOperands(expr.operation, left, right)?.let { (l, r) -> l >= r }
            "<" -> checkNumberOperands(expr.operation, left, right)?.let { (l, r) -> l < r }
            "<=" -> checkNumberOperands(expr.operation, left, right)?.let { (l, r) -> l <= r }

            // Equality
            "!=" -> !isEqual(left, right)
            "==" -> isEqual(left, right)
            
            else -> null
        }
    }

    // --- Type Checks and Utilities ---

    private fun isTruthy(obj: Any?): Boolean {
        // nil and false are falsey; everything else is truthy
        if (obj == null) return false
        if (obj is Boolean) return obj
        return true
    }
    
    private fun isEqual(a: Any?, b: Any?): Boolean {
        // nil == nil is true
        if (a == null && b == null) return true
        // only one is nil, so they are not equal
        if (a == null) return false
        
        return a == b
    }

    private fun checkNumberOperand(op: Token, operand: Any?): Double? {
        if (operand is Double) return operand
        throw RuntimeError(op, "Operand must be a number.")
    }

    private fun checkNumberOperands(op: Token, left: Any?, right: Any?): Pair<Double, Double>? {
        if (left is Double && right is Double) return Pair(left, right)
        throw RuntimeError(op, "Operands must be numbers.")
    }

    private fun stringify(obj: Any?): String {
        if (obj == null) return "nil"
        
        // Clean up floating point representation (e.g., 10.0 -> 10)
        if (obj is Double) {
            var text = obj.toString()
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length - 2)
            }
            return text
        }

        return obj.toString()
    }
}