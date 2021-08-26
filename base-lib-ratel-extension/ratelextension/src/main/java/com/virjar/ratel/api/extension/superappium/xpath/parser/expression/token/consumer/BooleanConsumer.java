package com.virjar.ratel.api.extension.superappium.xpath.parser.expression.token.consumer;

import com.virjar.ratel.api.extension.superappium.xpath.parser.TokenQueue;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.token.Token;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.token.TokenConsumer;

public class BooleanConsumer implements TokenConsumer {
    @Override
    public String consume(TokenQueue tokenQueue) {
        if (tokenQueue.matchesBoolean()) {
            return tokenQueue.consumeWord();
        }
        return null;
    }

    @Override
    public int order() {
        return 70;
    }

    @Override
    public String tokenType() {
        return Token.BOOLEAN;
    }
}
