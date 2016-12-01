package com.github.blindpirate.golang.plugin.core.cache

import org.junit.Test;

import static org.junit.Assert.*;

public class DefaultCacheDirectoryManagerTest {

    def cacheManager = new DefaultCacheDirectoryManager();

    @Test
    void "validation of global cache directory should success"() {
        cacheManager.ensureGlobalCacheExistAndWritable();
    }

}