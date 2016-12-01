package com.github.blindpirate.golang.plugin.core.dependency.tmp;

import com.github.blindpirate.golang.plugin.core.dependency.GolangPackageDependency;
import org.gradle.api.file.DirectoryTree;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.FileVisitDetails;
import org.gradle.api.file.FileVisitor;
import org.gradle.api.file.SourceDirectorySet;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public abstract class AbstractDependencyFactory implements DependencyFactory {

    protected abstract List<String> identityFileNames();

    @Override
    public boolean accept(DirectoryTree rootDir) {
        return anyFileExist(rootDir, identityFileNames());
    }

    private boolean anyFileExist(DirectoryTree rootDir, List<String> strings) {
        for (String fileRelativePath : strings) {
            if (contains(rootDir, fileRelativePath)) {
                return true;
            }
        }
        return false;
    }

    private boolean contains(DirectoryTree rootDir, String fileRelativePath) {
        String rootPath = rootDir.getDir().getAbsolutePath();
        return new File(rootPath + "/" + fileRelativePath).exists();
    }


//    // TODO This is inefficient and should be optimized
//    private Map<String, File> lookup(FileTree rootDir, List<String> identityFileNames) {
//        IdentityFileFinder finder = new IdentityFileFinder(identityFileNames);
//        rootDir.visit(finder);
//        return finder.getIdentityFileMap();
//    }
//
//    private static class IdentityFileFinder implements FileVisitor {
//
//        private Map<String, File> identityFileMap = new HashMap<String, File>();
//
//        public Map<String, File> getIdentityFileMap() {
//            return identityFileMap;
//        }
//
//        public IdentityFileFinder(List<String> identityFileNames) {
//            for (String identityFileName : identityFileNames) {
//                identityFileMap.put(identityFileName, null);
//            }
//        }
//
//        @Override
//        public void visitDir(FileVisitDetails dirDetails) {
//            visitDirOrFile(dirDetails);
//        }
//
//        private void visitDirOrFile(FileVisitDetails dirDetails) {
//            String relativePath = dirDetails.getRelativePath().toString();
//            if (identityFileMap.containsKey(relativePath)) {
//                identityFileMap.put(relativePath, dirDetails.getFile());
//            }
//        }
//
//
//        @Override
//        public void visitFile(FileVisitDetails fileDetails) {
//            visitDirOrFile(fileDetails);
//        }
//    }


}
