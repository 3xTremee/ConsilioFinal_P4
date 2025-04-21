package org.example.semantic;

import java.util.*;

public class SymbolTable {
    /**
     * Stack of scopes; each scope is a map from identifier → SymbolInfo.
     * scopes.peek() is the “current” (innermost) scope.
     */
    private final Deque<Map<String,SymbolInfo>> scopes = new ArrayDeque<>();

    public SymbolTable() {
        openScope();          // global scope
    }

    /** Start a new (inner) scope. */
    public void openScope() {
        scopes.push(new HashMap<>());
    }

    /** Pop the current scope off the stack. */
    public void exitScope() {
        if (scopes.size() == 1)
            throw new IllegalStateException("Can’t pop the global scope");
        scopes.pop();
    }

    /**
     * Declare a new symbol in *this* scope.
     * @throws SemanticException if name already exists in current scope.
     */
    public void enterSymbol(String name, SymbolInfo info) {
        Map<String,SymbolInfo> current = scopes.peek();
        if (current != null){
            if (current.containsKey(name))
                throw new SemanticException("Duplicate declaration of '" + name + "'");
            current.put(name, info);
        }
    }

    /**
     * Look up a symbol by walking from inner→outer scopes.
     * @throws SemanticException if name is undefined.
     */
    public SymbolInfo retrieveSymbol(String name) {
        for (Map<String,SymbolInfo> scope : scopes) {
            if (scope.containsKey(name))
                return scope.get(name);
        }
        throw new SemanticException("Undefined identifier '" + name + "'");
    }

    /** Only checks the *current* scope (no fallback). */
    public boolean isInCurrentScope(String name) {
        return scopes.peek().containsKey(name);
    }
}
