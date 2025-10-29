package cofty.core.parser.node.modifier;

import cofty.type.Containable;

@Deprecated
public enum ModifierType implements Containable<ModifierType> {
    CONSUME,
    DEPENDED,
    FAIL,
    GENERAL,
    PEEK,
    PREVIEW;

    public boolean isBasic() {
        return isIn(GENERAL, PEEK, FAIL);
    }
}
