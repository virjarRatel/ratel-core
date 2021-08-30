package com.virjar.ratel.api.extension.superappium.xpath.parser.expression.token.consumer;

import com.virjar.ratel.api.extension.superappium.xpath.parser.TokenQueue;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.OperatorEnv;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.token.Token;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.token.TokenConsumer;

import java.util.List;

public class OperatorConsumer implements TokenConsumer {
    @Override
    public String consume(TokenQueue tokenQueue) {
        List<OperatorEnv.AlgorithmHolder> algorithmHolders = OperatorEnv.allAlgorithmUnitList();
        for (OperatorEnv.AlgorithmHolder holder : algorithmHolders) {
            if (tokenQueue.matches(holder.getKey())) {
                tokenQueue.consume(holder.getKey());
                return holder.getKey();
            }
        }
        return null;
    }

    @Override
    public int order() {
        return 40;
    }

    @Override
    public String tokenType() {
        return Token.OPERATOR;
    }
}
