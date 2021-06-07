/*
 * YUI Compressor
 * Author: Julien Lecomte <jlecomte@yahoo-inc.com>
 * Copyright (c) 2007, Yahoo! Inc. All rights reserved.
 * Code licensed under the BSD License:
 *     http://developer.yahoo.net/yui/license.txt
 */

package com.yahoo.platform.yui.compressor;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

@SuppressWarnings( { "unchecked", "rawtypes"} )
class ScriptOrFnScope {

    private int braceNesting;
    private ScriptOrFnScope parentScope;
    private ArrayList subScopes;
    private Hashtable identifiers = new Hashtable();
    private Hashtable hints = new Hashtable();
    private boolean markedForMunging = true;
    private int varcount = 0;

    ScriptOrFnScope(int braceNesting, ScriptOrFnScope parentScope) {
        this.braceNesting = braceNesting;
        this.parentScope = parentScope;
        this.subScopes = new ArrayList();
        if (parentScope != null) {
            parentScope.subScopes.add(this);
        }
    }

    int getBraceNesting() {
        return braceNesting;
    }

    ScriptOrFnScope getParentScope() {
        return parentScope;
    }

    JavaScriptIdentifier declareIdentifier(String symbol) {
        JavaScriptIdentifier identifier = (JavaScriptIdentifier) identifiers.get(symbol);
        if (identifier == null) {
            identifier = new JavaScriptIdentifier(symbol, this);
            identifiers.put(symbol, identifier);
        }
        return identifier;
    }

    JavaScriptIdentifier getIdentifier(String symbol) {
        return (JavaScriptIdentifier) identifiers.get(symbol);
    }

    void addHint(String variableName, String variableType) {
        hints.put(variableName, variableType);
    }

    void preventMunging() {
        if (parentScope != null) {
            // The symbols in the global scope don't get munged,
            // but the sub-scopes it contains do get munged.
            markedForMunging = false;
        }
    }

    private ArrayList getUsedSymbols() {
        ArrayList result = new ArrayList();
        Enumeration elements = identifiers.elements();
        while (elements.hasMoreElements()) {
            JavaScriptIdentifier identifier = (JavaScriptIdentifier) elements.nextElement();
            String mungedValue = identifier.getMungedValue();
            if (mungedValue == null) {
                mungedValue = identifier.getValue();
            }
            result.add(mungedValue);
        }
        return result;
    }

    private ArrayList getAllUsedSymbols() {
        ArrayList result = new ArrayList();
        ScriptOrFnScope scope = this;
        while (scope != null) {
            result.addAll(scope.getUsedSymbols());
            scope = scope.parentScope;
        }
        return result;
    }

    int incrementVarCount() {
        varcount++;
        return varcount;
    }

    void munge() {

        if (!markedForMunging) {
            // Stop right here if this scope was flagged as unsafe for munging.
            return;
        }

        int pickFromSet = 1;

        // Do not munge symbols in the global scope!
        if (parentScope != null) {

            ArrayList freeSymbols = new ArrayList();

            freeSymbols.addAll(JavaScriptCompressor.ones);
            freeSymbols.removeAll(getAllUsedSymbols());
            if (freeSymbols.size() == 0) {
                pickFromSet = 2;
                freeSymbols.addAll(JavaScriptCompressor.twos);
                freeSymbols.removeAll(getAllUsedSymbols());
            }
            if (freeSymbols.size() == 0) {
                pickFromSet = 3;
                freeSymbols.addAll(JavaScriptCompressor.threes);
                freeSymbols.removeAll(getAllUsedSymbols());
            }
            if (freeSymbols.size() == 0) {
                throw new IllegalStateException("The YUI Compressor ran out of symbols. Aborting...");
            }

            Enumeration elements = identifiers.elements();
            while (elements.hasMoreElements()) {
                if (freeSymbols.size() == 0) {
                    pickFromSet++;
                    if (pickFromSet == 2) {
                        freeSymbols.addAll(JavaScriptCompressor.twos);
                    } else if (pickFromSet == 3) {
                        freeSymbols.addAll(JavaScriptCompressor.threes);
                    } else {
                        throw new IllegalStateException("The YUI Compressor ran out of symbols. Aborting...");
                    }
                    // It is essential to remove the symbols already used in
                    // the containing scopes, or some of the variables declared
                    // in the containing scopes will be redeclared, which can
                    // lead to errors.
                    freeSymbols.removeAll(getAllUsedSymbols());
                }

                String mungedValue;
                JavaScriptIdentifier identifier = (JavaScriptIdentifier) elements.nextElement();
                if (identifier.isMarkedForMunging()) {
                    mungedValue = (String) freeSymbols.remove(0);
                } else {
                    mungedValue = identifier.getValue();
                }
                identifier.setMungedValue(mungedValue);
            }
        }

        for (int i = 0; i < subScopes.size(); i++) {
            ScriptOrFnScope scope = (ScriptOrFnScope) subScopes.get(i);
            scope.munge();
        }
    }
}
