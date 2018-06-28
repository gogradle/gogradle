# 入门指南

Gogradle是[Gradle](https://gradle.org/)的一个插件。Gradle是一个使用Groovy作为DSL的自动化构建工具，允许用户通过自定义的构建脚本来完成构建工作。Gradle的文档在[这里](https://docs.gradle.org/current/userguide/userguide.html)。

## 准备工作

在开始之前，你无需安装Go和设置`GOPATH`——当然设置好了也无妨。

- 安装[JDK 8+](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
  - 如果你安装了JetBrains系列的IDE（IntellijIDEA/Gogland/WebStorm/PhpStorm/PyCharm/RubyMine/CLion）之一，那么你可以利用其自带的JRE，而无需额外安装。详见[IDE支持](./ide-cn.md)
- 将**本项目**中的`gradle`目录、`gradlew`文件、`gradlew.bat`文件拷贝到**欲构建的Go语言项目**目录下。
- 在欲构建的Go语言项目下新建`build.gradle`构建脚本，内容如下：

```groovy
plugins {
    id 'com.github.blindpirate.gogradle' version '0.10'
}

golang {
    packagePath = 'github.com/your/package' // 欲构建项目的go import path，注意不是本地目录的路径！
}
```

如果你之前使用的是`glide/glock/godep/gom/gopm/govendor/gvt/gbvendor/trash/gpm`之一，请运行

```
./gradlew init # *nix

gradlew init # Windows
```

来进行迁移，这会将外部依赖工具的依赖声明导入`build.gradle`。此外，你也可以令Gogradle生成自己的锁定文件`gogradle.lock`。详见[依赖锁定](#依赖锁定)一节。

## 离线使用Gogradle插件

本节的目的是为访问官方插件仓库[https://plugins.gradle.org](https://plugins.gradle.org)存在问题的小伙伴提供的workaround。若你在构建时没有遇到无法下载Gogradle插件的问题，请忽略本节。

- 移步[Gogradle Release](https://github.com/blindpirate/gogradle/releases)下载最新版本的jar包。
- 将构建脚本改为

build.gradle:

```
buildscript {
    dependencies {
        classpath files('<path to downloaded jar>')
    }
}

apply plugin: 'com.github.blindpirate.gogradle'

golang {
    packagePath = 'github.com/your/package' // 欲构建项目的path，不是项目的磁盘路径！
}

```

## 解析依赖并将其安装到vendor

进入项目目录，运行

```
./gradlew vendor # *nix

gradlew vendor # Windows
```

在下文中，`gradlew`命令将以统一的`gradlew <task>`形式给出，不再区分平台。

Gogradle会按照`build.gradle`或者`gogradle.lock`（稍后提及）文件的要求，解析依赖包及其传递性依赖，并将它们扁平化后安装到到`vendor`目录。Gogradle将`vendor`看做一个项目级的、放置依赖包的临时目录，因此，在每次运行此任务时，它会保证`vendor`目录和当前构建所声明或者锁定的全部依赖的版本一致。在Gogradle中存在两种模式：`DEVELOP`和`REPRODUCIBLE`模式（可缩写为`DEV`和`REP`），`DEVELOP`模式下，此任务会解析`build.gradle`中声明的依赖；`REPRODUCIBLE`模式下，此任务会解析`gogradle.lock`文件中锁定的依赖（如果它存在的话）。默认模式为`REPRODUCIBLE`模式，若希望切换模式，只需要在运行任意`gradlew xxx`时，加上参数

`gradle xxx -Dgogradle.mode=DEV`或者`gradle xxx -Dgogradle.mode=DEVELOP`（`DEVELOP`模式）

以及

`gradle xxx -Dgogradle.mode=REP`或者`gradle xxx -Dgogradle.mode=REPRODUCIBLE`（`REPRODUCIBLE`模式）即可。

你可以自行决定是否将`vendor`目录提交到源代码管理系统中，我个人倾向于提交依赖锁定文件`gogradle.lock`，在每次构建时重新生成`vendor`目录，以减小代码仓库的体积。

## 构建Go项目

进入项目目录，运行

```
./gradlew build # *nix

gradlew build # Windows
```

`build`任务默认[依赖](https://docs.gradle.org/current/userguide/tutorial_using_tasks.html#sec:task_dependencies)`resolveBuildDependencies`任务，因此，即使你并未指明执行`resolveBuildDependencies`任务，Gradle仍然会先运行`resolveBuildDependencies`，这与`make`类似。这意味着，在`build`任务执行时，`resolveBuildDependencies`任务已经执行完毕，并将所有的依赖包安装在了`vendor`目录中。默认情况下，`build`任务等价于调用`go build <current package path> -o <output location>`，你可以对其进行进一步的配置，详见[build任务](./tasks-cn.md#build)。

## 测试Go项目

进入项目目录，运行 

```
gradlew test
```

测试指定文件：

```
gradlew test --tests main_test.go // 指定一个测试文件
gradlew test --tests *_test.go // 通配符测试
```

若希望构建在测试完成之后进行，只需在`build.gradle`中添加

```groovy
build.dependsOn test
```

HTML格式的测试报告会被放置在`<project root>/.gogradle/reports/test`目录。更多细节，请参阅[test任务](./tasks-cn.md#test)。

## 自定义任务

如果你希望自定义一些任务来执行相应的操作，我建议你先阅读Gradle的文档以了解[任务机制](https://docs.gradle.org/current/userguide/tutorial_using_tasks.html)，它和`make`的任务相似，只是更加灵活、强大。

例如，若希望添加一个任务，运行[`golint`](https://github.com/golang/lint)检查，需要在`build.gradle`中添加：

```groovy
task golint(type: com.github.blindpirate.gogradle.Go) {
    dependsOn vendor // 令此任务依赖vendor任务，这样才能保证任务执行时所有依赖包都已经被安装到了vendor中
    environment MY_OWN_ENV1: 'value1', MY_OWN_ENV1: 'value2' // 设置要运行命令的环境变量
    run 'golint github.com/my/project' // 指定任务中运行的命令
}

check.dependsOn golint
```

注意，这种语法中不支持`stdout`和`stderr`的重定向，以及管道操作。如果你希望进行重定向，例如实现一个跨平台的[`tee`](https://en.wikipedia.org/wiki/Tee_(command))，需要利用`Groovy`的[闭包](http://groovy-lang.org/closures.html)：

```
task myTee(type: com.github.blindpirate.gogradle.Go){
    dependsOn vendor // 令此任务依赖vendor任务，这样才能保证任务执行时所有依赖包都已经被安装到了vendor中
    go('build -v github.com/my/project') {
        stdout { stdoutLine ->
            println stdoutLine
            new File('stdout.txt').append(stdoutLine)
        }
        stderr { stderrLine ->
            println stderrLine
            new File('stderr.txt').append(stderrLine)
        }
    }   
}
```

更简单地，如下代码

```
task myTee(type: com.github.blindpirate.gogradle.Go){
    dependsOn vendor // 令此任务依赖vendor任务，这样才能保证任务执行时所有依赖包都已经被安装到了vendor中
    go('build -v github.com/my/project') {
        stdout writeTo('stdout.txt')
        stderr appendTo('/this/is/absolute/path/stderr.txt')
    } 
}
```

指示Gogradle执行`build -v github.com/my/project`，并将标准输出写入项目目录下的`stdout.txt`，并将标准错误追加到`/this/is/absolute/path/stderr.txt`中。

如果希望实现一个跨平台的`/dev/null`，需要：

```
task outputToDevNull(type: com.github.blindpirate.gogradle.Go){
    dependsOn vendor // 令此任务依赖vendor任务，这样才能保证任务执行时所有依赖包都已经被安装到了vendor中
    go('build -v github.com/my/project') {
        stdout devNull()
        stderr devNull()
    }    
    
    doLast {
        if(exitValue!=0){
            println "return code is ${exitValue}"
        }
    }
}
```

这段代码指示Gogradle执行`go build -v github.com/my/project`，并将标准输入和标准输出丢弃。如果`go`进程的返回值不会0，则打印之。你可能会问，这么冗长复杂的命令，我为什么不用Shell和`make`实现呢？

答案是，第一，Shell跨平台能力差，而这里的所有的代码都是可以跨平台的；第二，这里可以编写任意的`Groovy`代码，引用JVM生态系统（Java/Groovy/Scala/Kotlin, etc）中的任何类库。

## 添加依赖

欲添加一个指定版本的依赖，只需要在`build.gradle`中的`dependencies`中，添加对应的包和版本即可：

```groovy
dependencies {
    golang {
        build 'github.com/a/b@v1.0.0' 
        test 'github.com/c/d#d3fbe10ecf7294331763e5c219bb5aa3a6a86e80'
    }
}
```

其中，build和test分别是构建和测试的依赖，二者是独立的，Java的开发者应该很熟悉这样的概念。默认情况下，`build`依赖`resolveBuildDependencies`任务，因此`build`运行时，`vendor`中只包含声明为`build`的依赖包；而`test`同时依赖`resolveBuildDependencies`和`resolveTestDependencies`，因此`test`运行时，`vendor`中同时包含声明为`build`和`test`的依赖包。

有关依赖的详细解释，参考[依赖管理](./dependency-management-cn.md)一节。

## 查看依赖

```
gradlew dependencies
```

输出如下：

```
build:
github.com/aws/aws-sdk-go
|-- github.com/go-ini/ini:e7fea39
|-- github.com/jmespath/go-jmespath:bd40a43
\-- golang.org/x/tools:bf4b54d
    |-- golang.org/x/crypto:0fe9631
    \-- golang.org/x/net:5139290
        |-- golang.org/x/crypto:0fe9631 (*)
        \-- golang.org/x/text:19e5161
            \-- golang.org/x/tools:bf4b54d -> bf4b54d (*)

test:
github.com/aws/aws-sdk-go
\-- github.com/stretchr/testify:4d4bfba
    |-- github.com/davecgh/go-spew:04cdfd4
    |-- github.com/pmezard/go-difflib:d8ed262
    \-- github.com/stretchr/objx:cbeaeb1

```

这是[aws-sdk-go](https://github.com/aws/aws-sdk-go)项目在`31484500fe`时，执行`init`自动导入后生成的依赖树。其中，箭头(->)代表该依赖包与其他依赖包冲突，因此被解析成了另外一个版本；星号(*)代表本节点之前已经显示过，因此忽略其后代。

## 依赖锁定

```
gradlew lock
```

这会在项目目录下生成一个`gogradle.lock`文件，其中记录了本项目的所有的依赖包。`gogradle.lock`是Gogradle推荐的依赖锁定方式。
锁定依赖包版本是稳定构建（Reproducible build）的重要因素。与[其他包管理工具](https://github.com/golang/go/wiki/PackageManagementTools)类似，Gogradle能够锁定当前的所有依赖包版本。有所不同的是，Gogradle做的更加彻底，它甚至能够锁定`vendor`目录中的依赖包！

Gogradle支持传递性依赖、依赖排除、自定义仓库URL等多种功能，详见[依赖管理](./dependency-management-cn.md)。

目前，Gogradle仅支持Git和Mercurial依赖，对其他版本控制工具的支持正在开发中。

## 自定义Gogradle配置

下面是完整的配置，位于`build.gradle`文件的`golang`中。

```groovy
golang {
    
    // 当前构建的包路径
    packagePath = 'github.com/user/project'
    
    // 指明当前的模式。有两个可选值：DEVELOP/REPRODUCIBLE，默认为REPRODUCIBLE
    // 该配置会被命令行参数-Dgogradle.mode所覆盖
    // 可缩写为DEV/REP
    buildMode = 'REPRODUCIBLE'
    
    // 构建所需的Go版本。详见 https://golang.org/dl/
    // 若此值未设定，则先去本地查找go命令，找到则使用之；否则自动下载最新的Go版本
    // 若此值已经设定，则检查本地的go版本是否相符，相符则使用之；否则自动下载该版本的Go
    goVersion = '1.8.1'
    
    // 默认为"go"。若go不在$PATH中，可以使用此配置指定其位置
    // 可以用 goExecutable = 'IGNORE_LOCAL' 来强制指定不使用本地的go
    goExecutable = '/path/to/go/executable'
    
    // 方便自定义仓库，Gogradle会尝试从以下地址下载Go的发行版
    // http://golangtc.com/static/go/${version}/go${version}.${os}-${arch}${extension}
    goBinaryDownloadTemplate == 'http://golangtc.com/static/go/${version}/go${version}.${os}-${arch}${extension}'
    
    // 默认为<go程序所在目录>/..
    goRoot = '/path/to/my/goroot'
    
    // 即build constraint。详见 https://golang.org/pkg/go/build/#hdr-Build_Constraints
    buildTags = ['appengine','anothertag']
    
    // 由于Go语言的官方下载地址在墙外，
    // 开启此设置时，会使用墙内地址下载Go语言安装包
    // 默认为false
    fuckGfw = true
}
```
