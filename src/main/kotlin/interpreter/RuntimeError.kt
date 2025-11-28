package interpreter

import scanner.Token

class RuntimeError(val token: Token, override val message: String) : RuntimeException(message)