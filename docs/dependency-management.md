# Dependency Management

Dependency management is a nightmare. Fortunately, the package management mechanism of Gogradle is excellent enough to handle complicated scenarios.

It's well known that golang doesn't manage packages at all. It assumes that all packages are located in one or more [Workspace](https://golang.org/doc/code.html#Workspaces) specified by `GOPATH`. In build, golang will find required package in `GOPATH`. This caused many issues:

- Lack of package version information makes it difficult to do reproducible and stable build.
- There may be multiple builds at the same time, and they may depend on different versions of a package.
- A project may depend on multiple versions of a package due to transitive dependencies

Therefore, golang introduced a mechanism called [vendor](https://docs.google.com/document/d/1Bz5-UB7g2uPBdOx-rw5t9MxJwkfpx90cqG9AFL0JAYo), allowing dependencies to be managed by vcs along with golang project itself. Some questions are solved, but some more arise:

- Existence of redundant code makes project fatter and fatter
- Existence of multiple versions in a project will cause [problems](https://github.com/blindpirate/golang-broken-vendor)
- Various [external package management tools](https://github.com/golang/go/wiki/PackageManagementTools) aren't compatible with each other.

Gogradle makes efforts to improve the situation. It doesn't follow golang's workspace convention, and uses totally isolated and project-grade workspace instead. All resolved packages will be placed into temp directory of project root, thus `GOPATH` is not required. 

## Dependency Declaration

You can declare dependent packages in `dependencies` block of `build.gradle`. Currently only packages managed by Git are supported. Supports for other vcs are under development.

Some examples are as follows:

```groovy
dependencies {
    golang {
        build 'github.com/user/project'  // No specific version, the latest will be used
        build name:'github.com/user/project' // Equivalent to last line
    
        build 'github.com/user/project@1.0.0-RELEASE' // Specify a version(tag in Git)
        build name:'github.com/user/project', tag:'1.0.0-RELEASE' // Equivalent to last line
        build name:'github.com/user/project', version:'1.0.0-RELEASE' // Equivalent to last line
    
        test 'github.com/user/project#d3fbe10ecf7294331763e5c219bb5aa3a6a86e80' // Specify a commit
        test name:'github.com/user/project', commit:'d3fbe10ecf7294331763e5c219bb5aa3a6a86e80' // Equivalent to last line
    }
}
```

By default, if you don't specify a commit, Gogradle won't do `git pull` or `hg update -u`  in local repository. You can use `--refresh-dependencies` to force Gogradle to do so:
 
```
gradlew goBuild --refresh-dependencies
``` 


[SemVersion](http://semver.org/) is supported in dependency declaration. In Git, a "version" is just a tag. Gogradle doesn't recommend to use SemVersion since it may break reproducibility of build.

```groovy
dependencies {
    golang {
        build 'github.com/user/project@1.*'  // Equivalent to >=1.0.0 & <2.0.0
        build 'github.com/user/project@1.x'  // Equivalent to last line
        build 'github.com/user/project@1.X'  // Equivalent to last line

        build 'github.com/user/project@~1.5' // Equivalent to >=1.5.0 & <1.6.0
        build 'github.com/user/project@1.0-2.0' // Equivalent to >=1.0.0 & <=2.0.0
        build 'github.com/user/project@^0.2.3' // Equivalent to >=0.2.3 & <0.3.0
        build 'github.com/user/project@1' // Equivalent to 1.X or >=1.0.0 & <2.0.0
        build 'github.com/user/project@!(1.x)' // Equivalent to <1.0.0 & >=2.0.0
        build 'github.com/user/project@ ~1.3 | (1.4.* & !=1.4.5) | ~2' // Very complicated expression
    }
}
```

You can specify a url in declaration, which is extremely useful in case of private repository. See [Repository Management](./repository-management.md) for more details.

```groovy
dependencies {
    golang {
        build name: 'github.com/user/project', url:'https://github.com/user/project.git', tag:'v1.0.0'
        build name: 'github.com/user/project', url:'git@github.com:user/project.git', tag:'v2.0.0'
    }
}
```

Multiple dependencies can be declared at the same time:

```groovy
dependencies {
    golang {
        build 'github.com/a/b@1.0.0', 'github.com/c/d@2.0.0', 'github.com/e/f#commitId'
    
        build([name: 'github.com/g/h', version: '2.5'],
               [name: 'github.com/i/j', commit: 'commitId'])
    }
}
```

Gogradle provides support for transitive dependencies. For example, the following declaration excludes transitive dependencies of `github.com/user/project`.

```groovy
dependencies {
    golang {
        build('github.com/user/project') {
            transitive = false
        }
    }
}
```

What's more, you can exclude some specific transitive dependencies. For example, the following declaration excludes all `github.com/c/d` and `github.com/e/f` in a specific version:

```groovy
dependencies {
    golang {
        build('github.com/a/b') {
            exclude name:'github.com/c/d'
            exclude name:'github.com/c/d', tag: 'v1.0.0'
        }
    }
}
```

If you have some packages in local directory, you can declare them with:

```groovy
dependencies {
    golang {
        build name: 'a/local/package', dir: 'path/to/local/package' // It must be absolute
    }
}
```

## Build Dependency and Test Dependency

You may notice that there are always `build` or `test` in dependency declarations. It's a term named [Configuration](https://docs.gradle.org/current/userguide/dependency_management.html#sub:configurations) in Gradle. Gogradle predefined `build` and `test` configuration, which you can see as two independent dependencies set. In build, only `build` dependencies will take effect; in test, both of them will take effect and dependencies in `build` have higher priority.

## Dependency Package Management

There are four kinds of dependency package in Gogradle:

- Package managed by vcs
- Package located in local file system
- Package in vendor directory
- Package imported in go source code

There isn't dependency package in golang's world, golang just treat a ordinary directory as a package. In Gogradle, a dependency package is usually root directory or a repo managed by vcs. For example, all go files in a repository managed by Git belong to one dependency package. Gogradle resolve the package path by [the default golang way](https://golang.org/cmd/go/#hdr-Relative_import_paths).

## Dependency Resolution

Dependency resolution is the process in which a dependency is resolved to some concrete code. This process usually relies on vcs such as Git. The ultimate goal of Gogradle is providing support for all four vcs (Git/Mercurial/Svn/Bazaar) with pure Java implementation. Currently only Git is supported. 

## Transitive Dependency

The dependency of a dependency (transitive dependency) can be from:

- dependencies in vendor directory
- dependencies in lock files
- dependencies in `import` of go source code

By default, Gogradle will read dependencies in vendor directory and lock files as transitive dependencies. If the result set is empty, `import` statement in `.go` source code will be scanned as transitive dependencies.

## Dependency Conflict

In practice, the situation may be extremely complicated due to the existence of transitive dependencies.

When a project depends multiple versions of one package, we say they are conflicted. For example, A depends B in version 1 and C, and C depends B in version 2, then version 1 and version 2 of B is conflicted. Golang's vendor mechanism allow them to exist at the same time, which is opposed by Gogradle. It brings [problems](https://github.com/blindpirate/golang-broken-vendor) sooner or later. Gogradle will resolve all conflict and flatten them, i.e., Gogradle assure that there is only one version for a package in the final build. The final dependencies will be placed into a temp directory in project root.

The conflict resolution strategy is:

- First level package always wins: dependencies declared in project to be built have higher priority

- Newer package wins: newer package have priority over old ones

In detail, Gogradle will detect "update time" of every dependency, and use that time to resolve conflicts.

- Update time of package managed by vcs is the commit time.
- Update time of package in local file system is the last modified time of directory
- Update time of package in vendor directory is determined by its "host" dependency.

## Dependency Lock

Gogradle can lock the dependencies in current build. A file named `gogradle.lock` recording all version information of dependency packages is generated in this task. It can make subsequent build stable and reproducible. Under no circumstances should this file be modified manually. Gogradle encourage to check in this file into vcs.

You can use

```
gradlew goLock
```

or

```
gradlew gL
```

to generate this file.

## Install Dependencies into Vendor

Vendor mechanism is introduced by golang 1.5. It is fully supported but not encouraged by Gogradle. To install dependencies into vendor directory, run:

```
gradlew goVendor
```

or

```
gradlew gV 
```

This task will copy all resolved `build` and `test` dependencies into vendor directory. 

