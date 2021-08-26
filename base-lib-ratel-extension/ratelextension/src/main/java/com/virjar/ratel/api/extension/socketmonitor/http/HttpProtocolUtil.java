package com.virjar.ratel.api.extension.socketmonitor.http;

import java.util.HashMap;
import java.util.Map;

import external.org.apache.commons.lang3.StringUtils;

public class HttpProtocolUtil {
    private static final String httpResponseMagic = "HTTP/";

    public static boolean maybeHttpResponse(byte[] data, long dataLength) {
        //first,find the start of data
        int pos = 0;
        while (pos < dataLength) {
            if (!isWhitespace(data[pos])) {
                break;
            }
            pos++;
        }
        if (pos + httpResponseMagic.length() >= dataLength) {
            return false;
        }
        //then the HTTP/1.1 200 OK
        //read 5 byte
        return StringUtils.equalsIgnoreCase(httpResponseMagic, new String(data, pos, httpResponseMagic.length()));
    }

    private static boolean isWhitespace(final byte ch) {
        return ch == HttpStreamUtil.SP || ch == HttpStreamUtil.HT || ch == HttpStreamUtil.CR || ch == HttpStreamUtil.LF;
    }

    enum Method {
        GET,
        PUT,
        POST,
        DELETE,
        HEAD,
        OPTIONS,
        TRACE,
        CONNECT,
        PATCH,
        PROPFIND,
        PROPPATCH,
        MKCOL,
        MOVE,
        COPY,
        LOCK,
        UNLOCK;
    }

    private static class Trie {
        private Map<Byte, Trie> values = new HashMap<>();
        private String method = null;

        void addToTree(byte[] data, int index, String worldEntry) {
            if (index >= data.length) {
                //the last
                if (this.method == null) {
                    this.method = worldEntry;
                }
                return;
            }
            Trie trie = values.get(data[index]);
            if (trie == null) {
                trie = new Trie();
                values.put(data[index], trie);
            }
            trie.addToTree(data, index + 1, worldEntry);
        }

        String find(byte[] pingyings, int index) {
            if (index >= pingyings.length) {
                return this.method;
            }

            Trie trie = values.get(pingyings[index]);
            if (trie == null) {
                return this.method;
//                return null;
            }
            return trie.find(pingyings, index + 1);

        }

    }

    private static Trie methodCharacterTree = new Trie();

    static {
        for (Method method : Method.values()) {
            String name = method.name();
            methodCharacterTree.addToTree(name.getBytes(), 0, name);
        }
    }


    public static boolean maybeHttpRequest(byte[] data) {
        return StringUtils.isNotBlank(methodCharacterTree.find(data, 0));
    }

}
