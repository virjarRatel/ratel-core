package com.virjar.ratel.api.extension.superappium.xpath.parser.expression.token.consumer;

import com.virjar.ratel.api.extension.superappium.xpath.parser.TokenQueue;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.token.Token;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.token.TokenConsumer;

public class FunctionConsumer implements TokenConsumer {
    @Override
    public String consume(TokenQueue tokenQueue) {
        if (tokenQueue.matchesFunction()) {
            return tokenQueue.consumeFunction();
        }
        return null;
    }

    @Override
    public int order() {
        return 60;
    }

    @Override
    public String tokenType() {
        return Token.FUNCTION;
    }
}
