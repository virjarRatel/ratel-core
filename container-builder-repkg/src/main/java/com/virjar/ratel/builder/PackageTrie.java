package com.virjar.ratel.builder;


import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by virjar on 2018/3/27.<br>
 * quick search if a package is a subPackage for packageNameList<br>
 * <p>
 * <pre>
 * PackageSearchNode root = new PackageSearchNode();
 * root.addToTree("android");
 * root.addToTree("java.lang");
 * root.addToTree("com.alibaba");
 * root.addToTree("com.alipay");
 * root.addToTree("com.baidu");
 * root.addToTree("com.tencent");
 * root.addToTree("com.google");
 * root.addToTree("com.networkbench");
 * root.addToTree("com.sina.weibo");
 * root.addToTree("com.taobao");
 * root.addToTree("com.tendcloud");
 * root.addToTree("com.umeng.message");
 * root.addToTree("org.android");
 * root.addToTree("org.aspectj");
 * root.addToTree("org.java_websocket");
 * </pre>
 * <p>
 * then
 * <p>
 * <pre>
 *     root.isSubPackage("com.alibaba.fastjson.JSONObject"); return true
 *     root.isSubPackage("com.163"); return false
 *     root.isSubPackage("com"); return false
 * </pre>
 *
 * @since 0.3.0
 */
public class PackageTrie {
    // private static final Splitter dotSplitter = Splitter.on(".").omitEmptyStrings();

    private Map<String, PackageTrie> children = new HashMap<>();

    private void addToTree(ArrayList<String> packageSplitItems, int index) {
        if (index > packageSplitItems.size() - 1) {
            return;
        }
        String node = packageSplitItems.get(index);
        PackageTrie packageTrie = children.get(node);
        if (packageTrie == null) {
            packageTrie = new PackageTrie();
            children.put(node, packageTrie);
        }
        packageTrie.addToTree(packageSplitItems, index + 1);
    }

    public PackageTrie addToTree(Collection<String> basePackageList) {
        for (String str : basePackageList) {
            addToTree(str);
        }
        return this;
    }

    public PackageTrie addToTree(String packageName) {
        addToTree(Lists.newArrayList(split(packageName)), 0);
        return this;
    }

    public boolean isSubPackage(String packageName) {
        return isSubPackage(Lists.newArrayList(split(packageName)), 0);
    }

    private boolean isSubPackage(ArrayList<String> packageSplitItems, int index) {
        if (children.size() == 0) {
            return true;
        }
        if (index > packageSplitItems.size() - 1) {
            return false;
        }

        String node = packageSplitItems.get(index);
        return children.containsKey(node) && children.get(node).isSubPackage(packageSplitItems, index + 1);
    }

    private List<String> split(String input) {
        if (input == null) {
            return Collections.emptyList();
        }
        List<String> ret = new ArrayList<>();
        String[] strings = input.split("\\.");
        for (String str : strings) {
            str = str.trim();
            if (str.length() == 0) {
                continue;
            }
            ret.add(str);
        }
        return ret;
    }
}