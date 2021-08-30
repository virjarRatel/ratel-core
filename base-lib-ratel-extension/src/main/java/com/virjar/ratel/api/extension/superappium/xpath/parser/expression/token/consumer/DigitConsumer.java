package com.virjar.ratel.api.extension.superappium.xpath.parser.expression.token.consumer;

import com.virjar.ratel.api.extension.superappium.xpath.parser.TokenQueue;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.token.Token;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.token.TokenConsumer;

public class DigitConsumer implements TokenConsumer {
    @Override
    public String consume(TokenQueue tokenQueue) {
        // 当前遇到的串是一个数字
        if (tokenQueue.matchesDigit()) {
            return tokenQueue.consumeDigit();
        }
        return null;
    }

    @Override
    public int order() {
        return 50;
    }

    @Override
    public String tokenType() {
        return Token.NUMBER;
    }
}
