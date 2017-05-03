package com.github.blindpirate.gogradle.support

import com.github.blindpirate.gogradle.util.IOUtils
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.errors.RepositoryNotFoundException
import org.eclipse.jgit.http.server.GitServlet
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.transport.ServiceMayNotContinueException
import org.eclipse.jgit.transport.resolver.RepositoryResolver
import org.eclipse.jgit.transport.resolver.ServiceNotAuthorizedException
import org.eclipse.jgit.transport.resolver.ServiceNotEnabledException

import javax.servlet.http.HttpServletRequest

class GitServer {
    public static final int DEFAULT_PORT = 8080
    private Map<String, Repository> repos = [:]

    private Server server

    static GitServer newServer() {
        return new GitServer()
    }

    static GitServer newServer(File repos) {
        GitServer ret = new GitServer()
        if (new File(repos, ".git").exists()) {
            ret.addRepo(repos.getName(), repos)
        } else {
            repos.listFiles().each {
                ret.addRepo(it.name, it)
            }
        }
        return ret
    }

    void addRepo(String path, File repo) throws IOException {
        repos.put(path, newRepository(repo))
    }

    void start(int port) throws Exception {
        server = new Server(port)

        ServletHandler handler = new ServletHandler()
        server.setHandler(handler)

        GitServlet gs = newServlet()
        handler.addServletWithMapping(new ServletHolder(gs), "/*")
        server.start()
    }

    void stop() throws Exception {
        server.stop()
    }

    private GitServlet newServlet() {
        GitServlet gs = new GitServlet()
        gs.setRepositoryResolver(new RepositoryResolver<HttpServletRequest>() {
            @Override
            Repository open(HttpServletRequest req, String name) throws RepositoryNotFoundException,
                    ServiceNotAuthorizedException, ServiceNotEnabledException, ServiceMayNotContinueException {
                Repository repository = repos.get(name)
                repository.incrementOpen()
                return repository
            }
        })
        return gs
    }

    static Repository newRepository(File dir) throws IOException {
        return new FileRepositoryBuilder().setGitDir(new File(dir, ".git"))
                .readEnvironment()
                .findGitDir()
                .build()
    }

    static void createRepository(File dir, String fileName) {
        Repository repository = FileRepositoryBuilder.create(new File(dir, '.git'))
        repository.create()
        Git git
        try {
            git = new Git(repository)
            fileName.split('&').each {
                IOUtils.write(dir, it.trim(), '')
            }
            git.add().addFilepattern('.').call()
            git.commit().setMessage('commit').call()
        } finally {
            if (git != null) {
                git.close()
            }
        }
    }

    static void addFileToRepository(File dir, String fileName) {
        Repository repository = FileRepositoryBuilder.create(new File(dir, '.git'))
        Git git
        try {
            git = new Git(repository)
            new File(dir, fileName).createNewFile()
            git.add().addFilepattern(fileName).call()
            git.commit().setMessage('commit').call()
        } finally {
            if (git != null) {
                git.close()
            }
        }
    }

}

