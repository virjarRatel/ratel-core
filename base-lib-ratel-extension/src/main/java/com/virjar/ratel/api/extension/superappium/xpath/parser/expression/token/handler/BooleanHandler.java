package com.virjar.ratel.api.extension.superappium.xpath.parser.expression.token.handler;

import com.virjar.ratel.api.inspect.Lists;
import com.virjar.ratel.api.extension.superappium.xpath.exception.XpathSyntaxErrorException;
import com.virjar.ratel.api.extension.superappium.xpath.function.FunctionEnv;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.SyntaxNode;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.node.FunctionNode;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.token.Token;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.token.TokenHandler;

import external.org.apache.commons.lang3.BooleanUtils;

public class BooleanHandler implements TokenHandler {
    @Override
    public SyntaxNode parseToken(final String tokenStr) throws XpathSyntaxErrorException {
        return new FunctionNode(FunctionEnv.getFilterFunction(BooleanUtils.toBoolean(tokenStr) ? "true" : "false"),
                Lists.<SyntaxNode>newLinkedList());
    }

    @Override
    public String typeName() {
        return Token.BOOLEAN;
    }
}
