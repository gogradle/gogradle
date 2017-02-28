# Task in Gogradle

A task unit executed independently is usually called [Task](https://docs.gradle.org/current/userguide/more_about_tasks.html). Gogradle predefined the following tasks:

- prepare
- resolveBuildDependencies
- resolveTestDependencies
- dependencies
- installBuildDependencies
- installTestDependencies
- build
- test
- clean
- check
- lock
- vendor

## prepare

Do some preparation, for example, verifying `build.gradle` and installing golang executables.

## resolveBuildDependencies/resolveTestDependencies

Resolve `build` and `test` dependencies to dependency trees. Conflicts will also be resolved in this task.

## dependencies

Display the dependency tree of current project. It's very useful when you need to resolve package conflict manually.

## installBuildDependencies/installTestDependencies

Flatten resolved `build` and `test` dependencies and install them into `.gogradle` directory respectively so that the future build can use them.

## build

Do build. This task is equivalent to:

```
cd <project path>
export GOPATH=<build dependencies path>
go build -o <output path> 
```

## test

Do test. This task is equivalent to:

```
cd <project path>
export GOPATH=<build dependencies path>:<test dependencies path>
go test
```

## check

This task is usually executed by CI to do some checking, such as test coverage rate. It depends on test task by default.

## clean

Clean temp files in project.

## lock

Generate dependency lock file. See [Dependency Lock](./getting-started.md#dependency-lock)

## vendor

Install resolved `build` dependencies into vendor directory. See [Install Dependencies into Vendor](./getting-started.md#install-dependencies-into-vendor). 


