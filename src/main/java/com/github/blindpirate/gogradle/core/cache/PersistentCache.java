package com.github.blindpirate.gogradle.core.cache;

import com.github.blindpirate.gogradle.core.GolangCloneable;
import com.github.blindpirate.gogradle.util.ExceptionHandler;
import com.github.blindpirate.gogradle.util.IOUtils;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.io.UncheckedIOException;
import java.util.Map;

public abstract class PersistentCache<K extends GolangCloneable, V extends GolangCloneable>
        extends AbstractCache<K, V> {
    private static final Logger LOGGER = Logging.getLogger(PersistentCache.class);

    private final File persistenceFile;

    public PersistentCache(File persistenceFile) {
        this.persistenceFile = persistenceFile;
    }

    @SuppressWarnings("unchecked")
    public void load() {
        if (persistenceFile.exists()) {
            try {
                container = (Map) IOUtils.deserialize(persistenceFile);
            } catch (ExceptionHandler.UncheckedException e) {
                LOGGER.warn("Exception in deserializing dependency cache, skip.");
                LOGGER.info("", e);
            }
        } else {
            LOGGER.info("Cache {} not found, skip.", getClass().getSimpleName());
        }
    }

    public void save() {
        try {
            IOUtils.serialize(container, persistenceFile);
        } catch (UncheckedIOException e) {
            LOGGER.warn("Exception in serializing dependency cache, skip.");
            LOGGER.info("", e);
        }
    }
}
