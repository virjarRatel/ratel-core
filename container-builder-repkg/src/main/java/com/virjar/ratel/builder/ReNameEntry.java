package com.virjar.ratel.builder;

import org.apache.tools.zip.ZipEntry;

import java.util.zip.ZipException;

public class ReNameEntry extends ZipEntry {
    public ReNameEntry(java.util.zip.ZipEntry entry, String name) throws ZipException {
        super(entry);
        setName(name);
    }
}
