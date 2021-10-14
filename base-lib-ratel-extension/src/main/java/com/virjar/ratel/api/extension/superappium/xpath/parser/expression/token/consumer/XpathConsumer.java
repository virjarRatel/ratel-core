package com.virjar.ratel.api.extension.superappium.xpath.parser.expression.token.consumer;

import com.virjar.ratel.api.extension.superappium.xpath.parser.TokenQueue;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.token.Token;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.token.TokenConsumer;

public class XpathConsumer implements TokenConsumer {
    @Override
    public String consume(TokenQueue tokenQueue) {
        // xpath子串
        if (tokenQueue.matches("`")) {
            return tokenQueue.chompBalanced('`', '`');
        }
        return null;
    }

    @Override
    public int order() {
        return 20;
    }

    @Override
    public String tokenType() {
        return Token.XPATH;
    }
}

