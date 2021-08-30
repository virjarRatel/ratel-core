package com.virjar.ratel.api.extension.superappium.xpath.parser.expression.token;

import com.virjar.ratel.api.extension.superappium.xpath.parser.expression.SyntaxNode;
import com.virjar.ratel.api.extension.superappium.xpath.exception.XpathSyntaxErrorException;

public interface TokenHandler {
    SyntaxNode parseToken(String tokenStr) throws XpathSyntaxErrorException;

    String typeName();
}
