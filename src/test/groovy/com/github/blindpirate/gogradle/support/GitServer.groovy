/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.github.blindpirate.gogradle.support

import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.ProcessUtils
import org.apache.tools.ant.types.Commandline
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletHandler
import org.eclipse.jetty.servlet.ServletHolder
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
    private static ProcessUtils processUtils = new ProcessUtils()

    static {
        if (System.getenv("APPVEYOR") != null) {
            git('config --global user.email "bo@gradle.com"', null)
            git('config --global user.name "Bo Zhang"', null)
        }
    }

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

    static ProcessUtils.ProcessResult git(String arg, File workingDir) {
        List args = Commandline.translateCommandline(arg) as List
        Process process = processUtils.run(['git'] + args, null, workingDir)
        ProcessUtils.ProcessResult result = processUtils.getResult(process)
        assert result.code == 0: result.stderr
        result
    }

    static String createRepository(File dir, String fileName) {
        git("init", dir)
        git("config --local commit.gpgsign false", dir)
        IOUtils.write(dir, fileName, '')
        git("add .", dir)
        def result = git("commit -m commit", dir)
        return extractCommitFromStdout(result.stdout)
    }

    static String addFileToRepository(File dir, String fileName, String fileContent = '') {
        IOUtils.write(new File(dir, fileName), fileContent)
        git("add " + fileName, dir)
        def result = git("commit -m commit", dir)
        return extractCommitFromStdout(result.stdout)
    }

    private static String extractCommitFromStdout(String stdout) {
        // [master 310e0ab] commit message
        def m = (stdout.split('\n')[0]) =~ /.+([0-9a-z]{7}).+/
        m.find()
        return m.group(1)
    }

    static void newBranch(File dir, String branch) {
        git("checkout -b " + branch, dir)
    }
}

