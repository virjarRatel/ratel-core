package com.virjar.ratel.api.extension.superappium.xpath.parser.expression.token.consumer;

import com.virjar.ratel.api.extension.superappium.xpath.parser.TokenQueue;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.token.Token;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.token.TokenConsumer;

public class StringConstantConsumer implements TokenConsumer {
    @Override
    public String consume(TokenQueue tokenQueue) {
        if (tokenQueue.matches("\'")) {
            return TokenQueue.unescape(tokenQueue.chompBalanced('\'', '\''));
        } else if (tokenQueue.matches("\"")) {
            return TokenQueue.unescape(tokenQueue.chompBalanced('\"', '\"'));
        }
        return null;
    }

    @Override
    public int order() {
        return 30;
    }

    @Override
    public String tokenType() {
        return Token.CONSTANT;
    }
}
