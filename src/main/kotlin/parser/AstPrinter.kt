package parser

import scanner.Token
import kotlin.collections.List

// NOTE: This class no longer uses the ExpressionVisitor or StatementVisitor interfaces,
// but rather uses direct recursion via printStatement/printTree.
class AstPrinter {

    // --- Public Entry Points ---

    // Prints a list of statements (for running a script)
    fun print(statements: List<Statement>) {
        statements.forEach { printStatement(it) }
    }

    // Prints a single expression (for REPL or expression debugging)
    fun print(expression: Expression) {
        printTree(expression)
        println()
    }

    // --- Statement Printing ---

    fun printStatement(statement: Statement) {
        when (statement) {
            is Statement.Print -> {
                print("(print ")
                printTree(statement.expression)
                print(")")
            }
            is Statement.ExpressionStatement -> {
                print("(expr-stmt ")
                printTree(statement.expression)
                print(")")
            }
            is Statement.Var -> {
                print("(var ${statement.name.lexeme} ")
                if (statement.initializer != null) {
                    printTree(statement.initializer)
                } else {
                    print("none") // For uninitialized variables (var x;)
                }
                print(");")
            }
            is Statement.Block -> {
                print("{\n")
                // Recursively print all statements inside the block
                statement.statements.forEach { s ->
                    printStatement(s)
                }
                print("}\n")
            }
            // FIXED: Correctly uses Statement.Ask and its properties
            is Statement.Ask -> { 
                print("(ask ${statement.name.lexeme} ")
                printTree(statement.prompt)
                print(");")
            }
            is Statement.Reserve -> {
                print("(reserve ${statement.typeToken.lexeme} ${statement.name.lexeme})")
            }
            is Statement.Move -> {
                print("(move ")
                print(statement.target.lexeme)
                print(" ")
                printTree(statement.value)
                print(")")
            }
            is Statement.Inc -> {
                print("(inc ")
                print(statement.target.lexeme)
                print(" ")
                printTree(statement.step)
                print(")")
            }
            is Statement.Dec -> {
                print("(dec ")
                print(statement.target.lexeme)
                print(" ")
                printTree(statement.step)
                print(")")
            }
            is Statement.When -> {
                print("(when ")
                printTree(statement.selector)
                print(" { ")
                statement.branches.forEach { b ->
                    print("(")
                    print(b.predicate.lexeme)
                    print(" -> ")
                    b.action.forEach { act -> printStatement(act) }
                    print(") ")
                }
                if (statement.elseBranch != null) {
                    print("(default -> ")
                    statement.elseBranch.forEach { act -> printStatement(act) }
                    print(") ")
                }
                print("}")
                print(")")
            }
            is Statement.Loop -> {
                print("(loop { ")
                statement.body.forEach { s -> printStatement(s) }
                print(" } ")
                if (statement.tailPredicate != null) {
                    print(" ")
                    printTree(statement.tailPredicate)
                }
                print(")")
            }
            is Statement.If -> {
                print("(if ")
                printTree(statement.condition)
                print(" then ")
                printStatement(statement.thenBranch)
                if (statement.elseBranch != null) {
                    print(" else ")
                    printStatement(statement.elseBranch)
                }
                print(")")
            }
            is Statement.While -> {
                print("(while ")
                printTree(statement.condition)
                print(" ")
                printStatement(statement.body)
                print(")")
            }
            is Statement.Function -> {
                print("(func ${statement.name.lexeme} (")
                statement.params.forEachIndexed { idx, p ->
                    if (idx > 0) print(" ")
                    print(p.lexeme)
                }
                print(") ")
                statement.body.forEach { s -> printStatement(s) }
                print(")")
            }
            is Statement.Return -> {
                print("(ret ")
                if (statement.value != null) printTree(statement.value) else print("nil")
                print(")")
            }
        }
    }
    
    // --- Program Printing (tree) ---
    
    fun printProgram(program: Program) {
        printTreeProgram(program)
        println()
    }

    private fun printTreeProgram(program: Program) {
        when (program) {
            is Program.Sequence -> {
                print("(seq ")
                printStatement(program.head)
                printTreeProgram(program.tail)
                print(")")
            }
            Program.Empty -> {
                print("(empty)")
            }
        }
    }
    
    // --- Expression Printing ---
    
    fun printTree(expression: Expression) {
        when (expression) {
            is Expression.Assignment -> {
                // Assignment is right-associative, format as (= identifier value)
                parenthesize("=", Expression.Identifier(expression.name), expression.value)
            }
            is Expression.Binary -> {
                parenthesize(expression.operation.lexeme, expression.left, expression.right)
            }
            is Expression.Unary -> {
                parenthesize(expression.op.lexeme, expression.right)
            }
            is Expression.Group -> {
                parenthesize("group", expression.expr)
            }
            is Expression.Concat -> {
                // Print as (concat part1 part2 ...)
                parenthesize("concat", *expression.parts.toTypedArray())
            }
            is Expression.PredCall -> {
                // Print predicate name with its arguments
                if (expression.args.isEmpty()) {
                    print("(${expression.name.lexeme})")
                } else {
                    parenthesize(expression.name.lexeme, *expression.args.toTypedArray())
                }
            }
            is Expression.OpCall -> {
                // Print operation call similarly to predicates
                parenthesize(expression.name.lexeme, *expression.args.toTypedArray())
            }
            is Expression.Call -> {
                // Print function call: (call callee args...)
                print("(call ")
                printTree(expression.callee)
                for (arg in expression.arguments) {
                    print(" ")
                    printTree(arg)
                }
                print(")")
            }
            is Expression.Identifier -> {
                print(expression.name.lexeme)
            }
            is Expression.Literal -> {
                val value = expression.value
                // Print strings with quotes; nil/null as 'nil'; others via toString
                when (value) {
                    null -> print("nil")
                    is String -> {
                        // Escape embedded quotes minimally
                        val escaped = value.replace("\"", "\\\"")
                        print("\"$escaped\"")
                    }
                    else -> print(value.toString())
                }
            }
        }
    }

    // --- Utility Function ---

    // Helper function to format the tree nodes recursively
    private fun parenthesize(name: String, vararg expressions: Expression) {
        print("($name")
        for (expr in expressions) {
            print(" ")
            printTree(expr)
        }
        print(")")
    }
}