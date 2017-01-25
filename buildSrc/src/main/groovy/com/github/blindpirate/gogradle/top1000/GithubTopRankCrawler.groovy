package com.github.blindpirate.gogradle.top1000

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

import java.nio.file.Path

class GithubTopRankCrawler {
    static String TARGET_URL = 'https://api.github.com/search/repositories?q=stars%3A%3E1+language%3A${language}&sort=stars&order=desc&type=Repositories&page=${page}&per_page=100'
    static String TOP_JSON = 'top.json'

    static void cloneAllInto(File dir, boolean refresh) {
        getTop1000('go', dir).each {
            cloneOne(dir.toPath(), it.full_name, it.clone_url, refresh)
        }
    }

    static List getTop1000(String language, File baseDir) {
        File topDotJson = new File(baseDir, TOP_JSON)
        if (topDotJson.exists()) {
            return new JsonSlurper().parseText(topDotJson.getText())
        } else {
            String url = TARGET_URL.replace('${language}', language)
            List allItems = (1..10).collect({ it ->
                String json = new URL(url.replace('${page}', it.toString())).getText()
                return new JsonSlurper().parseText(json).items
            }).flatten()
            topDotJson.write(JsonOutput.toJson(allItems))
            return allItems
        }
    }

    // a/b  https://github.com/a/b.git
    static void cloneOne(Path baseDir, String fullName, String cloneUrl, boolean refresh) {
        Path location = baseDir.resolve(fullName.replaceAll(/\//, '_'))
        if (location.toFile().exists()) {
            if (refresh) {
                runInheritIO(['git', 'pull'], [:])
            } else {
                println("${fullName} exists, skip.")
            }
        } else {
            runInheritIO(['git', 'clone', cloneUrl, location.toAbsolutePath().toString()], [:])
        }
    }

    static void runInheritIO(List<String> args, Map<String, String> envs) {
        ProcessBuilder pb = new ProcessBuilder().command(args).inheritIO()
        pb.environment().putAll(envs)
        pb.start().waitFor()
    }
}

