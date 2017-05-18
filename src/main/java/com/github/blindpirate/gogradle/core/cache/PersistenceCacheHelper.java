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

package com.github.blindpirate.gogradle.core.cache;

import com.github.blindpirate.gogradle.util.ExceptionHandler;
import com.github.blindpirate.gogradle.util.IOUtils;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.io.UncheckedIOException;
import java.util.Map;

public interface PersistenceCacheHelper {
    Logger LOGGER = Logging.getLogger(PersistenceCache.class);

    @SuppressWarnings("unchecked")
    static void load(Map cache, File persistenceFile) {
        if (persistenceFile.exists()) {
            try {
                cache.putAll((Map) IOUtils.deserialize(persistenceFile));
            } catch (ExceptionHandler.UncheckedException e) {
                LOGGER.warn("Exception in deserializing dependency cache, skip.");
                LOGGER.info("", e);
            }
        } else {
            LOGGER.info("Cache {} not found, skip.", persistenceFile);
        }
    }

    static void save(Object cache, File persistenceFile) {
        try {
            IOUtils.serialize(cache, persistenceFile);
        } catch (UncheckedIOException e) {
            LOGGER.warn("Exception in serializing dependency cache, skip.");
            LOGGER.info("", e);
        }
    }
}
