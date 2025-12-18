package cofty.core.semantics;

import cofty.core.parser.ast.*;
import org.jetbrains.annotations.NotNull;

public final class ModuleQuickScanner extends ModuleScanner {
    public ModuleQuickScanner(@NotNull ModuleContext context) {
        super(context);
    }

    @Override
    public boolean scan() {
        for (; index < currentBodyObject.residents().size(); index++) {
            final var residentObject = currentBodyObject.residents().get(index);

            switch (residentObject) {
                case ClassDeclarationObject classDeclarationObject -> {
                    final var classScope = scanClass(classDeclarationObject, true);

                    if (classScope != null)
                        diveInto(classScope, classDeclarationObject);
                }

                case FuncDeclarationObject funcDeclarationObject -> scanFunc(funcDeclarationObject, true);
                case FieldDeclarationObject fieldDeclarationObject -> quickScanFieldOrVariable(fieldDeclarationObject);

                default -> {}
            }

            tryStepOutScope();
        }

        return !isFailed;
    }
}
