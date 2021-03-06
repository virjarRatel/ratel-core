package com.virjar.ratel.api.extension.superappium.xpath.parser.expression.token;

import com.virjar.ratel.api.extension.superappium.xpath.parser.TokenQueue;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.token.consumer.AttributeActionConsumer;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.token.consumer.BooleanConsumer;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.token.consumer.DefaultWordConsumer;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.token.consumer.DefaultXpathConsumer;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.token.consumer.DigitConsumer;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.token.consumer.ExpressionConsumer;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.token.consumer.FunctionConsumer;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.token.consumer.OperatorConsumer;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.token.consumer.StringConstantConsumer;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.token.consumer.XpathConsumer;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.token.handler.BooleanHandler;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.token.handler.ConstantHandler;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.token.handler.ExpressionHandler;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.token.handler.NumberHandler;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.token.handler.XpathHandler;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.token.handler.AttributeHandler;
import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.token.handler.FunctionHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

public class TokenAnalysisRegistry {
    private static TreeSet<TokenConsumerWrapper> allConsumer = new TreeSet<>();
    private static Map<String, TokenHandler> allHandler = new HashMap<>();

    static {
        registerHandler(new AttributeHandler());
        registerHandler(new BooleanHandler());
        registerHandler(new ConstantHandler());
        registerHandler(new FunctionHandler());
        registerHandler(new NumberHandler());
        registerHandler(new XpathHandler());
        registerHandler(new ExpressionHandler());

        registerConsumer(new OperatorConsumer());// 40 ?????????????????? div(23) ???????????????????????????,????????????????????????????????????,????????????????????????
        registerConsumer(new DigitConsumer());// 50 ????????????
        registerConsumer(new FunctionConsumer());// 60 identify??????,???????????????,???????????????????????????
        registerConsumer(new BooleanConsumer());// 70 true,false

        // ?????????token????????????
        registerConsumer(new AttributeActionConsumer());// 10 @??????
        registerConsumer(new StringConstantConsumer());// 30 ????????????
        registerConsumer(new XpathConsumer());// 20 ???????????????
        registerConsumer(new ExpressionConsumer());// 0 ????????????

        // TODO
        registerConsumer(new DefaultWordConsumer());
        registerConsumer(new DefaultXpathConsumer());
    }

    public static void registerHandler(TokenHandler tokenHandler) {
        if (Token.OPERATOR.equals(tokenHandler.typeName()) && allHandler.containsKey(Token.OPERATOR)) {
            throw new IllegalStateException(
                    "can not register operator handler,operator handler must hold by framework");
        }
        allHandler.put(tokenHandler.typeName(), tokenHandler);
    }

    /**
     * @param tokenConsumer token?????????
     * @see OperatorEnv#addOperator(java.lang.String, int, java.lang.Class)
     */
    public static void registerConsumer(TokenConsumer tokenConsumer) {
        // operator???????????????,????????????????????????,???????????????????????????????????????,??????????????????????????????????????????????????????,
        // ?????????????????????,????????????????????????????????????,????????????????????????????????????
        if (!Token.OPERATOR.equals(tokenConsumer.tokenType()) && !allHandler.containsKey(tokenConsumer.tokenType())) {
            throw new IllegalStateException("can not register token consumer ,not token handler available");
        }
        allConsumer.add(new TokenConsumerWrapper(tokenConsumer));
    }

    public static TokenHandler findHandler(String tokenType) {
        return allHandler.get(tokenType);
    }

    public static Iterable<? extends TokenConsumer> consumerIterable() {
        return allConsumer;
    }

    private static class TokenConsumerWrapper implements Comparable<TokenConsumer>, TokenConsumer {
        private TokenConsumer delegate;

        TokenConsumerWrapper(TokenConsumer delegate) {
            this.delegate = delegate;
        }

        @Override
        public String consume(TokenQueue tokenQueue) {
            return delegate.consume(tokenQueue);
        }

        @Override
        public int order() {
            return delegate.order();
        }

        @Override
        public String tokenType() {
            return delegate.tokenType();
        }

        @Override
        public int compareTo(TokenConsumer o) {
            if (this == o) {
                return 0;
            }
            return Integer.valueOf(delegate.order()).compareTo(o.order());
        }
    }
}
