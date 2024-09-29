package org.pentaho.di.plugins.fileopensave.service;

import org.pentaho.di.plugins.fileopensave.cache.FileCache;

public enum FileCacheService {

    INSTANCE;

    private FileCache fileCache;

    private FileCacheService() {
        fileCache = new FileCache();
    }

    public FileCache get() {
        return fileCache;
    }

}
