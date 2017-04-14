package com.github.blindpirate.gogradle.core.cache;

import com.github.blindpirate.gogradle.core.GolangCloneable;
import com.github.blindpirate.gogradle.util.ExceptionHandler;
import com.github.blindpirate.gogradle.util.IOUtils;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

public abstract class PersistentCache<K, V extends GolangCloneable> extends AbstractCache<K, V> {

    private static final Logger LOGGER = Logging.getLogger(PersistentCache.class);
    private final Project project;

    protected PersistentCache(Project project) {
        this.project = project;
    }

    @SuppressWarnings("unchecked")
    public void load() {
        File persistenceFile = new File(project.getRootDir(), ".gogradle/cache/" + getClass().getSimpleName() + ".bin");
        if (persistenceFile.exists()) {
            try {
                container = (ConcurrentHashMap) IOUtils.deserialize(persistenceFile);
            } catch (ExceptionHandler.UncheckedException e) {
                LOGGER.warn("Exception in deserializing dependency cache, skip.");
                LOGGER.info("", e);
            }
        } else {
            LOGGER.info("Cache {} not found, skip.", getClass().getSimpleName());
        }
    }
}
