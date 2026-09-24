package cofty.core.semantics.symbol.scope;

import cofty.core.semantics.symbol.ArgsSignature;
import cofty.core.semantics.symbol.CompletedFieldSymbol;
import cofty.core.semantics.symbol.ExternalFieldSymbol;
import cofty.core.semantics.symbol.TypeDescriptor;
import cofty.core.semantics.symbol.path.AbsSymbolPath;
import cofty.core.semantics.symbol.path.SymbolPath;
import cofty.util.Lists;
import cofty.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class RootScope extends ChildScope {
    public final static String NAME = "#root";
    public final AbsSymbolPath PATH = AbsSymbolPath.ROOT;

    private RootScope() {
        super(NAME);
    }

    @Override
    public void setParent(@NotNull Scope parent) {
        throw new IllegalCallerException();
    }

    @Override
    public @NotNull AbsSymbolPath absPath() {
        return PATH;
    }

    public static @NotNull RootScope create() {
        final var rootScope = new RootScope();

        loadExternalClass(rootScope, "java.io.PrintStream");
        loadExternalClass(rootScope, "java.lang.System");

        return rootScope;
    }

    private static void loadExternalClass(@NotNull RootScope scope, @NotNull String fullyQualifiedName) {
        var currentScope = (Scope)scope;

        try {
            final var clazz = Class.forName(fullyQualifiedName);
            final var relativeClassPath = SymbolPath.relative(fullyQualifiedName);
            final var newScope = new ExternalClassScope(relativeClassPath.parts.getLast());
            scope.put(newScope);

            currentScope = newScope;

            for (final var method : clazz.getMethods()) {
                if (!Modifier.isPublic(method.getModifiers())) continue;

                final var returnType = javaToCoftyType(method.getReturnType());

                final var args = new ArrayList<CompletedFieldSymbol>();

                for (final var arg: method.getParameters()) {
                    final var coftyType = javaToCoftyType(arg.getType());

                    args.add(new ExternalFieldSymbol(
                            arg.getName(),
                            coftyType,
                            !Modifier.isFinal(arg.getModifiers()),
                            false
                    ));
                }

                var funcSignaturesScope = (FuncSignaturesScope)currentScope.resolve(method.getName());

                if (funcSignaturesScope == null) {
                    funcSignaturesScope = new FuncSignaturesScope(method.getName());
                    currentScope.put(funcSignaturesScope);
                }

                funcSignaturesScope.put(new ExternalFuncScope(
                        method.getName(),
                        false,
                        args.isEmpty()
                                ? ArgsSignature.EMPTY_ABSOLUTE
                                : ArgsSignature.create(args, Lists.createList(null, args.size())),
                        returnType
                ));
            }

            for (final var field: clazz.getFields()) {
                if (!Modifier.isPublic(field.getModifiers())) continue;
                currentScope.put(new ExternalFieldSymbol(field.getName(), javaToCoftyType(field.getType()), !Modifier.isFinal(field.getModifiers()), true));
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static @NotNull TypeDescriptor<AbsSymbolPath> javaToCoftyType(@NotNull Class<?> javaType) {
        if (javaType.isArray()) {
            final var canonicalName = javaType.getCanonicalName();
            final var dimensions = Strings.count(canonicalName, "[]");

            var arrayType = javaType;

            while (arrayType.isArray())
                arrayType = arrayType.getComponentType();

            return TypeDescriptor.reference(SymbolPath.asAbsolute(
                    "cofty.lang." + "array<".repeat(dimensions)
                            + javaToCoftyType(arrayType)
                            + ">".repeat(dimensions)
            ));
        }

        final var coftyTypeName = switch (javaType.getName()) {
            case "java.lang.String" -> "str";
            case "java.lang.Ineteger" -> "int";
            case "int", "float", "double" -> javaType.getName();
            case "java.lang.Float", "java.lang.Double" ->  javaType.getName().toLowerCase();
            case "boolean", "java.lang.Boolean" -> "bool";
            case "void" -> "null";
            default -> null;
        };

        return TypeDescriptor.reference(SymbolPath.asAbsolute(
                coftyTypeName == null
                        ? (javaType.getName().equals("java.io.PrintStream") ? "PrintStream" : javaType.getName())
                        : "test." + coftyTypeName
        ));
    }
}
