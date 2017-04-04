# Task in Gogradle

A task unit executed independently is usually called [Task](https://docs.gradle.org/current/userguide/more_about_tasks.html). Gogradle predefined the following tasks:

- prepare
- resolveBuildDependencies
- resolveTestDependencies
- dependencies
- build
- test
- fmt 
- vet 
- cover 
- check
- clean
- lock
- vendor

## prepare

Do some preparation, for example, verifying `build.gradle` and installing golang executables.

## resolveBuildDependencies/resolveTestDependencies

- Resolve `build` and `test` dependencies to dependency trees. Conflicts will also be resolved in this task.
- Flatten resolved `build` and `test` dependencies and install them into `.gogradle` directory respectively so that the future build can use them.

## dependencies

Display the dependency tree of current project. It's very useful when you need to resolve package conflict manually.

## build

Do build. This task is equivalent to:

```
cd <project path>
export GOPATH=<build dependencies path>
go build -o <output path> 
```

## test

Do test.

It will scan all packages in your project and test them one by one so that test reports can be generated. 
 
## fmt 

Run [gofmt](https://golang.org/cmd/gofmt/) on the whole project. It will use `-w` by default, so your code will be modified.
If the default behavior is not expected, or you want to change the command line argument, use following code to configure it:

```groovy
fmt {
    gofmt "-r '(a) -> a' -l *.go"
}
```

## vet 

Run [go vet](https://golang.org/cmd/vet/) on the whole project. By default, it will fail the build if `go vet` return non-zero.
Since `go vet` is inaccurate, you can ignore the error:

```groovy
vet {
    continueWhenFail = true
}
```
## cover 

Generate coverage reports. It will be placed into `<project root>/.gogradle/reports/coverage`

## check

This task is usually executed by CI to do some checking, such as test coverage rate. It depends on `test`/`cover`/`fmt`/`vet` task by default.

## clean

Clean temp files in project.

## lock

Generate dependency lock file. See [Dependency Lock](./getting-started.md#dependency-lock)

## vendor

Install resolved `build` dependencies into vendor directory. See [Install Dependencies into Vendor](./dependency-management.md#install-dependencies-into-vendor). 

# Define custom task

Gogradle support customized go task. For example, we want to add a task which runs [`golint`](https://github.com/golang/lint) on my project.

build.gradle:

```groovy
task golint(type: com.github.blindpirate.gogradle.Go){
    doLast {
        run 'golint github.com/my/project'
    }
}

check.dependsOn golint
```

That's all. More about tasks, please see the [doc](https://docs.gradle.org/current/userguide/more_about_tasks.html)


