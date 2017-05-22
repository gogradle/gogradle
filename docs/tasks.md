# Task in Gogradle

A task unit executed independently is usually called [Task](https://docs.gradle.org/current/userguide/more_about_tasks.html). Gogradle predefined the following tasks:

- General tasks
  - goClean
  - goPrepare
  - showGopathGoroot
- Initialization tasks
  - goInit
- IDE tasks
  - goIdea
  - gogland/phpStorm/webStorm/rubyMine/cLion/pyCharm
  - vscode
- Dependency tasks
  - resolveBuildDependencies
  - resolveTestDependencies
  - installDependencies
  - goDependencies
  - goVenodr
  - goLock
- Build tasks
  - goBuild
  - goTest
  - goCover
  - goVet
  - gofmt
  - goCheck

![1](https://raw.githubusercontent.com/blindpirate/gogradle/master/docs/images/tasks.png)

Before starting introduction, we assume your project's import path is `github.com/my/project` and your system is Windows x64.

## goClean

Clean temp files in project, i.e. `.gogradle` directory.

## goPrepare

Do some preparation, for example, verifying `build.gradle` and installing golang executables.

## showGopathGoroot

Depends on `goPrepare`. Because Gogradle support project-level `GOPATH` and multiple version of Go, this can be used to display present `GOPATH` and `GOROOT`.

## goInit

Depends on `goPrepare`. Perform migration from other package management tools. Currently `glide/glock/godep/gom/gopm/govendor/gvt/gbvendor/trash/gpm` are supported.

## goIdea

Depends on `goVendor`. Generates project files for `IntelliJIDEA`. Supports Community Edition and Ultimate Edition. Needs Go plugin. See [IDE integration](./ide.md) for more details.

## gogland/phpStorm/webStorm/rubyMine/cLion/pyCharm

Depends on `goVendor`. Generates project files for these IDEs. Needs Go plugin. See [IDE integration](./ide.md) for more details.

## vscode

Depends on `goVendor`. Generates `.vscode/settings.json` for VSCode. Need Go plugin. See [IDE integration](./ide.md) for more details.

## resolveBuildDependencies/resolveTestDependencies

Depends on `goPrepare`. Resolve `build` and `test` dependencies to dependency trees. Conflicts will also be resolved in this task.

## installDependencies

For internal use. Do not use this task direcly. This task examine the existence of `resolveBuildDependencies/resolveTestDependencies` task, and install corresponding dependencies into `vendor`. `build` dependencies have higher priority than `test` dependencies.

## goDependencies

Depends on `resolveBuildDependencies/resolveTestDependencies`. Display the dependency tree of current project. It's very useful when you need to resolve package conflict manually.

## goVendor

Depends on `resolveBuildDependencies/resolveTestDependencies/installDependencies`. Install both `build` and `test` dependencies into vendor directory. See [Install Dependencies into Vendor](./dependency-management.md#install-dependencies-into-vendor). 

## goLock

Depends on `goVendor`. Generate dependency lock file. See [Dependency Lock](./getting-started.md#dependency-lock)

## goBuild

Depends on `resolveBuildDependencies/installDependencies`. Do build. By default is equivalent to:

```
go build github.com/my/project -o .gogradle/windows_amd64_project
```

You can configure it as follows:

```
goBuild {
    // Cross-compile output
    targetPlatform = 'windows-amd64, linux-amd64, linux-386'
    
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
goBuild {
    doLast {
        go 'build -o ./gogradle/output github.com/my/package/my/subpackage --my-own-cmd-arguments'
    }
}
```

Note the quote after `go`.

## goTest

Do test.

Depends on `resolveBuildDependencies/resolveTestDependencies/installDependencies`. It will scan all packages in your project and test them one by one so that test reports can be generated. Assume your project contains several sub packages `github.com/my/project/sub1`,`github.com/my/project/sub2`, ..., `github.com/my/project/subN`, Gogradle will test these N packages and generate HTML reports for them. The reports will be placed in `<project root>/.gogradle/reports/test`.

## gofmt 

Depends on `goPrepare`. Run [gofmt](https://golang.org/cmd/gofmt/) on the whole project. It will use `-w` by default, so your code will be modified.
If the default behavior is not expected, or you want to change the command line argument, use following code to configure it:

```groovy
gofmt {
    gofmt "-r '(a) -> a' -l *.go"
}
```

## goVet 

Depends on `goVendor`. Run [go vet](https://golang.org/cmd/vet/) on the whole project. By default, it will fail the build if `go vet` return non-zero.
Since `go vet` is inaccurate, you can ignore the error:

```groovy
goVet {
    continueWhenFail = true
}
```
## goCover 

Depends on `goTest`. Generate coverage reports. It will be placed into `<project root>/.gogradle/reports/coverage`

## goCheck

This task is usually executed by CI to do some checking, such as test coverage rate. It depends on `goTest`/`gofmt`/`goVet` task by default.


# Define custom task

Gogradle support customized go task. See [Custom task](./getting-started.md#custom-task) for more details.


That's all. More about tasks, please see the [doc](https://docs.gradle.org/current/userguide/more_about_tasks.html)


