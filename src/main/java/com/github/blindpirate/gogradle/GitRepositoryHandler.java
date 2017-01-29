package com.github.blindpirate.gogradle;

import com.github.blindpirate.gogradle.vcs.git.GitRepository;
import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;
import org.gradle.util.Configurable;
import org.gradle.util.ConfigureUtil;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Singleton
public class GitRepositoryHandler extends GroovyObjectSupport implements Configurable<Void> {

    private List<GitRepository> gitRepositories = new ArrayList<>();

    public Optional<GitRepository> findMatchedRepository(String name, String url) {
        return gitRepositories.stream().filter(repo -> repo.match(name, url)).findFirst();
    }

    @Override
    public Void configure(Closure cl) {
        GitRepository repository = new GitRepository();
        ConfigureUtil.configure(cl, repository);
        gitRepositories.add(repository);
        return null;
    }
}
