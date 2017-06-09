# Gogradle - a Full-featured Build Tool for Golang

[中文文档](./README_CN.md)

[![Build Status](https://travis-ci.org/gogradle/gogradle.svg?branch=master)](https://travis-ci.org/gogradle/gogradle)
[![Build Status](https://ci.appveyor.com/api/projects/status/github/gogradle/gogradle?branch=master&svg=true&passingText=windows%20build%20passing&failingText=windows%20build%20failing)](https://ci.appveyor.com/api/projects/status/github/gogradle/gogradle?branch=master&svg=true&passingText=windows%20build%20passing&failingText=windows%20build%20failing)
[![Coverage Status](https://coveralls.io/repos/github/blindpirate/gogradle/badge.svg?branch=master)](https://coveralls.io/github/blindpirate/gogradle?branch=master)
[![Java 8+](https://img.shields.io/badge/java-8+-4c7e9f.svg)](http://java.oracle.com)
[![Apache License 2](https://img.shields.io/badge/license-APL2-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt)

![1](https://raw.githubusercontent.com/blindpirate/gogradle/master/docs/images/go-mini.png)

Gogradle is a gradle plugin which provides support for building golang.

> 2017-04-06 Now Gogradle can build 666 of [Github's top 1000 Go repositories](http://github-rank.com/star?language=Go) **WITHOUT** any extra configuration!
>
> 2017-03-20 Now Gogradle can generate HTML reports for test and coverage! 
>
> 2017-02-26 Now Gogradle can integrate with IDE perfectly!

## What is Gogradle?

Gogradle is a [Gradle](https://gradle.org) plugin which provide modern build support for [Golang](https://golang.org/). Gogradle is deeply inspired by [glide](https://github.com/Masterminds/glide)(I need to pay respect for it). You can simply think Gogradle as `glide`+`make`.

## Why Gogradle?

- `make` has a very steep learning curve, thus many people (like me) aren't good at it; Gradle use a DSL with similar syntax to Java to describe a build, which is more easy for me.
- `Makefile` and `Shell` have cross-platform issues, especially on Windows. Thanks to Gradle and JVM, Gogradle provide an excellent cross-platform support and can leverage the whole Java ecosystem
- There're many mature [plugins](https://plugins.gradle.org) in Gradle ecosystem, and it's easy for youself to implement a plugin to reuse your build code
- Gogradle supports project-scoped `GOPATH`, if you prefer
- Gogradle supports existence and switch of multi-version of Go
- There's plenty of [package management tools](https://github.com/blindpirate/report-of-go-package-management-tool) in Go community which are not compatible with each other.
  - Gogradle provides a [migration command](./docs/getting-started-cn.md#Start) which enables you to migrate from other tools.
  - Gogradle is compatible with `glide/glock/godep/gom/gopm/govendor/gvt/gbvendor/trash/gpm`. When retrieving a dependency package's transitive dependencies, it can recognized lock files of these tools.
- Gogradle has a long-term and active development support

Gogradle implements most features of `glide` and adds some extra features:

- Test and coverage reports generation
- Multi-version management of Go
- Full IDE support (IntelliJIDEA/VSCode/WebStorm/PhpStorm/PyCharm/RubyMine/CLion/Gogland/Vim), especially deep integration with IntelliJIDEA
- Declaration and substitution of repositories, which can be used as mirror repositories

If you puzzled over these issues, or you were a Java developer and familiar with Gradle, Gogradle is your choice!  

Gogradle's objective is not replacing other tools, it only provides an option for developers.

## Feature

- No need to preinstall anything but `JDK 8+` (including golang itself)
  - If you're using JetBrains IDE, then JDK is not required
- Supports Go 1.5+ and allow their existence at the same time
- Perfect cross-platform support (as long as `Java` can be run, all tests have passed on OS X 10.11/Ubuntu 12.04/Windows 7)
- Project-scope build, needless to set `GOPATH`
- Full-featured package management
  - Needless to install dependency packages manually, all you need to do is specifying the version
  - VCS supported: Git/Mercurial
  - Transitive dependency management
  - Resolves package conflict automatically
  - Supports dependency lock
  - Supports importing dependencies managed by various external tools such as glide/glock/godep/gom/gopm/govendor/gvt/gbvendor/trash/gpm (Based on [this report](https://github.com/blindpirate/report-of-go-package-management-tool))
  - Supports [submodule](https://git-scm.com/book/en/v2/Git-Tools-Submodules)
  - Supports [SemVersion](http://semver.org/)
  - Supports [vendor](https://docs.google.com/document/d/1Bz5-UB7g2uPBdOx-rw5t9MxJwkfpx90cqG9AFL0JAYo)
  - Supports flattening dependencies (Inspired by [glide](https://github.com/Masterminds/glide))
  - Supports renaming local packages
  - Supports private repository
  - Support automatic repository url substitution
  - `build`/`test` dependencies are managed separately
  - Supports dependency tree visualization
  - Supports sub packages
- Supports build/test/single-test/wildcard-test/cross-compile
- Modern production-grade support for automatic build, simple to define customized tasks
- Native syntax of gradle
- Additional features for users in mainland China who are behind the [GFW](https://en.wikipedia.org/wiki/Great_Firewall)
- Supports shadowsocks proxy 
- IDE support (IntelliJIDEA/Gogland/Webstorm/PhpStorm/PyCharm/RubyMine/CLion/Vim)
- Test and coverage report generation
- Incremental build 

## How Gogradle works

Gogradle's work is based on [vendor](https://docs.google.com/document/d/1Bz5-UB7g2uPBdOx-rw5t9MxJwkfpx90cqG9AFL0JAYo) mechanism. You declare your build dependencies and build logic with Gradle DSL in `build.gradle`, and Gogradle will resolve all dependencies and potential package conflict, then install them into `vendor` directory and execute a build. In this course, dependency packages will be flattened to avoid [issues](https://github.com/blindpirate/golang-broken-vendor). Later, you can lock your resolved dependencies to ensure a reproducible build. It's up to you whether to check in `vendor` directory or not.

See [here](https://github.com/gogradle/samples) for examples.


## Table of Content

- [Getting Started](./docs/getting-started.md)
- [Dependency Management](./docs/dependency-management.md)
- [Tasks in Gogradle](./docs/tasks.md)
- [Repository Management](./docs/repository-management.md)
- [Set Proxy For Build](./docs/proxy.md)
- [IDE Integration](./docs/ide.md)

Test report

![1](https://raw.githubusercontent.com/blindpirate/gogradle/master/docs/images/index.png)
![1](https://raw.githubusercontent.com/blindpirate/gogradle/master/docs/images/classes.png)
![1](https://raw.githubusercontent.com/blindpirate/gogradle/master/docs/images/packages.png)
![1](https://raw.githubusercontent.com/blindpirate/gogradle/master/docs/images/failedtest.png)

Coverage report

![1](https://raw.githubusercontent.com/blindpirate/gogradle/master/docs/images/coverage.png)
![1](https://raw.githubusercontent.com/blindpirate/gogradle/master/docs/images/coveragepackage.png)

## Contributing

If you like Gogradle, star it please.

Please feel free to submit an [issue](https://github.com/blindpirate/gogradle/issues/new).

Fork and [PR](https://github.com/blindpirate/gogradle/pulls) are always welcomed.
