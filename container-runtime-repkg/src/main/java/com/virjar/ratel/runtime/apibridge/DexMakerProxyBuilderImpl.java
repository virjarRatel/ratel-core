package com.virjar.ratel.runtime.apibridge;

import com.virjar.ratel.api.DexMakerProxyBuilder;
import com.virjar.ratel.api.hint.RatelEngineHistory;
import com.virjar.ratel.api.hint.RatelEngineVersion;
import com.virjar.ratel.runtime.RatelEnvironment;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import external.com.android.dx.stock.ProxyBuilder;

@RatelEngineVersion(value = RatelEngineHistory.V_1_2_8)
public class DexMakerProxyBuilderImpl<T> implements DexMakerProxyBuilder {
    private ProxyBuilder<T> delegate;

    private DexMakerProxyBuilderImpl(Class<T> clazz) {
        delegate = ProxyBuilder.forClass(clazz);
        delegate.dexCache(RatelEnvironment.sandHookCacheDir());
    }

    public static <T> DexMakerProxyBuilderImpl<T> forClass(Class<T> clazz) {
        return new DexMakerProxyBuilderImpl<>(clazz);
    }

    @Override
    public DexMakerProxyBuilder parentClassLoader(ClassLoader parent) {
        delegate.parentClassLoader(parent);
        return this;
    }

    @Override
    public DexMakerProxyBuilder handler(InvocationHandler handler) {
        delegate.handler(handler);
        return this;
    }

    @Override
    public DexMakerProxyBuilder constructorArgValues(Object... constructorArgValues) {
        delegate.constructorArgValues(constructorArgValues);
        return this;
    }

    @Override
    public DexMakerProxyBuilder onlyMethods(Method[] methods) {
        delegate.onlyMethods(methods);
        return this;
    }

    @Override
    public DexMakerProxyBuilder withSharedClassLoader() {
        delegate.withSharedClassLoader();
        return this;
    }

    @Override
    public DexMakerProxyBuilder markTrusted() {
        delegate.markTrusted();
        return this;
    }

    @Override
    public Object build() throws IOException {
        return delegate.build();
    }

    @Override
    public Class buildProxyClass() throws IOException {
        return delegate.buildProxyClass();
    }

    @Override
    public DexMakerProxyBuilder constructorArgTypes(Class[] constructorArgTypes) {
        delegate.constructorArgTypes(constructorArgTypes);
        return this;
    }

    @Override
    public DexMakerProxyBuilder implementing(Class[] interfaces) {
        delegate.implementing(interfaces);
        return this;
    }
}
