package com.github.blindpirate.gogradle.core.dependency.resolve;

import com.github.blindpirate.gogradle.core.FileSystemModule;
import com.github.blindpirate.gogradle.core.InjectionHelper;
import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.produce.VendorOnlyProduceStrategy;
import com.github.blindpirate.gogradle.core.pack.PackageInfo;
import com.github.blindpirate.gogradle.core.pack.PackagePathResolver;
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
public class VendorDirectoryVisitor extends SimpleFileVisitor<Path> {
    private static final Logger LOGGER = Logging.getLogger(VendorDirectoryVisitor.class);

    private Path parentModuleVendor;

    private FileSystemModule parentModule;

    private PackagePathResolver resolver;

    private GolangDependencySet dependencies = new GolangDependencySet();

    public GolangDependencySet getDependencies() {
        return dependencies;
    }

    public VendorDirectoryVisitor(FileSystemModule parentModule,
                                  PackagePathResolver resolver) {
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
        String packagePath = parentModuleVendor.relativize(currentAbsolutePath).toString();

        PackageInfo packageInfo = resolver.produce(packagePath).get();
        if (packageInfo != PackageInfo.INCOMPLETE) {
            // current path is root of a repo
            LOGGER.debug("Produce package {}.", packagePath);
            dependencies.add(createDependency(packagePath));
            return FileVisitResult.SKIP_SUBTREE;
        } else {
            LOGGER.debug("Cannot produce package with path {}, skip.", packagePath);
            return FileVisitResult.CONTINUE;
        }
    }

    private GolangDependency createDependency(String packagePath) {
        FileSystemModule module = parentModule.vendor(packagePath);
        module.setStrategy(InjectionHelper.strategy(VendorOnlyProduceStrategy.class));
        module.getDependencies();
        return module;
    }
}
