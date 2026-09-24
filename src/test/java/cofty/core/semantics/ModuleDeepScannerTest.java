package cofty.core.semantics;

import cofty.core.compiler.diagnostic.Errors;
import cofty.core.semantics.symbol.scope.ClassScope;
import org.junit.jupiter.api.Test;

class ModuleDeepScannerTest {
    @Test
    void validFieldDeclarationTests() {
        final var scope = SemanticAssert.fullScan("""
                var mut var1: int # mutable var with no value
                var var2: int # immutable var with value
                var var3 = true # immutable var with no type and with value
                var mut var4 = 2.3 # mutable var with no type and with value
                var var5 = getHelloWorld() # immutable var with no type and value from func call before func declaration
                
                fun getHelloWorld() -> str { return 'Hello world!' }
                
                var mut var6 = getHelloWorld() # mutable var with no type and value from func call after func declaration
                var var7 = state.ok()
                
                class state {
                    fun ok() -> bool {return true}
                }
                
                fun someFunc() {
                    var value = 4
                }
                """).ok()
                .hasNoDiagnosticMessages()
                .scope()
                .containsChildren(SemanticAssert.BUILTINS_CLASSES + 10);

        scope.completedField("var1").valueNotPassed().mutable().checkPossibleType("int");
        scope.completedField("var2").valueNotPassed().immutable().checkPossibleType("int");
        scope.completedField("var3").valuePassed().immutable().checkPossibleType("bool");
        scope.completedField("var4").valuePassed().mutable().checkPossibleType("double");
        scope.completedField("var5").valuePassed().immutable().checkPossibleType("str");
        scope.completedField("var6").valuePassed().mutable().checkPossibleType("str");
        scope.completedField("var7").valuePassed().immutable().checkPossibleType("bool");

        scope.completedPossibleFuncScope("someFunc")
                .completedField("value")
                .valuePassed()
                .immutable()
                .checkPossibleType("int");
    }

    @Test
    void validFieldAssignmentTests() {
        final var scope = SemanticAssert.fullScan("""
                var var1: int
                var1 = 51 # correct assignment to immutable uninitialized var
                
                var mut var2 = 69
                var2 = 999 # correct assignment to mutable initialized var
                
                state.isOk = true # correct assignment mutable field initialisation
                
                class state {
                    var mut isOk = false
                }
                """).ok()
                .hasNoDiagnosticMessages()
                .scope()
                .containsChildren(SemanticAssert.BUILTINS_CLASSES + 3);

        scope.completedField("var1").valuePassed().immutable().checkPossibleType("int");
        scope.completedField("var2").valuePassed().mutable().checkPossibleType("int");

        scope.scope(ClassScope.class, "state")
                .containsChildren(1)
                .completedField("isOk")
                .valuePassed()
                .mutable()
                .checkPossibleType("bool");
    }

    @Test
    void validFuncTests() {
        final var scope = SemanticAssert.fullScan("""
                        fun fun1() {}
                        fun fun2() {return}
                        
                        fun fun3() -> str {return 'hello'}
                        fun fun3(a: int, b: str = 'hello') {}
                        
                        fun3()
                        fun3(69)
                        fun3(69, "Oooopsie! I'm overriding the default one argument")
                        
                        class Container {
                            fun fun1(num: int) {
                               fun1(num)
                               Container.fun1(num)
                           }
                        }
                        """)
                .ok()
                .hasNoDiagnosticMessages()
                .scope()
                .containsChildren(SemanticAssert.BUILTINS_CLASSES + 4);

        scope.completedPossibleFuncScope("fun1").containsChildren(0);
        scope.completedPossibleFuncScope("fun2").containsChildren(0);
        scope.completedPossibleFuncScope("fun3").containsChildren(0);

        var funcScope = scope.completedPossibleFuncScope("fun3", "int")
                .containsChildren(2);

        funcScope.completedField("a").checkPossibleType("int").valueNotPassed().immutable();
        funcScope.completedField("b").checkPossibleType("str").valuePassed().immutable();
    }

    @Test
    void invalidAssignmentTests() {
        SemanticAssert.fullScan("""
                var var1 = 66
                var1 = 77          # [fail 1]
                
                var mut var2 = 69
                var2 = '69'        # [fail 2]
                
                var var3: int
                var var4 = var3    # [fail 3]
                var3 = var3        # [fail 4]
                var3 = 34
                var3 = 72          # [fail 5]
                
                var var5: var3     # [fail 6]
                var var6: nothing  # [fail 7]
                var var7 = var0    # [fail 8]
                
                class gg {}
                
                fun gg2() {
                    var value: bool
                    var value: str # [fail 9]
                }
                
                var var8 = gg      # [fail 10]
                var var9 = gg2     # [fail 11]
                """
        ).failed().hasErrors(
                Errors.IMMUTABLE_REASSIGNMENT,     // [fail 1]
                Errors.INCOMPATIBLE_TYPES,         // [fail 2]
                Errors.UNINITIALIZED_FIELD_ACCESS, // [fail 3]
                Errors.UNINITIALIZED_FIELD_ACCESS, // [fail 4]
                Errors.IMMUTABLE_REASSIGNMENT,     // [fail 5]
                Errors.NOT_A_TYPE,                 // [fail 6]
                Errors.UNRESOLVED_REFERENCE,       // [fail 7]
                Errors.UNRESOLVED_REFERENCE,       // [fail 8]
                Errors.DUPLICATE_NAME,             // [fail 9]
                Errors.NOT_A_VALUE,                // [fail 10]
                Errors.NOT_A_VALUE                 // [fail 11]
        );
    }

    @Test
    void invalidFuncTests() {
        SemanticAssert.fullScan("""
                        fun validFunc(immutableArg = 69) {
                            immutableArg = 77             # [fail 1]
                        }
                        
                        fun validFunc2() {
                            return "Must not return this" # [fail 2]
                        }
                        
                        fun validFunc3() -> int {
                            return "Must not return this" # [fail 3]
                        }
                        
                        fun validFunc4() -> int {
                            return                        # [fail 4]
                        }
                        
                        fun validFunc5(a: int, b: int, c = 'Hello') {}
                        
                        validFunc5()                      # [fail 5]
                        validFunc5('test')                # [fail 6]
                        validFunc5(999, 'test')           # [fail 7]
                        validFunc5(999, 777, false)       # [fail 8]
                        validFunc5(999, 777, 'Hi', false) # [fail 9]
                        
                        fun validFunc6() {}
                        fun validFunc6(a: int, b: int, c = 'Hello') {}
                        
                        validFunc6('test')                # [fail 10]
                        validFunc6('test', 53)            # [fail 11]
                        """
        ).failed().hasErrors(
                Errors.IMMUTABLE_REASSIGNMENT,   // [fail 1]
                Errors.UNEXPECTED_RETURN_VALUE,  // [fail 2]
                Errors.RETURN_TYPE_MISMATCH,     // [fail 3]
                Errors.MISSING_RETURN_VALUE,     // [fail 4]
                Errors.NOT_ENOUGH_ARGUMENTS,     // [fail 5]
                Errors.NOT_ENOUGH_ARGUMENTS,     // [fail 6]
                Errors.ARGUMENT_TYPE_MISMATCH,   // [fail 7]
                Errors.ARGUMENT_TYPE_MISMATCH,   // [fail 8]
                Errors.ARGUMENTS_COUNT_MISMATCH, // [fail 9]
                Errors.NOT_ENOUGH_ARGUMENTS,     // [fail 10]
                Errors.ARGUMENT_TYPE_MISMATCH    // [fail 11]
        );
    }
}