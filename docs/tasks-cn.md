# Gogradle的任务

在Gradle的构建模型中，一个独立执行的任务单元称为[Task](https://docs.gradle.org/current/userguide/more_about_tasks.html)。Gogradle预定义了以下任务：

- 通用任务
  - clean
  - prepare
  - showGopathGoroot
- 初始化任务
  - init
- IDE任务
  - goIdea
  - gogland/phpStorm/webStorm/rubyMine/cLion/pyCharm
  - vscode
- 依赖相关任务
  - resolveBuildDependencies
  - resolveTestDependencies
  - installDependencies
  - dependencies
  - venodr
  - lock
- 构建相关任务
  - build
  - test
  - coverage
  - vet
  - fmt
  - check

![1](https://raw.githubusercontent.com/blindpirate/gogradle/master/docs/images/tasks.png)

下面将对这些任务进行介绍。在开始前，我们假定你的项目的包路径是`github.com/my/project`，你的系统环境是Windows x64。

## clean

清除项目中的临时文件，即`.gogradle`目录。

## prepare

进行一些准备工作，例如`build.gradle`中配置的合法性校验、指定Go语言版本的下载与安装。

## showGopathGoroot

依赖`prepare`任务。因为Gogradle支持项目级的`GOPATH`和多Go版本的共存，此任务用于显示当前构建使用的`GOPATH`和`GOROOT`。

## init

依赖`prepare`任务。执行从其他包管理工具的迁移工作。当前支持的包管理工具有：`glide/glock/godep/gom/gopm/govendor/gvt/gbvendor/trash/gpm`。

## goIdea

依赖`vendor`任务，生成IntelliJIDEA的工程文件，支持社区版和旗舰版。需要安装Go插件，详见[IDE支持](./ide-cn.md)。

## gogland/phpStorm/webStorm/rubyMine/cLion/pyCharm

依赖`vendor`任务，生成这些IDE的相关工程文件。需要安装Go插件，详见[IDE支持](./ide-cn.md)。

## vscode

依赖`vendor`任务，生成`.vscode/settings.json`。需要安装Go插件，详见[IDE支持](./ide-cn.md)。

## resolveBuildDependencies/resolveTestDependencies

依赖`prepare`任务，分别解析`build`和`test`的依赖，生成依赖树。在这个过程中会解决相关依赖之间的冲突。

## installDependencies

内部任务，请勿使用。此任务检查`resolveBuildDependencies/resolveTestDependencies`任务是否存在，并将相应的依赖扁平化后安装到`vendor`中。其中`build`依赖的优先级高于`test`依赖。

## dependencies

依赖`resolveBuildDependencies/resolveTestDependencies`任务，显示当前项目的依赖树。这对于包冲突的解决非常有用。

## vendor

依赖`resolveBuildDependencies/resolveTestDependencies/installDependencies`任务。将解析后的`build`依赖和`test`依赖合并后安装到vendor目录。详见[依赖安装到vendor目录](./dependency-management-cn.md#依赖安装到vendor目录)。

## lock

依赖`vendor`任务，生成依赖锁定文件。详见[依赖锁定](./getting-started-cn.md#依赖锁定)。

## build

依赖`resolveBuildDependencies/installDependencies`。执行构建工作，默认情况下等价于运行：

```
go build github.com/my/project -o .gogradle/windows_amd64_project
```

你可以按照自己的实际需求进行配置，如下：

```
build {
    // 交叉编译的输出选项，注意，要求go 1.5+
    targetPlatform = 'windows-amd64, linux-amd64, linux-386'
    
    // 输出文件的路径，可以是绝对路径（相对于项目目录）或者相对路径
    // 其中的${}占位符会在交叉编译时被渲染
    outputLocation = './.gogradle/${GOOS}_${GOARCH}_${PROJECT_NAME}${GOEXE}'
}
```

例如，上面的配置指明，需要进行三次交叉编译和构建，并生成相应的输出结果。这会在项目目录下的`.gogradle`下生成三个文件：

- windows_amd64_project.exe
- linux_amd64_project
- linux_386_project

如果你的`main`包并不在项目的根目录下，或者你希望添加一些自定义的命令行参数，你需要进行如下配置：

```
build {
    doLast {
        go 'build -o ./gogradle/output github.com/my/package/my/subpackage --my-own-cmd-arguments'
    }
}
```

注意，`go`后面的引号是必须的。

## test

依赖`resolveBuildDependencies/resolveTestDependencies/installDependencies`。执行测试工作。假定你的项目`github.com/my/project`包含若干个子包`github.com/my/project/sub1`/`github.com/my/project/sub2`/.../`github.com/my/project/subN`，Gogradle会对这N个包逐个执行测试和覆盖率检查，并生成HTML格式的测试报告。测试报告位于`<project root>/.gogradle/reports/test`目录。目前，`test`任务尚不支持自定义配置。

## fmt

依赖`prepare`任务。运行[`gofmt`](https://golang.org/cmd/gofmt/)，默认情况下，它会使用`-w`修改项目中的文件。若不希望如此，或者想添加其他的参数，使用：

```groovy
fmt {
    doLast {
        gofmt "-r '(a) -> a' -l *.go"
    }
}
```

## vet

依赖`vendor`任务。运行[go vet](https://golang.org/cmd/vet/)。默认情况下，若`go vet`返回值非零，该任务会失败。由于`go vet`可能不准确，可以使用以下配置忽略错误：

```groovy
vet {
    continueWhenFail = true
}
```

## coverge 

依赖`test`任务，生成测试报告，默认位于 `<project root>/.gogradle/reports/coverage`

## check

通常该任务被CI系统调用，用于执行代码检查，例如覆盖率、代码风格等。默认依赖test任务、vet任务和fmt任务。


# 自定义任务

Gogradle支持自定义的go任务。详见[自定义任务](./getting-started-cn.md#自定义任务)。

欲了解更多有关任务的信息，请参考[官方文档](https://docs.gradle.org/current/userguide/more_about_tasks.html)。

# 任务名冲突

Gogradle默认的任务名`build`/`test`很容易和其他的插件冲突。若如此，请在构建的命令行中加入参数：`-Dgogradle.alias=true`：

`gradlew goBuild -Dgogradle.alias=true`

此参数将相关任务重命名为：

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

然后你就可以使用这些别名进行相关构建了。

你也可以在全局或者项目目录下的`gradle.properties`中指定此参数，详见[Gradle文档](https://docs.gradle.org/3.3/userguide/build_environment.html#sec:gradle_configuration_properties)：

例如，要使此参数变成全局的，修改`~/.gradle/gradle.properties`，若不存在则新建，加入：

```properties
org.gradle.jvmargs=-Dgogradle.alias=true
```
