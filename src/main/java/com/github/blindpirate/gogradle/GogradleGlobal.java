/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.github.blindpirate.gogradle;

import com.google.inject.Injector;
import com.google.inject.Key;
import org.gradle.api.Project;

public enum GogradleGlobal {
    INSTANCE;

    public static final String GOGRADLE_VERSION = "0.10";
    public static final String GOGRADLE_COMPATIBLE_VERSION = "0.10";
    public static final String DEFAULT_CHARSET = "UTF-8";
    public static final String GOGRADLE_BUILD_DIR_NAME = ".gogradle";
    public static final int MAX_DFS_DEPTH = 100;
    public static final String GOGRADLE_REFRESH = "gogradle.refresh";
    public static final String GOGRADLE_MODE = "gogradle.mode";
    public static final String GOGRADLE_ALIAS = "gogradle.alias";

    // For multi-project
    private ThreadLocal<Injector> currentInjector = new ThreadLocal<>();

    public Injector getInjector() {
        return currentInjector.get();
    }

    public void setCurrentProject(Project project) {
        if (project == null) {
            currentInjector.set(null);
        } else {
            currentInjector.set((Injector) project.getExtensions().getByName(GolangPlugin.GOGRADLE_INJECTOR));
        }
    }

    public static <T> T getInstance(Class<T> clazz) {
        return INSTANCE.getInjector().getInstance(clazz);
    }

    public static boolean isOffline() {
        return getInstance(Project.class).getGradle().getStartParameter().isOffline();
    }

    public static boolean isRefreshDependencies() {
        return getInstance(Project.class).getGradle().getStartParameter().isRefreshDependencies()
                || "true".equals(System.getProperty(GOGRADLE_REFRESH));
    }

    public static boolean isAlias() {
        return "true".equals(System.getProperty(GOGRADLE_ALIAS));
    }

    public static String getMode() {
        String mode = System.getProperty(GOGRADLE_MODE);
        return mode == null ? "" : mode;
    }

    public static <T> T getInstance(Key<T> key) {
        return INSTANCE.getInjector().getInstance(key);
    }
}
