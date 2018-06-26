package com.github.blindpirate.gogradle.core.dependency.produce.external.dep;

import com.github.blindpirate.gogradle.core.VcsGolangPackage;
import com.github.blindpirate.gogradle.core.dependency.NotationDependency;
import com.github.blindpirate.gogradle.core.pack.PackagePathResolver;
import com.github.blindpirate.gogradle.vcs.VcsScheme;
import com.google.common.collect.ImmutableMap;
import com.moandjiezana.toml.Toml;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Model of Gopkg.lock file managed by dep.
 *
 * @see <a href="https://github.com/golang/dep/blob/master/Gopkg.lock" >Gopkg.lock</a>
 */
public class GopkgDotLockModel {
    private static final Map<String, String> PROPERTY_NAME_CONVERSION =
            ImmutableMap.of("packages", "subpackages",
                    "revision", "commit",
                    "version", "tag");
    private static final Map<String, Function<List, List>> PROPERTY_CONVERSION =
            ImmutableMap.of("packages", GopkgDotLockModel::convertSubpackages);

    public static List<Map<String, Object>> parse(PackagePathResolver packagePathResolver, File file) {
        Toml toml = new Toml().read(file);
        List<Map<String, Object>> projects = toml.getList("projects");
        if (projects == null) {
            projects = Collections.emptyList();
        }
        convertProperties(projects);
        convertPropertyNames(projects);
        processSource(packagePathResolver, projects);
        return projects;
    }

    private static void processSource(PackagePathResolver packagePathResolver, List<Map<String, Object>> projects) {
        projects.forEach(project -> {
            String source = (String) project.remove("source");
            if (source != null) {
                if (startWithVcsScheme(source) || isGitUrl(source)) {
                    project.put("url", source);
                } else {
                    VcsGolangPackage pkg = (VcsGolangPackage) packagePathResolver.produce(source).get();
                    project.put("urls", pkg.getUrls());
                    project.put("vcs", pkg.getVcsType().getName());
                }
            }
        });
    }

    private static boolean isGitUrl(String source) {
        return source.startsWith("git@");
    }

    private static boolean startWithVcsScheme(String source) {
        return Stream.of(VcsScheme.values()).map(VcsScheme::getScheme).anyMatch(source::startsWith);
    }

    private static void convertPropertyNames(List<Map<String, Object>> projects) {
        projects.forEach((Map project) -> {
            PROPERTY_NAME_CONVERSION.forEach((nameBefore, nameAfter) -> {
                Object value = project.remove(nameBefore);
                if (value != null) {
                    project.put(nameAfter, value);
                }
            });
        });
    }

    private static void convertProperties(List<Map<String, Object>> projects) {
        projects.forEach((Map project) ->
                PROPERTY_CONVERSION.forEach((key, fn) -> {
                    if (project.containsKey(key)) {
                        List value = (List) project.get(key);
                        value = fn.apply(value);
                        project.put(key, value);
                    }
                })
        );
    }


    private static List<String> convertSubpackages(List<String> subpackagesInDepDotLock) {
        return subpackagesInDepDotLock.stream()
                .map(GopkgDotLockModel::convertDotToTripleDot)
                .collect(Collectors.toList());
    }

    private static String convertDotToTripleDot(String s) {
        return ".".equals(s) ? NotationDependency.ALL_DESCENDANTS : s;
    }
}
