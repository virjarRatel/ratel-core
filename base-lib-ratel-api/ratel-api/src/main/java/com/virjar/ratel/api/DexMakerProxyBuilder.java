package com.virjar.ratel.api;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public interface DexMakerProxyBuilder<T> {
    DexMakerProxyBuilder<T> parentClassLoader(ClassLoader parent);

    DexMakerProxyBuilder<T> handler(InvocationHandler handler);

    DexMakerProxyBuilder<T> implementing(Class<?>... interfaces);

    DexMakerProxyBuilder<T> constructorArgValues(Object... constructorArgValues);

    DexMakerProxyBuilder<T> constructorArgTypes(Class<?>... constructorArgTypes);

    DexMakerProxyBuilder<T> onlyMethods(Method[] methods);

    DexMakerProxyBuilder<T> withSharedClassLoader();

    DexMakerProxyBuilder<T> markTrusted();

    T build() throws IOException;

    Class<? extends T> buildProxyClass() throws IOException;
}
