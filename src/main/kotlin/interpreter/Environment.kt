package interpreter

import scanner.Token
import kotlin.collections.HashMap

/**
 * Manages the mapping of variable names to values, handling nested scopes.
 *
 * Each Environment links to its parent (enclosing) environment, creating a chain
 * for variable lookup (scope).
 *
 * @property enclosing The parent environment, or null if this is the global scope.
 * 
 * If an environment is created inside another one (when entering a block {...}),
 * the inner environment points to the outer one via enclosing
 */
class Environment(val enclosing: Environment? = null) {
    // Stores the variables and values defined directly in this scope
    private val values = HashMap<String, Any?>()

    /**
     * Defines a new variable in the current scope.
     * Used for 'var x = 10;'
     * @param name The lexeme (name) of the variable.
     * @param value The initial value (can be null/nil).
     */
    fun define(name: String, value: Any?) {
        // Redefinition is allowed in this implementation for simplicity, 
        // but it shadows the outer scope variable.
        values[name] = value
    }

    /**
     * Finds and returns the value of a variable, checking enclosing scopes recursively.
     * Used for 'print x;'
     * @param name The Token representing the identifier.
     * @throws RuntimeError if the variable is not found in any scope.
     */
    fun get(name: Token): Any? {
        // Check if variable name exists in hashmap for current scope.
        if (values.containsKey(name.lexeme)) {
            // Return variable found
            return values[name.lexeme]
        }

        // Recursively look up in the enclosing scope
        if (enclosing != null) {
            return enclosing.get(name)
        }

        // If we reach the global scope and still haven't found it
        throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }

    /**
     * Finds and updates the value of an EXISTING variable, checking enclosing scopes recursively.
     * Used for 'x = 10;'
     * @param name The Token representing the identifier.
     * @param value The new value to assign.
     * @throws RuntimeError if the variable is not found in any scope (cannot assign to undeclared).
     */
    fun assign(name: Token, value: Any?) {
        // Check if variable exists in hashmap for current scope.
        if (values.containsKey(name.lexeme)) {
            // Update with new value.
            values[name.lexeme] = value
            return
        }

        // Recursively try to assign in the enclosing scope
        if (enclosing != null) {
            enclosing.assign(name, value)
            return
        }

        // Error: Cannot assign to a variable that doesn't exist
        throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }
}