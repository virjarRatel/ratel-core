package com.virjar.ratel.api.extension.superappium.xmodel;


import com.virjar.ratel.api.extension.superappium.ViewImage;

public interface ValueGetter<T> {
    T get(ViewImage viewImage);

    boolean support(Class type);

    String attr();
}
