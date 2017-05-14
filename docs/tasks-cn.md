# Gogradle的任务

在Gradle的构建模型中，一个独立执行的任务单元称为[Task](https://docs.gradle.org/current/userguide/more_about_tasks.html)。Gogradle预定义了以下任务：

- goPrepare
- goInit
- resolveBuildDependencies
- resolveTestDependencies
- installBuildDependencies
- installTestDependencies
- goDependencies
- goBuild
- goTest
- gofmt
- goVet
- goCover
- goClean
- goCheck
- goLock
- goVendor

下面将对这些任务进行介绍。

## goPrepare

进行一些准备工作，例如`build.gradle`中配置的合法性校验、指定Go语言版本的下载与安装。

## goInit

执行从其他包管理工具的迁移工作。当前支持的包管理工具有：`glide/glock/godep/gom/gopm/govendor/gvt/gbvendor/trash`

## resolveBuildDependencies/resolveTestDependencies

分别解析`build`和`test`的依赖，生成依赖树。在这个过程中会解决相关依赖之间的冲突。

## installBuildDependencies/installTestDependencies

将解析完成的`build`和`test`进行扁平化，然后安装到项目目录的`.gogradle`文件夹中，以备构建使用。

## goDependencies

显示当前项目的依赖树。这对于包冲突的解决非常有用。

## goBuild

执行构建工作。这等价于：

```
cd <project path>
export GOPATH=<build dependencies path>
go build -o <output path> 
```

## goTest

执行测试工作。Gogradle会扫描所有的包并逐个测试，这是为了生成测试报告。

## gofmt

运行[`gofmt`](https://golang.org/cmd/gofmt/)，默认情况下，它会使用`-w`修改项目中的文件。若不希望如此，或者想添加其他的参数，使用：

```groovy
gofmt {
    gofmt "-r '(a) -> a' -l *.go"
}
```

## goVet

运行[go vet](https://golang.org/cmd/vet/)。默认情况下，若`go vet`返回值非零，该任务会失败。由于`go vet`可能不准确，可以使用以下配置忽略错误：

```groovy
goVet {
    continueWhenFail = true
}
```

## goCover 
生成测试报告，默认位于 `<project root>/.gogradle/reports/coverage`

## goCheck

通常该任务被CI系统调用，用于执行代码检查，例如覆盖率、代码风格等。默认依赖goTest任务、goVet任务和gofmt任务。

## goClean

清除项目中的临时文件。

## goLock

生成依赖锁定文件。详见[依赖锁定](./getting-started-cn.md#依赖锁定)。

## goVendor

将解析后的`build`依赖安装到vendor目录。详见[依赖安装到vendor目录](./dependency-management-cn.md#依赖安装到vendor目录)。

# 自定义任务

Gogradle支持自定义的go任务。例如，若希望添加一个任务，运行[`golint`](https://github.com/golang/lint)检查：

build.gradle:

```groovy
task golint(type: com.github.blindpirate.gogradle.Go){
    doLast {
        run 'golint github.com/my/project'
    }
}

goCheck.dependsOn golint
```

如此即可。欲了解更多有关任务的信息，请参考[官方文档](https://docs.gradle.org/current/userguide/more_about_tasks.html)



