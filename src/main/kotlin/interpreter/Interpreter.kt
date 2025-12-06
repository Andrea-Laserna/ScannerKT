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

    override fun visitReserveStatement(stmt: Statement.Reserve) {
        // Define reserved variable with nil
        environment.define(stmt.name.lexeme, null)
    }

    override fun visitMoveStatement(stmt: Statement.Move) {
        val value = evaluate(stmt.value)
        // Try to assign; if undefined, define in current scope
        try {
            environment.assign(stmt.target, value)
        } catch (e: RuntimeError) {
            environment.define(stmt.target.lexeme, value)
        }
    }

    override fun visitWhenStatement(stmt: Statement.When) {
        val selector = evaluate(stmt.selector)
        // Evaluate branches: first branch whose predicate matches selector runs its action
        for (branch in stmt.branches) {
            if (predicateMatches(branch.predicate.lexeme, selector)) {
                executeBlock(branch.action, Environment(environment))
                return
            }
        }
        // Else branch
        if (stmt.elseBranch != null) {
            executeBlock(stmt.elseBranch, Environment(environment))
        }
    }

    override fun visitLoopStatement(stmt: Statement.Loop) {
        do {
            executeBlock(stmt.body, Environment(environment))
            // Evaluate tail predicate; if absent, run once
        } while (stmt.tailPredicate?.let { isPredicateTrue(it) } == true)
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

    override fun visitConcatExpression(expr: Expression.Concat): Any? {
        val sb = StringBuilder()
        for (part in expr.parts) {
            sb.append(stringify(evaluate(part)))
        }
        return sb.toString()
    }

    override fun visitOpCallExpression(expr: Expression.OpCall): Any? {
        val name = expr.name.lexeme.uppercase()
        val args = expr.args.map { evaluate(it) }
        return when (name) {
            "ADD_OP", "ADD" -> binaryNum(args, name) { a, b -> a + b }
            "SUB_OP", "SUB" -> binaryNum(args, name) { a, b -> a - b }
            "MUL_OP", "MUL" -> binaryNum(args, name) { a, b -> a * b }
            "DIV_OP", "DIV" -> binaryNum(args, name) { a, b -> a / b }
            "MOD_OP", "MOD" -> binaryNum(args, name) { a, b -> a % b }
            "EXP_OP", "EXP" -> binaryNum(args, name) { a, b -> a.pow(b) }
            "INC" -> unaryNum(args, name) { a -> a + 1.0 }
            "DEC" -> unaryNum(args, name) { a -> a - 1.0 }
            "RAND" -> {
                // Simple placeholder: RAND min to max
                if (args.size >= 2 && args[0] is Double && args[1] is Double) {
                    val min = (args[0] as Double).toInt()
                    val max = (args[1] as Double).toInt()
                    (min..max).random().toDouble()
                } else 0.0
            }
            "CMP" -> {
                // Return three-way compare: -1 if a<b, 0 if equal, 1 if greater
                if (args.size >= 2 && args[0] is Double && args[1] is Double) {
                    val a = args[0] as Double
                    val b = args[1] as Double
                    when {
                        a < b -> -1.0
                        a > b -> 1.0
                        else -> 0.0
                    }
                } else 0.0
            }
            else -> null
        }
    }

    override fun visitPredCallExpression(expr: Expression.PredCall): Any? {
        // Evaluate predicate on its arguments; return boolean
        val name = expr.name.lexeme.uppercase()
        val args = expr.args.map { evaluate(it) }
        return when (name) {
            "EQUAL" -> compBool(args) { c -> c == 0.0 }
            "NOTEQUAL" -> compBool(args) { c -> c != 0.0 }
            "LESS" -> compBool(args) { c -> c < 0.0 }
            "GREATER" -> compBool(args) { c -> c > 0.0 }
            "LESSEQ" -> compBool(args) { c -> c <= 0.0 }
            "GREATEREQ" -> compBool(args) { c -> c >= 0.0 }
            "OK" -> args.firstOrNull() as? Boolean ?: false
            "ERR" -> !(args.firstOrNull() as? Boolean ?: false)
            else -> false
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

    // --- Helpers for op calls and predicates ---

    private fun binaryNum(args: List<Any?>, name: String, f: (Double, Double) -> Double): Double? {
        if (args.size < 2) return null
        val a = args[0]
        val b = args[1]
        if (a is Double && b is Double) return f(a, b)
        return null
    }

    private fun unaryNum(args: List<Any?>, name: String, f: (Double) -> Double): Double? {
        if (args.isEmpty()) return null
        val a = args[0]
        if (a is Double) return f(a)
        return null
    }

    private fun compBool(args: List<Any?>, pred: (Double) -> Boolean): Boolean {
        // Compute compare result c: if a,b provided use CMP(a,b); else expect first arg is c
        val c: Double? = when {
            args.size >= 2 && args[0] is Double && args[1] is Double -> {
                val a = args[0] as Double
                val b = args[1] as Double
                when {
                    a < b -> -1.0
                    a > b -> 1.0
                    else -> 0.0
                }
            }
            args.isNotEmpty() && args[0] is Double -> args[0] as Double
            else -> null
        }
        return c?.let { pred(it) } ?: false
    }

    private fun predicateMatches(name: String, selector: Any?): Boolean {
        val n = name.uppercase()
        val c = when (selector) {
            is Double -> selector
            is Boolean -> if (selector) 0.0 else 1.0
            else -> null
        }
        return when (n) {
            "EQUAL" -> c?.let { it == 0.0 } ?: false
            "NOTEQUAL" -> c?.let { it != 0.0 } ?: false
            "LESS" -> c?.let { it < 0.0 } ?: false
            "GREATER" -> c?.let { it > 0.0 } ?: false
            "LESSEQ" -> c?.let { it <= 0.0 } ?: false
            "GREATEREQ" -> c?.let { it >= 0.0 } ?: false
            "OK" -> selector is Boolean && selector
            "ERR" -> selector is Boolean && !selector
            else -> false
        }
    }

    private fun isPredicateTrue(expr: Expression): Boolean {
        val v = evaluate(expr)
        return when (v) {
            is Boolean -> v
            is Double -> v != 0.0
            else -> false
        }
    }
}