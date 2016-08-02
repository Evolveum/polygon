/*
 * Copyright (c) 2010-2016 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
    
    public String getClearString() {
    	return new String(clearChars);
    }
}
