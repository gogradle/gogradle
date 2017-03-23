package com.github.blindpirate.gogradle;

import com.github.blindpirate.gogradle.vcs.git.GolangRepository;
import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;
import org.gradle.util.Configurable;
import org.gradle.util.ConfigureUtil;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Singleton
public class GolangRepositoryHandler extends GroovyObjectSupport implements Configurable<Void> {

    private List<GolangRepository> gitRepositories = new ArrayList<>();

    public GolangRepository findMatchedRepository(String name) {
        Optional<GolangRepository> matched = gitRepositories.stream()
                .filter(repo -> repo.match(name))
                .findFirst();
        return matched.isPresent() ? matched.get() : GolangRepository.EMPTY_INSTANCE;
    }

    @Override
    public Void configure(Closure cl) {
        GolangRepository repository = new GolangRepository();
        ConfigureUtil.configure(cl, repository);
        gitRepositories.add(repository);
        return null;
    }
}
