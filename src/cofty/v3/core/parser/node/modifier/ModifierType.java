package cofty.v3.core.parser.node.modifier;

import cofty.type.Containable;

public enum ModifierType implements Containable<ModifierType> {
    ACTION,
    DEPENDED,
    FAIL,
    GENERAL,
    PEEK,
    PREVIEW;

    public boolean isBasic() {
        return isIn(GENERAL, PEEK, FAIL);
    }
}
