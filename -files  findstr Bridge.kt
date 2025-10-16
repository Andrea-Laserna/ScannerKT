[1mdiff --git a/src/main/Bridge.kt b/src/Bridge.kt[m
[1msimilarity index 80%[m
[1mrename from src/main/Bridge.kt[m
[1mrename to src/Bridge.kt[m
[1mindex ac50507..1a12821 100644[m
[1m--- a/src/main/Bridge.kt[m
[1m+++ b/src/Bridge.kt[m
[36m@@ -1,17 +1,12 @@[m
[31m-package main[m
 /* run via:[m
[31m-kotlinc (Get-ChildItem -Recurse -Filter *.kt | ForEach-Object { $_.FullName }) -include-runtime -d Bridge.jar[m
[31m->> java -jar Bridge.jar code.txt[m
[32m+[m[32mkotlinc (Get-ChildItem -Filter *.kt | ForEach-Object { $_.FullName }) -include-runtime -d Bridge.jar[m
[32m+[m[32mjava -jar .Bridge.jar[m
 */[m
 import scanner.Token[m
 import scanner.TokenScanner[m
[31m-import parser.Parser[m
[31m-import parser.TokenStream[m
[31m-import parser.AstPrinter[m
[31m-import kotlin.system.exitProcess[m
 import java.io.File[m
 import java.io.InputStream[m
[31m-[m
[32m+[m[32mimport kotlin.system.exitProcess[m
 [m
 [m
 // entry point[m
[36m@@ -21,7 +16,7 @@[m [mobject Bridge {[m
     fun start(args: Array<String>) {[m
         // java -jar updated.jar one two three[m
         if (args.size > 1) {[m
[31m-            println("Usage: Bridge [script]")[m
[32m+[m[32m            println("Usage: .Bridge [script]")[m
             exitProcess(64)[m
         // java -jar updated.jar code.txt[m
         } else if (args.size == 1) {[m
[36m@@ -54,7 +49,7 @@[m [mobject Bridge {[m
     }[m
 [m
     private fun run(source: String) {[m
[31m-        // Scanner[m
[32m+[m[32m        // build the scanner[m
         val scanner = TokenScanner(source)[m
         // scan for tokens in the string[m
         val tokens: List<Token> = scanner.scanTokens()[m
[36m@@ -62,17 +57,6 @@[m [mobject Bridge {[m
         for (token in tokens) {[m
             println(token)[m
         }[m
[31m-[m
[31m-        // Parser[m
[31m-        val stream = TokenStream(tokens)[m
[31m-        val parser = Parser(stream)[m
[31m-        val expression = parser.expr()[m
[31m-[m
[31m-        // Print AST[m
[31m-        if (!errorExists) {[m
[31m-            val printer = AstPrinter()[m
[31m-            println(printer.print(expression))[m
[31m-        }[m
     }[m
 [m
     // error handling[m
[1mdiff --git a/src/code.txt b/src/code.txt[m
[1mdeleted file mode 100644[m
[1mindex cba549f..0000000[m
[1m--- a/src/code.txt[m
[1m+++ /dev/null[m
[36m@@ -1 +0,0 @@[m
[31m-1 + 2 * 3[m
[1mdiff --git a/src/parser/Expression.kt b/src/parser/Expression.kt[m
[1mindex 0279b21..1823ac1 100644[m
[1m--- a/src/parser/Expression.kt[m
[1m+++ b/src/parser/Expression.kt[m
[36m@@ -1,13 +1,10 @@[m
[31m-// defines the abstract syntax tree node types[m
 package parser[m
[31m-import main.Bridge[m
[32m+[m
 import scanner.Token[m
 [m
 sealed class Expression {[m
[31m-    data class Binary(val left: Expression, val operation: Token, val right: Expression): Expression()[m
[32m+[m[32m    data class Binary(val left: Expression, val operation: Token?, val right: Expression): Expression()[m
     data class Unary(val op: Token, val right: Expression) : Expression()[m
     data class Group(val expr: Expression) : Expression()[m
     data class Identifier(val name: Token) : Expression()[m
[31m-    data class Literal(val value: Any?) : Expression()[m
[31m-[m
 }[m
\ No newline at end of file[m
[1mdiff --git a/src/parser/Parser.kt b/src/parser/Parser.kt[m
[1mindex 588b12f..1144702 100644[m
[1m--- a/src/parser/Parser.kt[m
[1m+++ b/src/parser/Parser.kt[m
[36m@@ -1,6 +1,5 @@[m
 package parser[m
 [m
[31m-import main.Bridge[m
 import scanner.Token[m
 import scanner.TokenType[m
 import kotlin.math.exp[m
[36m@@ -9,31 +8,24 @@[m [mimport kotlin.math.exp[m
 [m
 class Parser(val tokens: TokenStream<Token>) {[m
 [m
[31m-    //entry point starting at highest level rule: equality[m
[31m-    fun expr(): Expression{[m
[32m+[m[32m    //entry[m
[32m+[m[32m    private fun expr(): Expression{[m
         return equality()[m
     }[m
[31m-    // equality[m
     private fun equality(): Expression{[m
[31m-        // parse a comparison[m
         var expr = comparison()[m
         while (tokens.nextToken?.type == TokenType.EQUAL_EQUAL ||[m
             tokens.nextToken?.type == TokenType.BANG_EQUAL){[m
[31m-            // when == or != is seen, parse another comparison[m
             val operation = tokens.current[m
             tokens.advance()[m
             val right = comparison()[m
[31m-            // write both sides in a Binary node[m
[31m-            expr = Expression.Binary(expr, operation!!, right)[m
[32m+[m[32m            expr = Expression.Binary(expr, operation, right)[m
         }[m
         return expr[m
     }[m
 [m
[31m-// --- FUNCTIONS ---[m
     private fun comparison(): Expression{[m
[31m-        // parse higher-precedence rule first - term is higher than comparison[m
         var expr = term()[m
[31m-        // next token is one of its operators[m
         while (tokens.nextToken?.type == TokenType.GREATER ||[m
             tokens.nextToken?.type == TokenType.GREATER_EQUAL ||[m
             tokens.nextToken?.type == TokenType.LESS ||[m
[36m@@ -41,90 +33,67 @@[m [mclass Parser(val tokens: TokenStream<Token>) {[m
             tokens.advance()[m
             val operation = tokens.current[m
             val right = term()[m
[31m-            // combine into binary expression[m
[31m-            expr = Expression.Binary(expr, operation!!, right)[m
[32m+[m[32m            expr = Expression.Binary(expr, operation, right)[m
         }[m
         return expr[m
     }[m
     private fun term(): Expression{[m
[31m-        // parse factor - higher than term[m
[31m-        var expr = factor()[m
[32m+[m[32m        factor()[m
         while (tokens.nextToken?.type == TokenType.PLUS ||[m
             tokens.nextToken?.type == TokenType.MINUS) {[m
             tokens.advance()[m
[31m-            val operation = tokens.current[m
[31m-            val right = factor()[m
[31m-            expr = Expression.Binary(expr, operation!!, right)[m
[32m+[m[32m            factor()[m
         }[m
[31m-        return expr[m
     }[m
[31m-[m
     private fun factor(): Expression{[m
[31m-        // parse exponent - higher than factor[m
[31m-        var expr = exponent()[m
[32m+[m[32m        exponent()[m
         while (tokens.nextToken?.type == TokenType.STAR ||[m
             tokens.nextToken?.type == TokenType.SLASH) {[m
             tokens.advance()[m
[31m-            val operation = tokens.current[m
[31m-            val right = exponent()[m
[31m-            expr = Expression.Binary(expr, operation!!, right)[m
[32m+[m[32m            exponent()[m
         }[m
[31m-        return expr[m
     }[m
     private fun exponent(): Expression{[m
[31m-        // parse unary - higher than factor[m
[31m-        val expr = unary()[m
[32m+[m[32m        unary()[m
         if (tokens.nextToken?.type == TokenType.CARET) {        // CARET IS YUNG ^[m
             tokens.advance()[m
[31m-            val right = exponent()                              // recursion makes it right associative[m
[31m-            return Expression.Binary(expr, tokens.current!!, right)                                        // recursive call here sa exponent instead sa unary makes it right-associative[m
[32m+[m[32m            exponent()                                          // recursive call here sa exponent instead sa unary makes it right-associative[m
[32m+[m[32m                                                                // e.g. 2 ^ 3 ^ 2 should start sa (3^2) then 2^(8)[m
         }[m
[31m-        return expr[m
     }[m
[31m-[m
     private fun unary(): Expression{[m
[31m-        // sign[m
         if (tokens.nextToken?.type == TokenType.BANG ||[m
             tokens.nextToken?.type == TokenType.MINUS ||[m
             tokens.nextToken?.type == TokenType.PLUS) {[m
             tokens.advance()[m
[31m-            val operation = tokens.current!![m
[31m-            val right = unary()[m
[31m-            return Expression.Unary(operation, right)[m
[32m+[m[32m            unary()[m
         } else {[m
[31m-            // parse primary - higher than unary[m
[31m-            return primary()[m
[32m+[m[32m            primary()[m
         }[m
     }[m
     private fun primary(): Expression{[m
[31m-        // handles leaf nodes - identifiers, literals, group expr[m
[31m-        return when (tokens.nextToken?.type) {[m
[32m+[m[32m        when (tokens.nextToken?.type) {[m
             TokenType.IDENTIFIER,[m
             TokenType.INT,[m
             TokenType.FLOAT,[m
             TokenType.BOOL -> {[m
                 tokens.advance()[m
                 //build here sa tree[m
[31m-                Expression.Identifier(tokens.current!!)[m
             }[m
 [m
             TokenType.LEFT_PAREN -> {[m
                 tokens.advance()[m
[31m-                // start parsing at expr[m
[31m-                val expr = expr()[m
[31m-                if (tokens.nextToken?.type == TokenType.RIGHT_PAREN){[m
[32m+[m[32m                expr()[m
[32m+[m[32m                if (tokens.current?.type == TokenType.RIGHT_PAREN){[m
                     tokens.advance()[m
[31m-                    return Expression.Group(expr)[m
                 }[m
                 else{[m
                     Bridge.error(1, "no closing parenthesis")[m
[31m-                    return Expression.Group(expr)[m
                 }[m
             }[m
 [m
             else -> {[m
                 Bridge.error(1, "no primary")                       //temporary lng to dito and line, not implemented properly yet[m
[31m-                Expression.Identifier(scanner.Token(scanner.TokenType.IDENTIFIER, "error", null, 1)) // placeholder[m
             }[m
         }[m
     }[m
[1mdiff --git a/src/parser/Printer.kt b/src/parser/Printer.kt[m
[1mdeleted file mode 100644[m
[1mindex fba096e..0000000[m
[1m--- a/src/parser/Printer.kt[m
[1m+++ /dev/null[m
[36m@@ -1,30 +0,0 @@[m
[31m-package parser[m
[31m-import main.Bridge[m
[31m-[m
[31m-class AstPrinter {[m
[31m-    // take an expression node and return its string represention[m
[31m-    fun print(expr: Expression): String {[m
[31m-        return when (expr) {[m
[31m-            // subclass of expression we are dealing with[m
[31m-            is Expression.Binary -> parenthesize(expr.operation?.lexeme ?: "?", expr.left, expr.right) // parenthesis around, get operator symbol..uses ? if it's null[m
[31m-            is Expression.Unary -> parenthesize(expr.op.lexeme, expr.right)                            // parenthesis around, get operator, get right expression[m
[31m-            is Expression.Group -> parenthesize("group", expr.expr)                                    // adds group before the expression[m
[31m-            is Expression.Identifier -> expr.name.lexeme                                                // variable name[m
[31m-            is Expression.Literal -> expr.value.toString()                                              // print actual value in string                                  [m
[31m-        }[m
[31m-    }[m
[31m-[m
[31m-    // helper function: wraps operator (name) and operands in parentheses[m
[31m-    // pass any number of expressions[m
[31m-    private fun parenthesize(name: String, vararg exprs: Expression): String {[m
[31m-        // build string piece by piece[m
[31m-        val builder = StringBuilder() [m
[31m-        builder.append("(").append(name)[m
[31m-        // for each child expression, add a space and call print recursively[m
[31m-        for (expr in exprs) {[m
[31m-            builder.append(" ").append(print(expr))[m
[31m-        }[m
[31m-        builder.append(")")[m
[31m-        return builder.toString()[m
[31m-    }[m
[31m-}[m
[1mdiff --git a/src/parser/TokenStream.kt b/src/parser/TokenStream.kt[m
[1mindex 2a91b89..b82d3c3 100644[m
[1m--- a/src/parser/TokenStream.kt[m
[1m+++ b/src/parser/TokenStream.kt[m
[36m@@ -1,19 +1,14 @@[m
[31m-// move through tokens one by one[m
 package parser[m
[31m-import main.Bridge[m
 [m
 class TokenStream<Token>(tokens: List<Token>) {[m
 [m
     private val iterator = tokens.iterator()[m
 [m
[31m-    // token consumed[m
     var current: Token? = null[m
         private set[m
[31m-    // next token to read[m
     var nextToken: Token? = if (iterator.hasNext()) iterator.next() else null[m
         private set[m
 [m
[31m-    // move forward by one token[m
     fun advance() {[m
         current = nextToken[m
         nextToken = if (iterator.hasNext()) iterator.next() else null[m
[1mdiff --git a/src/Bridge.jar b/src/scanner/Bridge.jar[m
[1msimilarity index 94%[m
[1mrename from src/Bridge.jar[m
[1mrename to src/scanner/Bridge.jar[m
[1mindex f09292e..a5bbe1d 100644[m
Binary files a/src/Bridge.jar and b/src/scanner/Bridge.jar differ
[1mdiff --git a/src/scanner/Keywords.kt b/src/scanner/Keywords.kt[m
[1mindex 5c1fa27..1eec217 100644[m
[1m--- a/src/scanner/Keywords.kt[m
[1m+++ b/src/scanner/Keywords.kt[m
[36m@@ -1,7 +1,5 @@[m
 package scanner[m
 [m
[31m-import main.Bridge[m
[31m-[m
 object Keywords{[m
     val keywords: Map<String, TokenType> = mapOf([m
         "and" to TokenType.AND,[m
[1mdiff --git a/src/scanner/Token.kt b/src/scanner/Token.kt[m
[1mindex c8e976a..24db406 100644[m
[1m--- a/src/scanner/Token.kt[m
[1m+++ b/src/scanner/Token.kt[m
[36m@@ -1,5 +1,4 @@[m
 package scanner[m
[31m-import main.Bridge[m
 [m
 class Token (val type: TokenType, val lexeme: String, val literal: Any?, val line: Int) {[m
     override fun toString(): String { // print readable token instead of memory address[m
[1mdiff --git a/src/scanner/TokenScanner.kt b/src/scanner/TokenScanner.kt[m
[1mindex 14f3694..24c8a73 100644[m
[1m--- a/src/scanner/TokenScanner.kt[m
[1m+++ b/src/scanner/TokenScanner.kt[m
[36m@@ -1,7 +1,5 @@[m
 package scanner[m
 [m
[31m-import main.Bridge[m
[31m-[m
 data class TokenScanner(val source: String) {[m
     var current = 0 // to move until we find the end of the token[m
     var start = 0 // to know where the start of the token is[m
[1mdiff --git a/src/scanner/TokenType.kt b/src/scanner/TokenType.kt[m
[1mindex 75bfc24..8d7294c 100644[m
[1m--- a/src/scanner/TokenType.kt[m
[1m+++ b/src/scanner/TokenType.kt[m
[36m@@ -1,7 +1,5 @@[m
 package scanner[m
 [m
[31m-import main.Bridge[m
[31m-[m
 // fixed master list of language's alphabet for type safety[m
 enum class TokenType {[m
     // Single-character tokens[m
[1mdiff --git a/src/scanner/code.txt b/src/scanner/code.txt[m
[1mnew file mode 100644[m
[1mindex 0000000..f62a666[m
[1m--- /dev/null[m
[1m+++ b/src/scanner/code.txt[m
[36m@@ -0,0 +1,2 @@[m
[32m+[m[32mallow x = true[m
[32m+[m[32mprint x[m
\ No newline at end of file[m
