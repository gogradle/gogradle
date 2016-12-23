package com.github.blindpirate.gogradle.core.dependency.resolve;

import com.github.blindpirate.gogradle.core.FileSystemModule;
import com.github.blindpirate.gogradle.core.dependency.DependencyHelper;
import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.produce.VendorOnlyProduceStrategy;
import com.github.blindpirate.gogradle.core.pack.PackageInfo;
import com.github.blindpirate.gogradle.core.pack.PackageNameResolver;
import com.google.common.base.Optional;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static com.github.blindpirate.gogradle.core.dependency.resolve.VendorDependencyFactory.VENDOR_DIRECTORY;

/**
 * Analyze vendor directory to generate dependencies.
 */
public class VendorDirectoryVistor extends SimpleFileVisitor<Path> {
    private static final Logger LOGGER = Logging.getLogger(VendorDirectoryVistor.class);

    static final int MAX_DEPTH = 100;

    private Path parentModuleVendor;

    private FileSystemModule parentModule;

    private PackageNameResolver resolver;

    private GolangDependencySet dependencies = new GolangDependencySet();

    public GolangDependencySet getDependencies() {
        return dependencies;
    }

    public VendorDirectoryVistor(FileSystemModule parentModule,
                                 PackageNameResolver resolver) {
        this.resolver = resolver;
        this.parentModule = parentModule;
        this.parentModuleVendor = parentModule.getRootDir().resolve(VENDOR_DIRECTORY);
    }

    @Override
    public FileVisitResult preVisitDirectory(Path currentAbsolutePath, BasicFileAttributes attrs)
            throws IOException {
        if (currentAbsolutePath.equals(parentModuleVendor)) {
            // ignore vendor root
            return FileVisitResult.CONTINUE;
        }

        // relative path, i.e "github.com/a/b"
        String packageName = parentModuleVendor.relativize(currentAbsolutePath).toString();

        Optional<PackageInfo> packageInfo = resolver.produce(packageName);
        if (isRootPackage(packageInfo)) {
            // current path is root of a repo
            LOGGER.debug("Produce package {}.", packageName);
            dependencies.add(createDependency(packageName));
            return FileVisitResult.SKIP_SUBTREE;
        } else {
            LOGGER.debug("Cannot produce package with path {}, skip.", packageName);
            return FileVisitResult.CONTINUE;
        }
    }

    private boolean isRootPackage(Optional<PackageInfo> packageInfo) {
        return packageInfo.isPresent() && packageInfo.get() != PackageInfo.INCOMPLETE;
    }

    private GolangDependency createDependency(String packageName) {
        FileSystemModule module = parentModule.vendor(packageName);
        module.setStrategy(DependencyHelper.strategy(VendorOnlyProduceStrategy.class));
        module.getDependencies();
        return module;
    }
}
