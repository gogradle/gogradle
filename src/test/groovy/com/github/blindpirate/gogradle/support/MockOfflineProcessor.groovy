package com.github.blindpirate.gogradle.support

import org.gradle.StartParameter

import static org.mockito.Mockito.when

class MockOfflineProcessor extends GogradleGlobalProcessor<MockOffline> {
    @Override
    void doMock(StartParameter startParameter, MockOffline annotation) {
        when(startParameter.isOffline()).thenReturn(annotation.value())
    }
}
