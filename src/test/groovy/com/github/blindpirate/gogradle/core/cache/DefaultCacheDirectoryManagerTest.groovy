package com.github.blindpirate.gogradle.core.cache

import org.junit.Test

public class DefaultCacheDirectoryManagerTest {

    def cacheManager = new DefaultCacheDirectoryManager();

    @Test
    void "validation of global cache directory should success"() {
        cacheManager.ensureGlobalCacheExistAndWritable();
    }

}