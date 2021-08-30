package com.virjar.ratel.api.extension.superappium.xpath.exception;

public class XpathSyntaxErrorException extends Exception {
    private  int errorPos;


    public XpathSyntaxErrorException(int errorPos,Throwable cause) {
        super(cause);
        this.errorPos = errorPos;
    }

    public XpathSyntaxErrorException(int errorPos,String msg) {
        super(msg);
        this.errorPos = errorPos;
    }

    public XpathSyntaxErrorException(int errorPos, String message, Throwable cause) {
        super(message, cause);
        this.errorPos = errorPos;
    }

    public XpathSyntaxErrorException(int errorPos, String message, Throwable cause, boolean enableSuppression,
                                     boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.errorPos = errorPos;
    }
}
