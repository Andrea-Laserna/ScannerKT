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

    init {
        // Native functions
        globals.define("clock", object : LoxCallable {
            override fun arity(): Int = 0
            override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                return (System.currentTimeMillis() / 1000.0)
            }
            override fun toString(): String = "<native fn clock>"
        })
        globals.define("toString", object : LoxCallable {
            override fun arity(): Int = 1
            override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                return interpreter.stringify(arguments[0])
            }
            override fun toString(): String = "<native fn toString>"
        })
        globals.define("readLine", object : LoxCallable {
            override fun arity(): Int = 0
            override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                return kotlin.io.readLine()
            }
            override fun toString(): String = "<native fn readLine>"
        })
    }

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
        // Print the prompt and read a line from the console
        // Keep prompt formatting consistent: do not append newline so user can type on same line
        print(prompt)
        // Flush is not necessary in simple console apps, but keep it explicit
        System.out.flush()
        val input = kotlin.io.readLine()

        // Try to parse numeric input to Double; otherwise keep as String. Null if user cancelled/EOF.
        val value: Any? = when {
            input == null -> null
            input.toDoubleOrNull() != null -> input.toDouble()
            else -> input
        }

        // Store the value in the current environment
        try {
            environment.assign(stmt.name, value)
        } catch (e: RuntimeError) {
            environment.define(stmt.name.lexeme, value)
        }
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

    override fun visitIncStatement(stmt: Statement.Inc) {
        val stepVal = evaluate(stmt.step)
        val currentVal = try { environment.get(stmt.target) } catch (e: RuntimeError) { null }
        val newVal = if (currentVal is Double && stepVal is Double) currentVal + stepVal else currentVal
        if (newVal == null) throw RuntimeError(stmt.target, "INC requires numeric variable and step.")
        try {
            environment.assign(stmt.target, newVal)
        } catch (e: RuntimeError) {
            environment.define(stmt.target.lexeme, newVal)
        }
    }

    override fun visitDecStatement(stmt: Statement.Dec) {
        val stepVal = evaluate(stmt.step)
        val currentVal = try { environment.get(stmt.target) } catch (e: RuntimeError) { null }
        val newVal = if (currentVal is Double && stepVal is Double) currentVal - stepVal else currentVal
        if (newVal == null) throw RuntimeError(stmt.target, "DEC requires numeric variable and step.")
        try {
            environment.assign(stmt.target, newVal)
        } catch (e: RuntimeError) {
            environment.define(stmt.target.lexeme, newVal)
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

    override fun visitIfStatement(stmt: Statement.If) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch)
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch)
        }
    }

    override fun visitWhileStatement(stmt: Statement.While) {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body)
        }
    }

    override fun visitFunctionStatement(stmt: Statement.Function) {
        val function = LoxFunction(stmt, environment)
        environment.define(stmt.name.lexeme, function)
    }

    override fun visitReturnStatement(stmt: Statement.Return) {
        val value = if (stmt.value != null) evaluate(stmt.value) else null
        throw ReturnException(value)
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
            // String ops
            "LEN" -> {
                val s = args.getOrNull(0) as? String ?: return 0.0
                s.length.toDouble()
            }
            "CHAR" -> {
                val s = args.getOrNull(0) as? String ?: return ""
                val i = (args.getOrNull(1) as? Double)?.toInt() ?: return ""
                if (i < 0 || i >= s.length) "" else s[i].toString()
            }
            "SUBSTR" -> {
                val s = args.getOrNull(0) as? String ?: return ""
                val start = (args.getOrNull(1) as? Double)?.toInt() ?: 0
                val len = (args.getOrNull(2) as? Double)?.toInt() ?: 0
                val from = start.coerceIn(0, s.length)
                val to = (from + len).coerceIn(0, s.length)
                s.substring(from, to)
            }
            "SETCHAR" -> {
                val s = args.getOrNull(0) as? String ?: return ""
                val idx = (args.getOrNull(1) as? Double)?.toInt() ?: return s
                val ch = args.getOrNull(2) as? String ?: return s
                if (ch.isEmpty() || idx < 0 || idx >= s.length) return s
                val b = StringBuilder(s)
                b.setCharAt(idx, ch[0])
                b.toString()
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
        // Short-circuit logical operators
        if (expr.operation.lexeme == "or" || expr.operation.lexeme == "||") {
            val lv = evaluate(expr.left)
            if (isTruthy(lv)) return lv
            return evaluate(expr.right)
        }
        if (expr.operation.lexeme == "and" || expr.operation.lexeme == "&&") {
            val lv = evaluate(expr.left)
            if (!isTruthy(lv)) return lv
            return evaluate(expr.right)
        }
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

    override fun visitCallExpression(expr: Expression.Call): Any? {
        val callee = evaluate(expr.callee)
        val arguments = expr.arguments.map { evaluate(it) }

        if (callee !is LoxCallable) {
            throw RuntimeError((expr.callee as? Expression.Identifier)?.name ?: Token(scanner.TokenType.IDENTIFIER, "<fn>", null, 0), "Can only call functions.")
        }
        if (arguments.size != callee.arity()) {
            throw RuntimeError(expr.paren, "Expected ${callee.arity()} arguments but got ${arguments.size}.")
        }
        return callee.call(this, arguments)
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

// Callable interface
interface LoxCallable {
    fun arity(): Int
    fun call(interpreter: Interpreter, arguments: List<Any?>): Any?
}

// Function object with closure
class LoxFunction(private val declaration: Statement.Function, private val closure: Environment) : LoxCallable {
    override fun arity(): Int = declaration.params.size
    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val environment = Environment(closure)
        for (i in declaration.params.indices) {
            environment.define(declaration.params[i].lexeme, arguments[i])
        }
        try {
            interpreter.executeBlock(declaration.body, environment)
        } catch (ret: ReturnException) {
            return ret.value
        }
        return null
    }
    override fun toString(): String = "<fn ${declaration.name.lexeme}>"
}

// Return unwinding via exception
class ReturnException(val value: Any?) : RuntimeException(null, null, false, false)