package be.guldentops.geert.lox;

class Token {

    private final Type type;
    private final String lexeme;
    private final Object literal;
    private final int line;

    Token(Type type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    Type getType() {
        return type;
    }

    String getLexeme() {
        return lexeme;
    }

    Object getLiteral() {
        return literal;
    }

    int getLine() {
        return line;
    }

    @Override
    public String toString() {
        return type + " " + lexeme + " " + literal;
    }

    enum Type {
        // Single-character tokens.
        LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
        COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR,

        // One or two character tokens.
        BANG, BANG_EQUAL,
        EQUAL, EQUAL_EQUAL,
        GREATER, GREATER_EQUAL,
        LESS, LESS_EQUAL,

        // Literals.
        IDENTIFIER, STRING, NUMBER,

        // Keywords.
        AND, CLASS, ELSE, FALSE, FUN, FOR, IF, NIL, OR,
        PRINT, RETURN, SUPER, THIS, TRUE, VAR, WHILE,

        EOF
    }
}
