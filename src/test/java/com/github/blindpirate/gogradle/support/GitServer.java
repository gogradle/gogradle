package com.github.blindpirate.gogradle.support;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jgit.http.server.GitServlet;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GitServer {
    private Map<String, Repository> repos = new HashMap<>();

    private Server server;

    public static GitServer newServer() {
        return new GitServer();
    }

    public void addRepo(String path, File repo) throws IOException {
        repos.put(path, newRepository(repo));
    }

    public void start(int port) throws Exception {
        server = new Server(port);

        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);

        GitServlet gs = newServlet();
        handler.addServletWithMapping(new ServletHolder(gs), "/*");
        server.start();
    }

    public void stop() throws Exception {
        server.stop();
    }

    private GitServlet newServlet() {
        GitServlet gs = new GitServlet();
        gs.setRepositoryResolver((req, name) -> {
            Repository repository = repos.get(name);
            repository.incrementOpen();
            return repository;
        });
        return gs;
    }

    private Repository newRepository(File dir) throws IOException {
        return new FileRepositoryBuilder().setGitDir(new File(dir, ".git"))
                .readEnvironment()
                .findGitDir()
                .build();
    }

}
