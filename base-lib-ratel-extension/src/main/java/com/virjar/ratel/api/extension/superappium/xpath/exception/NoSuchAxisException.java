package com.virjar.ratel.api.extension.superappium.xpath.exception;

public class NoSuchAxisException extends XpathSyntaxErrorException {
    public NoSuchAxisException(int errorPos, String msg) {
        super(errorPos, msg);
    }
}
