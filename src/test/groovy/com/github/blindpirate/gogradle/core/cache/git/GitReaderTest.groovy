package com.github.blindpirate.gogradle.core.cache.git

import com.github.blindpirate.gogradle.GolangPluginSetting
import com.github.blindpirate.gogradle.core.cache.CacheManager
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.runners.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.class)
class GitReaderTest {
    @InjectMocks
    GitReader gitReader
    @Mock
    GolangPluginSetting setting
    @Mock
    CacheManager cacheManager

    @Test
    public void initTest() {

    }

    @Test
    public void initTest2() {

    }

}
