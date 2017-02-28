# Gogradle的任务

在Gradle的构建模型中，一个独立执行的任务单元称为[Task](https://docs.gradle.org/current/userguide/more_about_tasks.html)。Gogradle预定义了以下任务：

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

下面将对这些任务进行介绍。

## prepare

进行一些准备工作，例如`build.gradle`中配置的合法性校验、指定Go语言版本的下载与安装。

## resolveBuildDependencies/resolveTestDependencies

分别解析`build`和`test`的依赖，生成依赖树。在这个过程中会解决相关依赖之间的冲突。

## dependencies

显示当前项目的依赖树。这对于包冲突的解决非常有用。

## installBuildDependencies/installTestDependencies

将解析完成的`build`和`test`进行扁平化，然后安装到项目目录的`.gogradle`文件夹中，以备构建使用。

## build

执行构建工作。这等价于：

```
cd <project path>
export GOPATH=<build dependencies path>
go build -o <output path> 
```

## test

执行测试工作。这等价于：

```
cd <project path>
export GOPATH=<build dependencies path>:<test dependencies path>
go test
```

## check

通常该任务被CI系统调用，用于执行代码检查，例如覆盖率、代码风格等。默认依赖test任务。

## clean

清除项目中的临时文件。

## lock

生成依赖锁定文件。详见[依赖锁定](#依赖锁定)。

## vendor

将解析后的`build`依赖安装到vendor目录。详见[依赖安装到vendor目录](#依赖安装到vendor目录)。


