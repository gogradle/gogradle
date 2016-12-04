package com.github.blindpirate.gogradle.core.cache

import org.junit.Test

public class DefaultCacheManagerTest {

    def cacheManager = new DefaultCacheManager();

    @Test
    void "validation of global cache directory should success"() {
        cacheManager.ensureGlobalCacheExistAndWritable();
    }

}