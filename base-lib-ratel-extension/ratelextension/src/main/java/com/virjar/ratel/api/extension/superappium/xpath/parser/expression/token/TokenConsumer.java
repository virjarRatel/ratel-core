package com.virjar.ratel.api.extension.superappium.xpath.parser.expression.token;

import com.virjar.ratel.api.extension.superappium.xpath.parser.TokenQueue;

public interface TokenConsumer {
    String consume(TokenQueue tokenQueue);

    int order();

    String tokenType();
}
