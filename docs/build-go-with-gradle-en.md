# Build Go Project With Gradle

Tired of global `GOPATH`, or not good at writing `Makefile`, or fond of IDE support like `Java`? Try [Gogradle](https://github.com/gogradle/gogradle).

The target audience of this article are Go developers, especially those who have experience in Java and Gradle.

## What is Gogradle

Gogradle is a Gradle plugin. [Gradle](https://gradle.org) is a modern build tool which is similar to GNU Make. User can define custom build logic with DSLs (Domain Specific Language).
Java and Android developers might feel familiar with it, since it is dominant in Java and official build tool of Android. According a [survey](https://github.com/blindpirate/report-of-build-tools-for-java-and-golang), in January 2017, 62.7% of [Github Top 1000 Java Projects](http://github-rank.com/star?language=Java) use Gradle, and only 26.4% use Maven. Gradle is backed by a company, thus it has a very active development. 

![1](https://raw.githubusercontent.com/blindpirate/report-of-build-tools-for-java-and-golang/master/trending.png)

Gradle has a much better plugin system than `make` (It seems that [Make 4.0](https://debian-administration.org/article/706/GNU_Make_4.0_released_including_support_for_plugins) supports plugin, but the tragedy is you can only write plugin with C). In the world of `make`, if you want to reuse some build logic, the common solution is `Shell` script, which means poor cross-platform compatibility (Windows users are crazy). In `Gradle`, there are about 3000 plugins available in community and countless non-public plugins used in companies all over the world, according to Eric Wendelin, Gradle core team leader.

Gogradle is a Gradle plugin providing Go build support. You can simply think it as `glide`+`make`. It implements most features of `glide` and adds lots of new features.

## Why Gogradle

- Gradle is based on Groovy and JVM and has an excellent compatibility. It's easy to use and there are many wheels in JVM ecosystem (Java/Groovy/Scala/Kotlin).
- There are many plugins in [Gradle ecosystem](https://plugins.gradle.org/)
- Gradle has many features:
  - Task dependencies and automatic task DAG 
  - [Up-to-date check](https://docs.gradle.org/current/userguide/more_about_tasks.html#sec:up_to_date_checks)
  - Gradle wrapper, automatic specific Gradle version downloading to support reproducible build
  - ...
- Gogradle supports project-level `GOPATH`, if you prefer
- It's not required to pre-install Go. Gogradle can download it automatically. Gogradle supports existence and switch of multiple Go version
- There are plenty of package management tools in community and they're not compatible with each other.
  - Gogradle provides import commands to simplify the migration.
  - Gogradle is compatible with `glide/glock/godep/gom/gopm/govendor/gvt/gbvendor/trash/gpm`. When looking up a package's transitive dependencies, it can recognize these tools automatically.
- Gogradle has many extra features:
  - Test and coverage HTML reports generation
  - IDE support
  - Repository substitution and management, easy to set a mirror repository.

Gogradle is [here](https://github.com/gogradle/gogradle). Its objective is not to replace other tools but to give an extra choice to developers. If you've been bothered by issues mentioned above, or you're familiar with Java and Gradle, Gogradle is your choice.

Here is test reports of [`gogs`](https://github.com/gogits/gogs):

![1](https://raw.githubusercontent.com/blindpirate/gogradle/master/docs/images/classes.png)

And coverage report:

![1](https://raw.githubusercontent.com/blindpirate/gogradle/master/docs/images/coverage.png)
![1](https://raw.githubusercontent.com/blindpirate/gogradle/master/docs/images/coveragepackage.png)

Gogradle will resolve that package and all its transitive packages, fix potential conflict, and install it into `vendor`. Other IDE has no native Gradle support so some command line stuff is required. Gogradle support IDE. See [IDE integration](https://github.com/gogradle/gogradle/blob/master/docs/ide.md) for more details.

## Get started 

Assume you have a computer with only operation system and `Git`, let's talk about how to build Go with Gradle from scratch.

### Install JRE and IDE

All Gogradle needs is a JVM. Now you need to [install JDK or JRE 8+](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html). However, if you decide to use JetBrains IDE (IntellijIDEA/GoLand/WebStorm/PhpStorm/PyCharm/RubyMine/CLion), You can leverage the JRE shipped with it without extra installation. See [IDE integration](https://github.com/gogradle/gogradle/blob/master/docs/ide.md) to set configure that. Similarly, if you decide to use VSCode or Vim, you should install corresponding plugins as described in that documentation.

### Copy Gogradle scripts

Copy `gradle` directory/`gradlew`/`gradlew.bat` of [Gogradle](https://github.com/gogradle/gogradle) into your project root direcotry. This is a mechanism named `Wrapper` by Gradle. When wrapper scripts execute, it will download the correct version of Gradle. Therefore, we don't need to install Gradle actually. 

### Initialization

Create a new file named `build.gradle` in your project root:

```
plugins {
    id 'com.github.blindpirate.gogradle' version '0.10' // Please use the latest version
}

golang {
    packagePath = 'github.com/your/package' // import path of project, not file system path!
}
```

Enter your project root and run

```
./gradlew init # *nix

gradlew init # Windows
```

Hereinafter, we will use uniform command form `gradlew <task>` on both *nix and Windows. This command will scan your project and find dependencies. Particularly, if you used one of `glide/glock/godep/gom/gopm/govendor/gvt/gbvendor/trash/gpm` tools, Gogradle can recognize its configuration files.

`build.gradle` is written in a kind of DSL based on [`Groovy`](http://groovy-lang.org/). It specify the build logic. You can think Groovy as a super set of Java, and don't need to go into it.

### Build

Run `gradlew build` in project root. It will resolve all dependencies and transitive dependencies, resolve conflict, and install them into project root and invoke `go build`.

You may have doubts, WTF, I haven't installed Go! It's OK, if Gogradle fails to find Go on your machine, it will download and install latest version of Go automatically. 

And it's not required to set `GOPATH`. If Gogradle find you haven't set a `GOPATH`, it will create a symbolic link in `.gogradle` directory of you project root and use it as the project-level `GOPATH`. Because all dependencies will be installed into `vendor`, `cannot find package` won't happen.

Of course, if you have already installed Go and set `GOPATH`, Gogradle will use them directly.

See [build task](https://github.com/gogradle/gogradle/blob/master/docs/tasks.md#build) for more details.

![1](http://gogradle.oss-cn-hongkong.aliyuncs.com/build.png)

This is a snapshot of `build` task, you can see tasks executed.

### Test

Run `gradlew test` in project root. It will test each package and generate test/coverage report in HTML format as mentioned above. Does it looks better than native `go test`?

![1](https://raw.githubusercontent.com/blindpirate/gogradle/master/docs/images/failedtest.png)

![1](http://gogradle.oss-cn-hongkong.aliyuncs.com/test.png)

This build failed due to several failed tests. The output indicates the location of test reports.

### Check

Gogradle put several comman code check task into a `check` task. By default, it is out-of-the-box and depends on `vet`/`fmt` and `cover`.

![1](http://gogradle.oss-cn-hongkong.aliyuncs.com/check.png)

In this build, `check` is executed because `build` depends on `check`.

See [Tasks in Gogradle](https://github.com/gogradle/gogradle/blob/master/docs/tasks.md) for more details.

### dependency management 

We can declare our dependencies in `build.gradle`. Gogradle will retrieve and download all dependencies and transitive dependencies. The following code gives some examples of dependency declaration (in `build.gradle`):

```
dependencies {
    golang {
        build 'github.com/user/project'  // No version specified, use latest
        build name:'github.com/user/project' // Equivalent to last line 
    
        build 'github.com/user/project@1.0.0-RELEASE' // specify a tag
        build name:'github.com/user/project', tag:'1.0.0-RELEASE' // Equivalent to last line 

        build name: 'github.com/user/project', url:'https://github.com/user/project.git', tag:'v1.0.0' // specify a url
    
        test 'github.com/user/project#d3fbe10ecf7294331763e5c219bb5aa3a6a86e80' // specify a commit
        test name:'github.com/user/project', commit:'d3fbe10ecf7294331763e5c219bb5aa3a6a86e80' // Equivalent to last line 

        // semantic version 
        build 'github.com/user/project@1.*'  // Equivalent to >=1.0.0 & <2.0.0
        build 'github.com/user/project@1.x'  // Equivalent to last line
        build 'github.com/user/project@1.X'  // Equivalent to last line
        build 'github.com/user/project@~1.5' // Equivalent to >=1.5.0 & <1.6.0
        build 'github.com/user/project@1.0-2.0' // Equivalent to >=1.0.0 & <=2.0.0
        build 'github.com/user/project@^0.2.3' // Equivalent to >=0.2.3 & <0.3.0
        build 'github.com/user/project@1' // Equivalent to 1.X or >=1.0.0 & <2.0.0
        build 'github.com/user/project@!(1.x)' // Equivalent to <1.0.0 & >=2.0.0
        build 'github.com/user/project@ ~1.3 | (1.4.* & !=1.4.5) | ~2' // Very complicated expression

        build 'github.com/a/b@1.0.0', 'github.com/c/d@2.0.0', 'github.com/e/f#commitId' // declare multiple dependencies 

        // declare a dependency and forbid all its transitive dependencies
        build('github.com/user/project') {
            transitive = false
        }

        // declare a dependency and exclude some trasitive dependencies
        build('github.com/a/b') {
            exclude name:'github.com/c/d'
            exclude name:'github.com/c/d', tag: 'v1.0.0'
        }

        build name: 'github.com/big/package', subpackages: ['.', 'sub1', 'sub2/subsub'] // depends on some sub packages only
    }
}
```

We can see `build` and `test` before each declared dependeny, which Java develooer may be familiar with. Gogradle provides a mechanism of package isolation. In `build` task, only `build` dependencies take effect; in `test` task, both `build` and `test` dependencies take effect. The advantage is, assume we have a common library A, it depends on some libraries used only in test, this mechanism can tell A's client, 'hey, these libraries are only used in A's test, you don't need to pull them into your vendor'. In this way, the redundant dependencies can be avoided. 

See [Dependency Management](https://github.com/gogradle/gogradle/blob/master/docs/dependency-management.md) for more details.

### Display Dependency Tree

In the course of dependency management, we will encounter package conflict inevitably and have to deal with it manully. In this case, we can use:

```
gradlew dependencies
```

It will print current dependecny tree:


```
build:

github.com/gogits/gogs
|-- github.com/Unknwon/cae:c6aac99
|-- github.com/Unknwon/com:28b053d
|-- github.com/Unknwon/i18n:39d6f27
|   |-- github.com/Unknwon/com:28b053d (*)
|   \- gopkg.in/ini.v1:766e555 -> 6f66b0e
|-- github.com/Unknwon/paginater:701c23f
|-- github.com/bradfitz/gomemcache:2fafb84
|-- github.com/go-macaron/binding:4892016
|   |-- github.com/Unknwon/com:28b053d (*)
|   \- gopkg.in/macaron.v1:ddb19a9
|       |-- github.com/Unknwon/com:28b053d (*)
|       |-- github.com/go-macaron/inject:d8a0b86 -> c5ab7bf
|       \- gopkg.in/ini.v1:766e555 -> 6f66b0e (*)
... 

```

For example, it's part of `gogs`'s dependency tree. The arrow indicates that some dependencies conflict and have been resolved. The conflict resolution strategy is:

- First level wins: dependencies declared in root project wins.
- The newer wins: a commit with newer commit time will defeat one with older commit time.

At last, Gogradle will ensure package with same name appears only once in `vendor`. It's very similar to Java dependencies resolution.

### Custom Repositories and Mirror Repositories

Why do we need custom repositories and mirror repositories? Consider the following cases:

- You fork `github.com/foo/bar` to your repository `github.com/my/bar` and make some modification. You're so proud of your modification that you want it used in any projects evermore. It means that in any code, `github.com/foo/bar` should be replaced with your own implementation `github.com/my/bar`. Notice, anywhere, your projects, your projects' transitive dependencies, vendor in your projects' transitive dependencies.
- A company want to set a mirror of Github to limit the internal access or speed up.

Gogradle provides a extremely flexible mechanism to solve the problems.

To substitute a package globally, we should add some code in `build.gradle`:

```
repositories {
  golang {
        root 'github.com/foo/bar'
        url 'https://github.com/my/bar.git'
        vcs 'git' // ????????????git,???????????????
    }
}
```

It tells Gogradle, whenever it encountered `github.com/foo/bar` package, use `https://github.com/my/bar.git` instead.

In second case, we need to add the following code to `build.gradle`:

```
repositories {
    golang {
        root ~/github\.com\/[\w-]+\/[\w-]+/  // any path matching this regular expression will be passed to the url closure, resulting in a substituted url
        url { path->
            def split = path.split('/')
           "https://my-repo.com/${split[1]}/${split[2]}" 
        }
    }
}   
```

`root` can accept any type, including string, regular expression and closure. In this example, all package path will be compared with `~/github\.com\/[\w-]+\/[\w-]+/`. If matched, it will be passed into `url` closure and the final url is returned.

These example are simple but representative. In fact, you can write any code in Gradle build script, and use any library of JVM ecosystem, which makes Gogradle extremely powerful.

You may doubt that user have to add such long configurations into every project. Here's a solution: write a Gradle plugin and move all logic into it. In this way, wherever you need to use these logic, just write a line:

```
apply plugin:'my.custom.repositories.management'
```

See [Repository Management](https://github.com/gogradle/gogradle/blob/master/docs/repository-management.md) for more details.

### IDE Support

To see the details of IDE support, please consult [Gogradle IDE intergration](https://github.com/gogradle/gogradle/blob/master/docs/ide.md).

## Epilogue 

It needs to be emphasized that Gogradle is not a toy. In an experimental project, I succeed in building [`docker`](https://github.com/gogradle/moby) with Gogradle. It is still in heavy development and new features are added every week. Forks and issues are always welcomed. 

The purpose of this article is just to make a simple introduction. Please visit [https://github.com/gogradle/gogradle](https://githubu.com/gogradle/gogradle) for detailed documentation.

At last, you can visit [talk on Gradle Summit 2017](https://www.youtube.com/watch?v=Mvf3gY1MopE&t=350s) for a talk on Gogradle. Hope that helps.

