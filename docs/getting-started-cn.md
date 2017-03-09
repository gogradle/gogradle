# 入门指南

Gogradle是[Gradle](https://gradle.org/)的一个插件。Gradle是一个使用Groovy作为DSL的自动化构建工具，允许用户通过自定义的构建脚本来完成构建工作。
大多数情况下，你无需了解Gradle本身，只需要阅读本文档即可。

## 准备工作

- 安装[JDK 8+](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
  - 如果你安装了JetBrains系列的IDE（IntellijIDEA/Gogland/WebStorm/PhpStorm/PyCharm/RubyMine/CLion）之一，那么你可以利用其自带的JDK，而无需额外安装。详见[IDE支持](./ide-cn.md)
- 将**本项目**中的`gradle`目录、`gradlew`文件、`gradlew.bat`文件拷贝到**欲构建的Go语言项目**目录下。
- 在欲构建的Go语言项目下新建`build.gradle`构建脚本，内容如下：

```groovy
plugins {
    id 'com.github.blindpirate.gogradle' version '0.2.8'
}

golang {
    packagePath = 'github.com/your/package' // 欲构建项目的path
}
```
如果你之前使用的是glide/glock/godep/gom/gopm/govendor/gvt/gbvendor/trash之一，那么无需任何设置，Gogradle会自动读取这些包管理工具保存在项目目录中的依赖锁定文件。此外，可以令Gogradle生成自己的锁定文件`gogradle.lock`。一旦该文件生成，原先的包管理工具的依赖锁定文件就不会再生效，你可以删除之。详见[依赖锁定](#依赖锁定)一节。

## 离线使用Gogradle插件
本节的目的是为访问官方插件仓库[https://plugins.gradle.org](https://plugins.gradle.org)存在问题的小伙伴提供的workaround。若你在构建时没有遇到无法下载Gogradle插件的问题，请忽略本节。

- 移步[Gogradle Release](https://github.com/blindpirate/gogradle/releases)下载最新版本的jar包。
- 将构建脚本改为

build.gradle:

```
buildscript {
    dependencies {
        classpath files('<path to the jar>')
    }
}

apply plugin: 'com.github.blindpirate.gogradle'

golang {
    packagePath = 'your/package/path' // 欲构建项目的path
}

```

## 构建Go项目

进入项目目录，运行

```
./gradlew build # *nix

gradlew build # Windows
```

在下文中，`gradlew`命令将以统一的`gradlew <task>`形式给出，不再区分平台。

以上命令等价于在当前项目目录下运行`go build`，区别在于，Gogradle自动完成了依赖解析、安装等一系列过程。注意，Gogradle**不使用全局的`GOPATH`**，它会将所有的依赖安装在当前项目目录下并自动设置与构建相关的环境变量——这意味着构建是完全隔离的、可复现的。


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

## 添加依赖

欲添加一个指定版本的依赖，只需要在`build.gradle`中的`dependencies`中，添加对应的包和版本即可：

```groovy
dependencies {
    build 'github.com/a/b@v1.0.0' 
    test 'github.com/c/d#d3fbe10ecf7294331763e5c219bb5aa3a6a86e80'
}
```

其中，build和test分别是构建和测试的依赖，二者是独立的。构建时的环境中只包含声明为`build`的依赖包，测试时的环境中包含声明为`build`和`test`的依赖包。

有关依赖的详细解释，参考[依赖管理](./dependency-management-cn.md)一节

## 查看依赖

```
gradlew dependencies
```

输出如下：

```
build:

github.com/gogits/gogs
├── github.com/Unknwon/cae:c6aac99 √
├── github.com/Unknwon/com:28b053d √
├── github.com/Unknwon/i18n:39d6f27 √
│   ├── github.com/Unknwon/com:28b053d √ (*)
│   └── gopkg.in/ini.v1:766e555 -> 6f66b0e
├── github.com/Unknwon/paginater:701c23f √
├── github.com/bradfitz/gomemcache:2fafb84 √
├── github.com/go-macaron/binding:4892016 √
│   ├── github.com/Unknwon/com:28b053d √ (*)
│   └── gopkg.in/macaron.v1:ddb19a9 √
│       ├── github.com/Unknwon/com:28b053d √ (*)
│       ├── github.com/go-macaron/inject:d8a0b86 -> c5ab7bf
│       └── gopkg.in/ini.v1:766e555 -> 6f66b0e (*)
... 

```

这是[gogs](https://github.com/gogits/gogs)项目v0.9.113的依赖树。在其项目目录下存在`glide.lock`文件，这是[glide](https://github.com/Masterminds/glide)工具生成的，因此Gogradle自动导入了它。无需任何设置，就是这么简单。

其中，对号(√)代表该依赖包即最终的依赖包；
箭头(->)代表该依赖包与其他依赖包冲突，因此被解析成了另外一个版本；星号(*)代表本节点之前已经显示过，因此忽略其后代。

## 依赖锁定

```
gradlew lock
```

这会在项目目录下生成一个`gogradle.lock`文件，其中记录了本项目的所有的依赖包。
`gogradle.lock`是Gogradle推荐的依赖锁定方式。
锁定依赖包版本是稳定构建（Reproducible build）的重要因素。与[其他包管理工具](https://github.com/golang/go/wiki/PackageManagementTools)类似，
Gogradle能够锁定当前的所有依赖包版本。有所不同的是，Gogradle做的更加彻底，它甚至能够锁定`vendor`目录中的依赖包！

Gogradle支持传递性依赖、依赖排除、自定义仓库URL等多种功能，详见[依赖管理](./dependency-management-cn.md)。

目前，Gogradle仅支持Git依赖，对其他版本控制工具的支持正在开发中。

## 配置

下面是完整的配置，位于`build.gradle`文件的`golang`中。

```groovy
golang {
    
    // 当前构建的包路径
    packagePath = 'github.com/user/project'
    
    // 指明当前的模式。有两个可选值：DEVELOP/REPRODUCIBLE，默认为DEVELOP
    // 在DEVELOP模式下，Gogradle会优先使用build.gradle中声明的依赖（这些依赖包可能是以模糊方式声明的）
    // 然后依次使用被锁定的依赖包（gogradle.lock或者其他的包管理工具）、vendor目录中的依赖包
    // 在REPRODUCIBLE模式下，这个顺序是vendor目录中的依赖包、被锁定的依赖包、build.gradle中声明的依赖包
    mode = 'REPRODUCIBLE'
    
    // 构建所需的Go版本。详见 https://golang.org/dl/
    // 若不指定此值，且goExecutable存在，则使用之；否则，使用最新的Stable版本
    goVersion = '1.7.1'
    
    // 默认为"go"。若go不在$PATH中，可以使用此配置指定其位置
    goExecutable = '/path/to/go/executable'
    
    // 即build constraint。详见 https://golang.org/pkg/go/build/#hdr-Build_Constraints
    buildTags = ['appengine','anothertag']
    
    // 由于Go语言的官方下载地址在墙外，
    // 开启此设置时，会使用墙内地址下载Go语言安装包
    // 默认为false
    fuckGfw = true
    
    // 全局缓存的时间，默认为24小时
    globalCacheFor 24,'hours'
    
    // 在构建和测试时额外传递给go命令行的参数，默认均为空列表
    extraBuildArgs = ['arg1','arg2']
    extraTestArgs = []

    // 输出文件的位置，默认为./.gogradle
    // 可以为绝对路径或者相对项目目录的相对路径
    outputLocation = ''
    // 输出文件的格式，这里必须使用单引号
    outputPattern = '${os}_${arch}_${packageName}'
    // 交叉编译的输出选项，注意，要求go 1.5+
    targetPlatform = 'windows-amd64, linux-amd64, linux-386'
}
```
