package com.virjar.ratel.envmock;

import android.support.annotation.Keep;

public class PropertiesMockItem {
    @Keep
    public PropertiesMockItem(String properties_key, String properties_value) {
        this.properties_key = properties_key;
        this.properties_value = properties_value;
    }

    private String properties_key;
    private String properties_value;

    public String getProperties_key() {
        return properties_key;
    }

    public String getProperties_value() {
        return properties_value;
    }
}
