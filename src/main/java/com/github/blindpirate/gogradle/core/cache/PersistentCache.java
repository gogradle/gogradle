package com.github.blindpirate.gogradle.core.cache;

import com.github.blindpirate.gogradle.core.GolangCloneable;
import com.github.blindpirate.gogradle.util.ExceptionHandler;
import com.github.blindpirate.gogradle.util.IOUtils;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class PersistentCache<K extends GolangCloneable, V extends GolangCloneable>
        extends AbstractCache<K, V> {
    private static final Logger LOGGER = Logging.getLogger(PersistentCache.class);
    private final Project project;
    private final ConcurrentMap<K, V> hitCache = new ConcurrentHashMap<>();

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

    @SuppressWarnings("unchecked")
    @Override
    public void hitCache(K key, V cachedItem) {
        hitCache.put((K) key.clone(), cachedItem);
    }

    public void save() {
        File persistenceFile = new File(project.getRootDir(), ".gogradle/cache/" + getClass().getSimpleName() + ".bin");
        try {
            IOUtils.serialize(hitCache, persistenceFile);
        } catch (ExceptionHandler.UncheckedException e) {
            LOGGER.warn("Exception in serializing dependency cache, skip.");
            LOGGER.info("", e);
        }
    }
}
