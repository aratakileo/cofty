package cofty.core.semantics;

import cofty.core.compiler.diagnostic.Errors;
import cofty.core.lexer.token.TypedToken;
import cofty.core.parser.ast.*;
import cofty.core.parser.ast.value.ExpressionValueObject;
import cofty.core.parser.ast.value.complex.ComplexValueObject;
import cofty.core.parser.ast.value.complex.FieldAccessObject;
import cofty.core.parser.ast.value.complex.FuncCallObject;
import cofty.core.parser.ast.value.complex.SimpleValueObject;
import cofty.core.semantics.symbol.*;
import cofty.core.semantics.symbol.path.AbsSymbolPath;
import cofty.core.semantics.symbol.path.RelativeSymbolPath;
import cofty.core.semantics.symbol.path.SymbolPath;
import cofty.core.semantics.symbol.scope.*;
import cofty.core.semantics.symbol.TypeDescriptor;
import cofty.type.Representable;
import cofty.util.Cast;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public sealed abstract class ModuleScanner permits ModuleQuickScanner, ModuleDeepScanner {
    private final ArrayList<WithBody> bodyObjectsStack = new ArrayList<>();
    private final ArrayList<Integer> childIndexesStack = new ArrayList<>();
    private final ArrayList<Scope> scopesStack = new ArrayList<>();

    protected int index = 0;
    protected Scope currentScope;
    protected WithBody currentBodyObject;
    protected boolean isFailed = false;

    public final ModuleContext context;

    protected ModuleScanner(@NotNull ModuleContext context) {
        this.context = context;
        this.currentScope = context.scope;
        this.currentBodyObject = context.bodyObject;
    }

    public abstract boolean scan();

    protected @Nullable ClassScope scanClass(@NotNull ClassDeclarationObject classDeclarationObject, boolean quickScan) {
        final var resolvedSymbol = currentScope.resolveLocal(classDeclarationObject.name());

        if (resolvedSymbol != null && (quickScan || !(resolvedSymbol instanceof ClassScope))) {
            reportDuplicateName((NamedSymbol)resolvedSymbol, classDeclarationObject.nameToken());

            isFailed = true;
            return null;
        }

        if (resolvedSymbol != null)
            return (ClassScope)resolvedSymbol;

        final var classScope = new ClassScope(classDeclarationObject.name);
        currentScope.put(classScope);
        return classScope;
    }

    protected @Nullable FuncScope<?> scanFunc(@NotNull FuncDeclarationObject funcDeclarationObject, boolean quickScan) {
        final var resolvedSymbol = currentScope.resolveLocal(funcDeclarationObject.name());

        if (resolvedSymbol != null && !(resolvedSymbol instanceof FuncSignaturesScope)) {
            reportDuplicateName((NamedSymbol)resolvedSymbol, funcDeclarationObject.nameToken());
            isFailed = true;
            return null;
        }

        final var signaturesScopeIsNew = resolvedSymbol == null;

        final var funcSignaturesScope = signaturesScopeIsNew
                ? new FuncSignaturesScope(funcDeclarationObject.nameToken())
                : (FuncSignaturesScope)resolvedSymbol;

        final var resolvedFuncScope = !signaturesScopeIsNew
                ? (FuncScope<?>)funcSignaturesScope.resolve(funcDeclarationObject.argsSignature().minimumSignature)
                : null;

        if (signaturesScopeIsNew)
            currentScope.put(funcSignaturesScope);
        else if (!quickScan && resolvedFuncScope instanceof CompletedFuncScope) return resolvedFuncScope;

        final var signatureIsDuplicate = !signaturesScopeIsNew
                && resolvedFuncScope instanceof IncompletedFuncScope
                && quickScan ;

        if (signatureIsDuplicate) {
            reportDuplicateFuncSignature(funcDeclarationObject, resolvedFuncScope);
            isFailed = true;
            return null;
        }

        final var initialScope = currentScope;

        currentScope = funcSignaturesScope;

        if (resolvedFuncScope == null) {
            final var args = new HashMap<String, IncompletedFieldSymbol>();

            for (final var argObject: funcDeclarationObject.args) {
                final var incompletedArg = (IncompletedFieldSymbol)scanFuncArg(argObject, true);

                if (incompletedArg == null) {
                    isFailed = true;
                    currentScope = initialScope;
                    return null;
                }

                if (args.containsKey(incompletedArg.name())) {
                    reportDuplicateName(args.get(incompletedArg.name()), SymbolType.ARGUMENT, incompletedArg.nameToken());
                    isFailed = true;
                    currentScope = initialScope;
                    return null;
                }

                args.put(incompletedArg.name(), incompletedArg);
            }

            currentScope = initialScope;

            final var incompletedFuncScope = IncompletedFuncScope.create(funcDeclarationObject);

            funcSignaturesScope.put(incompletedFuncScope);

            return incompletedFuncScope;
        }

        final var returnTypeResolveResult = currentScope.resolveTypePath(
                resolvedFuncScope.valueType() != null
                        ? (RelativeSymbolPath) resolvedFuncScope.valueTypeOrThrow().path
                        : TypeDescriptor.RELATIVE_NULL.path
        );

        if (returnTypeResolveResult.isErr()) {
            context.messages.reportRange(
                    Objects.requireNonNull(funcDeclarationObject.returnType).name,
                    returnTypeResolveResult.unwrapErr()
            );

            isFailed = true;
            currentScope = initialScope;
            return null;
        }

        final var args = new ArrayList<CompletedFieldSymbol>();

        for (final var argObject :funcDeclarationObject.args) {
            final var completedArg = (CompletedFieldSymbol)scanFuncArg(argObject, false);

            if (completedArg != null) {
                args.add(completedArg);
                continue;
            }

            isFailed = true;
            currentScope = initialScope;
            return null;
        }

        currentScope = initialScope;

        final var completedFuncScope = new CompletedFuncScope(
                funcDeclarationObject.nameToken(),
                ArgsSignature.create(
                        args,
                        funcDeclarationObject.args.stream().map(arg -> arg.value).toList()
                ),
                TypeDescriptor.reference(returnTypeResolveResult.unwrap())
        );

        funcSignaturesScope.removeSignature(Cast.<ArgsSignature<?>, ArgsSignature<RelativeSymbolPath>>quiet(
                resolvedFuncScope.argsSignature
        ));

        funcSignaturesScope.put(completedFuncScope);

        return completedFuncScope;
    }

    protected void quickScanFieldOrVariable(
            @NotNull FieldDeclarationObject fieldDeclarationObject
    ) {
        scanField(fieldDeclarationObject, true, true, true);
    }

    protected @Nullable FieldSymbol<?> deepScanFieldOrVariable(
            @NotNull FieldDeclarationObject fieldDeclarationObject,
            boolean isCompletedInsideOfMainCycle
    ) {
        return scanField(fieldDeclarationObject, false, true, isCompletedInsideOfMainCycle);
    }

    private @Nullable FieldSymbol<?> scanFuncArg(
            @NotNull FieldDeclarationObject fieldDeclarationObject,
            boolean quickScan
    ) {
        return scanField(fieldDeclarationObject, quickScan, false, true);
    }

    private @Nullable FieldSymbol<?> scanField(
            @NotNull FieldDeclarationObject fieldDeclarationObject,
            boolean quickScan,
            boolean putInScope,
            boolean isCompletedInsideOfMainCycle
    ) {
        final var resolvedSymbol = currentScope.resolveLocal(fieldDeclarationObject.name());

        final var isCompletedSymbolDuplicate = resolvedSymbol instanceof CompletedFieldSymbol completedFieldSymbol
                && completedFieldSymbol.isCompletedInsideOfMainCycle();

        final var isIncompletedSymbolDuplicate = resolvedSymbol != null && quickScan;

        if (isIncompletedSymbolDuplicate || isCompletedSymbolDuplicate) {
            reportDuplicateName((NamedSymbol)resolvedSymbol, fieldDeclarationObject.nameToken());
            isFailed = true;
            return null;
        }

        if (resolvedSymbol instanceof CompletedFieldSymbol completedFieldSymbol) {
            completedFieldSymbol.markAsCompletedInsideOfMainCycle();
            return completedFieldSymbol;
        }

        if (quickScan) {
            final var incompletedFieldSymbol = IncompletedFieldSymbol.create(fieldDeclarationObject);

            if (putInScope) currentScope.put(incompletedFieldSymbol);

            return incompletedFieldSymbol;
        }

        final var resolvedType = resolveType(
                fieldDeclarationObject.valueType,
                fieldDeclarationObject.value
        );

        if (resolvedType == null) {
            isFailed = true;
            return null;
        }

        final var completedFieldSymbol = CompletedFieldSymbol.create(
                fieldDeclarationObject,
                resolvedType,
                isCompletedInsideOfMainCycle
        );

        if (putInScope)
            currentScope.put(completedFieldSymbol);

        return completedFieldSymbol;
    }

    protected @Nullable TypeDescriptor<AbsSymbolPath> resolveType(
            @Nullable TypeDescriptorObject type,
            @Nullable ExpressionValueObject value
    ) {
        if (type == null && value == null)
            throw new IllegalArgumentException();

        var resolvedExplicitType = (TypeDescriptor<AbsSymbolPath>)null;
        var resolvedValueType = (TypeDescriptor<AbsSymbolPath>)null;

        if (type != null) {
            final var typePath = SymbolPath.rawRelative(type.name);
            final var valueTypeResolveResult = currentScope.resolveTypePath(typePath);

            if (valueTypeResolveResult.isErr()) {
                context.messages.reportRange(type.name, valueTypeResolveResult.unwrapErr());
                return null;
            }

            resolvedExplicitType = TypeDescriptor.reference(valueTypeResolveResult.unwrap());
        }

        if (value != null) {
            resolvedValueType = resolveType(value);
            if (resolvedValueType == null) return null;
        }

        if (resolvedExplicitType != null && resolvedValueType != null) {
            if (!resolvedExplicitType.equals(resolvedValueType)) {
                context.messages.reportRange(
                        value.failTokensRange(),
                        Errors.INCOMPATIBLE_TYPES,
                        resolvedExplicitType,
                        resolvedValueType
                );

                return null;
            }

            return resolvedExplicitType;
        }

        if (resolvedExplicitType != null)
            return resolvedExplicitType;

        return resolvedValueType;
    }

    protected @Nullable TypeDescriptor<AbsSymbolPath> resolveType(@NotNull ExpressionValueObject value) {
        if (value instanceof SimpleValueObject simpleValueObject)
            return TypeDescriptor.reference(
                    currentScope.resolveOrThrow(simpleValueObject.valueTypeName()).absPath()
            );

        final var resolvedValue = resolveValue(value);

        if (resolvedValue == null)
            return null;

        if (resolvedValue instanceof CompletedFieldSymbol completedFieldSymbol && !completedFieldSymbol.isValuePassed()) {
            context.messages.report(
                    value instanceof FieldAccessObject fieldAccessObject
                            ? fieldAccessObject.nameToken()
                            : value.lastFailToken(),

                    Errors.UNINITIALIZED_FIELD_ACCESS,
                    SymbolType.of(completedFieldSymbol),
                    completedFieldSymbol.name()
            );

            return null;
        }

        return Cast.<Symbol, WithValueType<AbsSymbolPath>>quiet(resolvedValue).valueTypeOrThrow();
    }

    protected @Nullable Symbol resolveValue(@NotNull ExpressionValueObject value) {
        final var valueSegments = value instanceof ComplexValueObject complexValueObject
                ? complexValueObject.segments : List.of(value);

        final var initialScope = currentScope;

        var resolvedSymbol = (Symbol)null;

        for (final var currentValueSegment: valueSegments) {
            switch (currentValueSegment) {
                case SimpleValueObject _ -> throw new IllegalArgumentException();
                case FieldAccessObject fieldAccessObject -> {
                    resolvedSymbol = resolveSymbol(fieldAccessObject);

                    if (resolvedSymbol == null) {
                        currentScope = initialScope;
                        return null;
                    }

                    if (resolvedSymbol instanceof ClassScope classScope) {
                        currentScope = classScope;
                        continue;
                    }

                    currentScope = (Scope)currentScope.resolve(
                            ((CompletedFieldSymbol)resolvedSymbol).valueTypeOrThrow().path
                    );
                }

                case FuncCallObject funcCallObject -> {
                    resolvedSymbol = resolveScope(funcCallObject);

                    if (resolvedSymbol == null) {
                        currentScope = initialScope;
                        return null;
                    }

                    currentScope = (Scope)currentScope.resolve(
                            ((CompletedFuncScope)resolvedSymbol).valueTypeOrThrow().path
                    );
                }

                default -> throw new IllegalStateException();
            }
        }

        if (resolvedSymbol instanceof ClassScope classScope) {
            context.messages.report(
                    classScope.nameToken(),
                    Errors.NOT_A_VALUE,
                    SymbolType.FIELD,
                    SymbolType.CLASS
            );

            currentScope = initialScope;
            return null;
        }

        currentScope = initialScope;

        return resolvedSymbol;
    }

    private @Nullable Scope resolveScope(@NotNull FuncCallObject funcCallObject) {
        final var resolvedFuncSignaturesScope = currentScope.resolveLocal(funcCallObject.name());

        if (isNotDefined(funcCallObject, resolvedFuncSignaturesScope)) return null;
        if (isNotInstanceof((NamedSymbol)resolvedFuncSignaturesScope, SymbolType.FUNCTION)) return null;

        var resolvedFuncScope = (FuncScope<?>)null;

        if (!funcCallObject.args.isEmpty()) {
            final var passingArgsSignature = new ArrayList<TypeDescriptor<AbsSymbolPath>>();

            for (final var argValue: funcCallObject.args) {
                final var passingType = resolveType(argValue);

                if (passingType == null)
                    return null;

                passingArgsSignature.add(passingType);
            }

            final var funcSignatureScope = ((FuncSignaturesScope)resolvedFuncSignaturesScope);

            if (passingArgsSignature.size() > funcSignatureScope.maxArgumentsCount()) {
                final var invalidArguments = funcCallObject.args.subList(
                        funcSignatureScope.maxArgumentsCount(),
                        passingArgsSignature.size()
                );

                context.messages.reportRange(
                        invalidArguments.getFirst().firstFailToken(),
                        invalidArguments.getLast().lastFailToken(),
                        Errors.ARGUMENTS_COUNT_MISMATCH,
                        funcCallObject.name(),
                        funcSignatureScope.maxArgumentsCount(),
                        passingArgsSignature.size()
                );
                return null;
            }

            if (passingArgsSignature.size() < funcSignatureScope.minArgumentsCount()) {
                context.messages.reportRange(
                        funcCallObject.args.getFirst().firstFailToken(),
                        funcCallObject.args.getLast().lastFailToken(),
                        Errors.NOT_ENOUGH_ARGUMENTS,
                        funcCallObject.name(),
                        funcSignatureScope.minArgumentsCount(),
                        passingArgsSignature.size()
                );
                return null;
            }

            resolvedFuncScope = (FuncScope<?>)funcSignatureScope.resolve(ArgsSignature.viewOf(passingArgsSignature));

            if (resolvedFuncScope == null) {
                final var resolveResult = funcSignatureScope.resolveByArgsSignature(passingArgsSignature);

                if (resolveResult.isErr()) {
                    final var incompatibleArgInfo = resolveResult.unwrapErr();

                    if (passingArgsSignature.size() < incompatibleArgInfo.func.argsSignature.requiredArgsCount) {
                        context.messages.reportRange(
                                funcCallObject.args.getFirst().firstFailToken(),
                                funcCallObject.args.getLast().lastFailToken(),
                                Errors.NOT_ENOUGH_ARGUMENTS,
                                funcCallObject.name(),
                                incompatibleArgInfo.func.argsSignature.requiredArgsCount,
                                passingArgsSignature.size()
                        );
                        return null;
                    }

                    context.messages.reportRange(
                            funcCallObject.args.get(incompatibleArgInfo.invalidArgIndex).failTokensRange(),
                            Errors.ARGUMENT_TYPE_MISMATCH,
                            incompatibleArgInfo.invalidArgIndex + 1,
                            incompatibleArgInfo.func.name(),
                            incompatibleArgInfo.func.argsSignature.getType(incompatibleArgInfo.invalidArgIndex),
                            passingArgsSignature.get(incompatibleArgInfo.invalidArgIndex)
                    );
                    return null;
                }

                resolvedFuncScope = resolveResult.unwrap();
            }
        } else resolvedFuncScope = ((FuncSignaturesScope)resolvedFuncSignaturesScope).resolveByEmptyArgsSignature();

        if (resolvedFuncScope == null) {
            context.messages.reportRange(
                    funcCallObject.failTokensRange(),
                    Errors.NOT_ENOUGH_ARGUMENTS,
                    funcCallObject.name(),
                    ((FuncSignaturesScope)resolvedFuncSignaturesScope).minArgumentsCount(),
                    0
            );

            return null;
        }

        if (resolvedFuncScope instanceof IncompletedFuncScope incompletedFuncScope) {
            final var completedFuncScope = scanFunc(incompletedFuncScope.basedOn(), false);

            if (completedFuncScope == null) return null;

            resolvedFuncScope = completedFuncScope;
        }

        return Objects.requireNonNull(resolvedFuncScope);
    }

    private @Nullable Symbol resolveSymbol(@NotNull FieldAccessObject fieldAccessObject) {
        var resolvedSymbol = currentScope.resolveLocal(fieldAccessObject.name());

        if (isNotDefined(fieldAccessObject, resolvedSymbol)) return null;
        if (resolvedSymbol instanceof ClassScope) return resolvedSymbol;
        if (isNotInstanceof((NamedSymbol)resolvedSymbol, SymbolType.FIELD)) return null;

        if (resolvedSymbol instanceof IncompletedFieldSymbol incompletedFieldSymbol) {
            final var completedFieldSymbol = deepScanFieldOrVariable(
                    incompletedFieldSymbol.basedOn(),
                    false
            );

            if (completedFieldSymbol == null) return null;

            resolvedSymbol = completedFieldSymbol;
        }

        return resolvedSymbol;
    }

    protected void diveInto(@NotNull Scope scope, @NotNull WithBody bodyObject) {
        if (bodyObject.residents().isEmpty()) return;

        bodyObjectsStack.add(currentBodyObject);
        childIndexesStack.add(index);
        scopesStack.add(currentScope);

        currentScope = scope;
        currentBodyObject = bodyObject;
        index = -1;
    }

    protected boolean tryStepOutScope() {
        var steppedOutAtLeastOnce = false;

        while (index == currentBodyObject.residents().size() - 1 && !scopesStack.isEmpty()) {
            index = childIndexesStack.removeLast();
            currentBodyObject = bodyObjectsStack.removeLast();
            currentScope = scopesStack.removeLast();
            steppedOutAtLeastOnce = true;
        }

        return steppedOutAtLeastOnce;
    }

    protected void reportDuplicateName(
            @NotNull NamedSymbol firstDeclaration,
            @NotNull TypedToken<?> newName
    ) {
        reportDuplicateName(firstDeclaration, null, newName);
    }

    protected void reportDuplicateName(
            @NotNull NamedSymbol firstDeclaration,
            @Nullable SymbolType firstDeclarationType,
            @NotNull TypedToken<?> newName
    ) {
        context.messages.report(
                newName,
                Errors.DUPLICATE_NAME,
                firstDeclaration.name(),
                firstDeclarationType == null ? SymbolType.of(firstDeclaration) : firstDeclarationType,
                firstDeclaration.nameToken().getLineNumber(context.text)
        );
    }

    private void reportDuplicateFuncSignature(
            @NotNull FuncDeclarationObject newDeclaration,
            @NotNull FuncScope<?> firstDeclaration
    ) {
        context.messages.reportRange(
                newDeclaration.argsSignatureRange(),
                Errors.DUPLICATE_FUNC_SIGNATURE,
                firstDeclaration.name(),
                Representable.reprAsTuple(firstDeclaration.argsSignature.types()),
                firstDeclaration.nameToken().getLineNumber(context.text)
        );
    }

    private boolean isNotDefined(@NotNull WithName withName, @Nullable Symbol resolvedSymbol) {
        if (resolvedSymbol != null) return false;

        context.messages.report(withName.nameToken(), Errors.UNRESOLVED_REFERENCE);
        return true;
    }

    private boolean isNotInstanceof(@NotNull NamedSymbol symbol, @NotNull SymbolType expectedInstance) {
        if (expectedInstance.isinstance(symbol)) return false;

        context.messages.report(symbol.nameToken(), Errors.NOT_A_VALUE, expectedInstance, SymbolType.of(symbol));
        return true;
    }
}
