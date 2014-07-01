package com.evolveum.polygon.common;

import org.identityconnectors.common.security.GuardedString;

/**
 * @author lazyman
 */
public class GuardedStringAccessor implements GuardedString.Accessor {

    private char[] clearChars;

    @Override
    public void access(char[] clearChars) {
        this.clearChars = clearChars;
    }

    public char[] getClearChars() {
        return clearChars;
    }
}
