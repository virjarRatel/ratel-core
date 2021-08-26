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

        registerConsumer(new OperatorConsumer());// 40 指定字符开头 div(23) 可以理解为一个函数,也可以理解为一个除法运算,这种歧义不解决了
        registerConsumer(new DigitConsumer());// 50 数字开头
        registerConsumer(new FunctionConsumer());// 60 identify开头,紧随左括号,和一般字母可能冲突
        registerConsumer(new BooleanConsumer());// 70 true,false

        // 下面的token不会冲突
        registerConsumer(new AttributeActionConsumer());// 10 @开头
        registerConsumer(new StringConstantConsumer());// 30 单号开头
        registerConsumer(new XpathConsumer());// 20 反引号开头
        registerConsumer(new ExpressionConsumer());// 0 括号开头

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
     * @param tokenConsumer token识别器
     * @see OperatorEnv#addOperator(java.lang.String, int, java.lang.Class)
     */
    public static void registerConsumer(TokenConsumer tokenConsumer) {
        // operator是特殊逻辑,他应该由系统解析,外部不能知道如何构建语法树,所以操作符的语法节点管理权由框架持有,
        // 第三方如需扩展,可以通过扩展操作符的方式,注册操作符的运算逻辑即可
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
