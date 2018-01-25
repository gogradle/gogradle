# Task in Gogradle

A task unit executed independently is usually called [Task](https://docs.gradle.org/current/userguide/more_about_tasks.html). Gogradle predefined the following tasks:

- General tasks
  - clean
  - prepare
  - showGopathGoroot
- Initialization tasks
  - init
- Dependency tasks
  - resolveBuildDependencies
  - resolveTestDependencies
  - installDependencies
  - dependencies
  - venodr
  - lock
- Build tasks
  - build
  - test
  - coverage
  - vet
  - fmt
  - check

![1](https://raw.githubusercontent.com/blindpirate/gogradle/master/docs/images/tasks.png)

Before starting introduction, we assume your project's import path is `github.com/my/project` and your system is Windows x64.

## clean

Clean temp files in project, i.e. `.gogradle` directory.

## prepare

Do some preparation, for example, verifying `build.gradle` and installing golang executables.

## showGopathGoroot

Depends on `prepare`. Because Gogradle support project-level `GOPATH` and multiple version of Go, this can be used to display present `GOPATH` and `GOROOT`.

## init

Depends on `prepare`. Perform migration from other package management tools. Currently `glide/glock/godep/gom/gopm/govendor/gvt/gbvendor/trash/gpm` are supported.

## resolveBuildDependencies/resolveTestDependencies

Depends on `prepare`. Resolve `build` and `test` dependencies to dependency trees. Conflicts will also be resolved in this task.

## installDependencies

For internal use. Do not use this task direcly. This task examine the existence of `resolveBuildDependencies/resolveTestDependencies` task, and install corresponding dependencies into `vendor`. `build` dependencies have higher priority than `test` dependencies.

## dependencies

Depends on `resolveBuildDependencies/resolveTestDependencies`. Display the dependency tree of current project. It's very useful when you need to resolve package conflict manually.

## vendor

Depends on `resolveBuildDependencies/resolveTestDependencies/installDependencies`. Install both `build` and `test` dependencies into vendor directory. See [Install Dependencies into Vendor](./dependency-management.md#install-dependencies-into-vendor). 

## lock

Depends on `vendor`. Generate dependency lock file. See [Dependency Lock](./getting-started.md#dependency-lock)

## build

Depends on `resolveBuildDependencies/installDependencies`. Do build. By default is equivalent to:

```
go build github.com/my/project -o .gogradle/windows_amd64_project
```

You can configure it as follows:

```
build {
    // Cross-compile output
    targetPlatform = ['windows-amd64', 'linux-amd64', 'linux-386']
    
    // Output location, can be relative path (to project root) or absolute path
    // The ${} placeholder will be rendered in cross-compile
    outputLocation = './.gogradle/${GOOS}_${GOARCH}_${PROJECT_NAME}${GOEXE}'
}
```

This code snippet tells Gogradle to run `go build` three times with different environments and generate three output. The result is in `.gogradle` directory:

- windows_amd64_project.exe
- linux_amd64_project
- linux_386_project

If your main package is not located in your project root, or you want to add some custom command line arguments, you need:

```
build {
    go 'build -o ./gogradle/output --my-own-cmd-arguments github.com/my/package/my/subpackage'
}
```

Note the quote after `go`.

## test

Do test.

Depends on `resolveBuildDependencies/resolveTestDependencies/installDependencies`. It will scan all packages in your project and test them one by one so that test reports can be generated. Assume your project contains several sub packages `github.com/my/project/sub1`,`github.com/my/project/sub2`, ..., `github.com/my/project/subN`, Gogradle will test these N packages and generate HTML reports for them. The reports will be placed in `<project root>/.gogradle/reports/test`.

## fmt 

Depends on `prepare`. Run [gofmt](https://golang.org/cmd/gofmt/) on the whole project. It will use `-w` by default, so your code will be modified.
If the default behavior is not expected, or you want to change the command line argument, use following code to configure it:

```groovy
fmt {
    gofmt "-r '(a) -> a' -l *.go"
}
```

## vet 

Depends on `vendor`. Run [go vet](https://golang.org/cmd/vet/) on the whole project. By default, it will fail the build if `go vet` return non-zero.
Since `go vet` is inaccurate, you can ignore the error:

```groovy
vet {
    continueOnFailure = true
}
```
## coverage

Depends on `test`. Generate coverage reports. It will be placed into `<project root>/.gogradle/reports/coverage`

## check

This task is usually executed by CI to do some checking, such as test coverage rate. It depends on `test`/`fmt`/`vet` task by default.


# Define Custom Task

Gogradle support customized go task. See [Custom task](./getting-started.md#custom-task) for more details.


That's all. More about tasks, please see the [doc](https://docs.gradle.org/current/userguide/more_about_tasks.html)

## Task Name Conflict

It's likely that Gogradle default task `build`/`test` conflict with other plugin. To solve this problem, add argument `-Dgogradle.alias=true` in your comman line:

`gradlew goBuild -Dgogradle.alias=true`

| Default Name | Alias          |
|--------------|----------------|
| clean        | goClean        |
| prepare      | goPrepare      |
| init         | goInit         |
| dependencies | goDependencies |
| vendor       | goVendor       |
| lock         | goLock         |
| build        | goBuild        |
| test         | goTest         |
| coverage     | goCover        |
| vet          | goVet          |
| fmt          | gofmt          |
| check        | goCheck        |

Then you can use these aliases to do the build.

You can also specify this argument in project or global `gradle.properties`:

For example, to make this argument global, modify `~/.gradle/gradle.properties` (create it if it doesn't exist) and add a line:

```properties
org.gradle.jvmargs=-Dgogradle.alias=true
```

For more details, please consult [Gradle doc](https://docs.gradle.org/3.3/userguide/build_environment.html#sec:gradle_configuration_properties)


