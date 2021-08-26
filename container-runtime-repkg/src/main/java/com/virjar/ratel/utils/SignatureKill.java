package com.virjar.ratel.utils;

import android.content.pm.Signature;

import java.util.ArrayList;
import java.util.List;

import external.org.apache.commons.lang3.StringUtils;

/**
 * Created by yanchen on 17-12-10.
 */

public class SignatureKill {
    public static String signatureInfo(Signature[] signatures) {
        List<String> strings = new ArrayList<>();
        if (signatures == null) {
            return StringUtils.join(strings, "*");// Joiner.on("*").join(strings);
        }
        for (Signature signature : signatures) {
            strings.add(signature.toCharsString());
        }
        return StringUtils.join(strings, "*");
    }
}
