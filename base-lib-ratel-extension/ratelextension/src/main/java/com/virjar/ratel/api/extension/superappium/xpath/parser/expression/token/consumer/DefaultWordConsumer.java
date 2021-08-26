package com.virjar.ratel.api.extension.superappium.xpath.parser.expression.token.consumer;

import com.virjar.ratel.api.extension.superappium.xpath.parser.TokenQueue;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.token.Token;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.token.TokenConsumer;

public class DefaultWordConsumer implements TokenConsumer {
    @Override
    public String consume(TokenQueue tokenQueue) {
        if (tokenQueue.matchesWord()) {
            return tokenQueue.consumeWord();
        }
        return null;
    }

    @Override
    public int order() {
        return 90;
    }

    @Override
    public String tokenType() {
        return Token.CONSTANT;
    }
}
