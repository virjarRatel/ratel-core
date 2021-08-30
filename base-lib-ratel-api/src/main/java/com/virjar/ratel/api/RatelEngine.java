package com.virjar.ratel.api;

/**
 * ratel框架支持的四种引擎
 */
public enum RatelEngine {
    //入口重编
    REBUILD_DEX,
    //修改AndroidManifest.xml，并植入新的dex
    APPEND_DEX,
    //通过shell包裹，这种引擎线上使用此时非常少
    SHELL,
    //zelda引擎，这里指商业版，开源版引擎: https://github.com/virjar/zelda
    ZELDA,
    //源码编译入口，功能入口在Android framework，不涉及对抗
    KEATOS,
}
