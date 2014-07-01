package com.evolveum.polygon.common;

import org.identityconnectors.common.security.GuardedString;

import java.util.Arrays;

/**
 * @author lazyman
 */
public class GuardedStringAccessor implements GuardedString.Accessor {

    private char[] clearChars;

    @Override
    public void access(char[] clearChars) {
        this.clearChars = Arrays.copyOf(clearChars, clearChars.length);
    }

    public char[] getClearChars() {
        return clearChars;
    }
}
