package cofty.core.compiler.diagnostic;

import org.jetbrains.annotations.NotNull;

public enum Errors implements DiagnosticCode {
    LEXER_SYNTAX_ERROR(CodePrefix.LEXER_ERR, 1, Type.SYNTAX_ERROR, "invalid syntax"),
    PARSER_INVALID_SYNTAX(CodePrefix.PARSING_ERR, 1, Type.SYNTAX_ERROR, "invalid syntax"),
    NO_OPENED_BRACKETS(
            CodePrefix.PARSING_ERR,
            2,
            Type.SYNTAX_ERROR,
            "expected the start of {0} using the {1} bracket `{2}` here"
    ),
    UNCLOSED_BRACKETS(
            CodePrefix.PARSING_ERR,
            3,
            Type.SYNTAX_ERROR,
            "expected the end of {0} using the {1} bracket `{2}` here"
    ),
    NOT_ALLOWED(CodePrefix.PARSING_ERR, 4, Type.SYNTAX_ERROR, "not allowed here"),
    MISSING_SEPARATOR(
            CodePrefix.PARSING_ERR,
            5,
            Type.SYNTAX_ERROR,
            "expected a {0} separator here between the {1}"
    ),
    UNEXPECTED_SEPARATOR(
            CodePrefix.PARSING_ERR,
            6,
            Type.SYNTAX_ERROR,
            "expected {0} here, not the {1} separator"
    ),
    EXPECTED_NAME(CodePrefix.PARSING_ERR, 7, Type.SYNTAX_ERROR, "expected a {0} name here"),
    EXPECTED_FIELD_TYPE(
            CodePrefix.PARSING_ERR,
            8,
            Type.SYNTAX_ERROR,
            "expected a {0} value type here"
    ),
    EXPECTED_FUNC_RETURN_TYPE(
            CodePrefix.PARSING_ERR,
            9,
            Type.SYNTAX_ERROR,
            "expected a function return type here"
    ),
    EXPECTED_FUNC_RETURN_STATEMENT(
            CodePrefix.PARSING_ERR,
            10,
            Type.SYNTAX_ERROR,
            "expected the return statement"
    ),
    EXPECTED_ASSIGNMENT_TARGET(
            CodePrefix.PARSING_ERR,
            11,
            Type.SYNTAX_ERROR,
            "expected specified either the {0} value type or the {0} value itself"
    ),
    EXPECTED_ASSIGNABLE_FIELD(
            CodePrefix.PARSING_ERR,
            12,
            Type.SYNTAX_ERROR,
            "expected an assignable field, not the {0}"
    ),
    EXPECTED_ASSIGNABLE_VALUE(
            CodePrefix.PARSING_ERR,
            13,
            Type.SYNTAX_ERROR,
            "expected an assignable value here"
    ),
    MISSING_EXPLICIT_TYPE(
            CodePrefix.PARSING_ERR,
            14,
            Type.SYNTAX_ERROR,
            "expected an explicit specification of the {0} value type here, since the assigned value is not constant"
    ),
    EXPECTED_MEMBER_ACCESS(
            CodePrefix.PARSING_ERR,
            15,
            Type.SYNTAX_ERROR,
            "expected a field access or a function call here"
    ),
    DISALLOWED_OPERATOR_BEFORE_ASSIGN(
            CodePrefix.PARSING_ERR,
            16,
            Type.SYNTAX_ERROR,
            "operator `{0}` is not allowed here before the assignment"
    ),
    UNCLOSED_PARENTHESIS(
            CodePrefix.PARSING_ERR,
            17,
            Type.SYNTAX_ERROR,
            "parenthesis opened here but never closed"
    ),
    EXPECTED_BINARY_OPERATOR(
            CodePrefix.PARSING_ERR,
            18,
            Type.SYNTAX_ERROR,
            "expected a binary operator, not an opening parenthesis"
    ),
    EXPECTED_OPERAND_NOT_CLOSING_PARENT(
            CodePrefix.PARSING_ERR,
            19,
            Type.SYNTAX_ERROR,
            "expected an operand, not a closing parenthesis"
    ),
    INVALID_OPERATOR_FOR_CONTEXT(
            CodePrefix.PARSING_ERR,
            20,
            Type.SYNTAX_ERROR,
            "expected a contextually appropriate {0} operator, not the {1} operator `{2}`"
    ),
    EXPECTED_OPERAND(
            CodePrefix.PARSING_ERR,
            21,
            Type.SYNTAX_ERROR,
            "expected an operand"
    ),
    DUPLICATE_NAME(
            CodePrefix.SEMANTIC_ERR,
            1,
            Type.NAME_ERROR,
            "this name `{0}` is already defined as a {1} (see line {2})"
    ),
    DUPLICATE_FUNC_SIGNATURE(
            CodePrefix.SEMANTIC_ERR,
            2,
            Type.NAME_ERROR,
            "the function with those name and arguments signature has been already defined (see line {0})"
    ),
    NOT_A_TYPE(CodePrefix.SEMANTIC_ERR, 3, Type.TYPE_ERROR, "not a type"),
    NOT_A_VALUE(CodePrefix.SEMANTIC_ERR, 4, Type.VALUE_ERROR, "not a value (expected {0}, got {1})"),
    UNRESOLVED_REFERENCE(CodePrefix.SEMANTIC_ERR, 5, Type.NAME_ERROR, "does not exist"),
    INCOMPATIBLE_TYPES(
            CodePrefix.SEMANTIC_ERR,
            6,
            Type.TYPE_ERROR,
            "expected {0} value, got {1} value"
    );

    private final CodePrefix prefix;
    private final int codeNum;
    private final Type type;
    private final String placeholder;

    Errors(@NotNull CodePrefix prefix, int codeNum, @NotNull Type type, @NotNull String placeholder) {
        this.prefix = prefix;
        this.codeNum = codeNum;
        this.type = type;
        this.placeholder = placeholder;

        if (prefix.severity != DiagnosticMsg.Severity.ERR)
            throw new IllegalStateException();
    }

    @Override
    public @NotNull CodePrefix prefix() {
        return prefix;
    }

    @Override
    public int num() {
        return codeNum;
    }

    @Override
    public @NotNull String placeholder() {
        return placeholder;
    }

    public @NotNull Type type() {
        return type;
    }

    public enum Type {
        SYNTAX_ERROR,
        NAME_ERROR,
        TYPE_ERROR,
        VALUE_ERROR
    }
}
