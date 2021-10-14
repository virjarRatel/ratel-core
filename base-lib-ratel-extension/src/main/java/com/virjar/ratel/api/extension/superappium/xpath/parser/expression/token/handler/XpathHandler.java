package com.virjar.ratel.api.extension.superappium.xpath.parser.expression.token.handler;

import com.virjar.ratel.api.extension.superappium.ViewImage;
import com.virjar.ratel.api.extension.superappium.xpath.XpathParser;
import com.virjar.ratel.api.extension.superappium.xpath.exception.XpathSyntaxErrorException;
import com.virjar.ratel.api.extension.superappium.xpath.model.XNode;
import com.virjar.ratel.api.extension.superappium.xpath.model.XNodes;
import com.virjar.ratel.api.extension.superappium.xpath.model.XpathEvaluator;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.SyntaxNode;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.token.Token;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.token.TokenHandler;

public class XpathHandler implements TokenHandler {
    @Override
    public SyntaxNode parseToken(String tokenStr) throws XpathSyntaxErrorException {
        final XpathEvaluator xpathEvaluator = new XpathParser(tokenStr).parse();
        return new SyntaxNode() {
            @Override
            public Object calc(ViewImage element) {
                return xpathEvaluator.evaluate(new XNodes(XNode.e(element)));
            }
        };
    }

    @Override
    public String typeName() {
        return Token.XPATH;
    }
}
