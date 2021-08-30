package com.virjar.ratel.api.extension.superappium.xpath.parser.expression.token.consumer;

import com.virjar.ratel.api.extension.superappium.xpath.parser.TokenQueue;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.token.Token;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.token.TokenConsumer;

public class AttributeActionConsumer implements TokenConsumer {
    @Override
    public String consume(TokenQueue tokenQueue) {
        if (tokenQueue.matches("@")) {
            tokenQueue.advance();
            return tokenQueue.consumeAttributeKey();
        }
        return null;
    }

    @Override
    public int order() {
        return 10;
    }

    @Override
    public String tokenType() {
        return Token.ATTRIBUTE_ACTION;
    }
}
