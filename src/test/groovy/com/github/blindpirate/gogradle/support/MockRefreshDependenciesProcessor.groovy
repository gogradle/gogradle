package com.github.blindpirate.gogradle.support

import org.gradle.StartParameter

import static org.mockito.Mockito.when

class MockRefreshDependenciesProcessor extends GogradleGlobalProcessor<MockRefreshDependencies> {
    void doMock(StartParameter startParameter, MockRefreshDependencies annotation) {
        when(startParameter.isRefreshDependencies()).thenReturn(annotation.value())
    }
}
