package com.virjar.ratel.api.extension.superappium.xpath.model;

import com.virjar.ratel.api.inspect.Lists;
import com.virjar.ratel.api.extension.superappium.ViewImage;
import com.virjar.ratel.api.extension.superappium.xpath.function.FunctionEnv;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.SyntaxNode;


public class Predicate {
    private SyntaxNode syntaxNode;
    private String predicateStr;

    public String getPredicateStr() {
        return predicateStr;
    }

    public Predicate(String predicateStr, SyntaxNode syntaxNode) {
        this.predicateStr = predicateStr;
        this.syntaxNode = syntaxNode;
    }

    boolean isValid(ViewImage element) {
        return (Boolean) FunctionEnv.getFilterFunction("sipSoupPredictJudge").call(element,
                Lists.newArrayList(syntaxNode));
    }
}
