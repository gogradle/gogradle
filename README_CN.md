# Gogradle - 完善的Go语言构建工具

[![Build Status](https://travis-ci.org/blindpirate/gogradle.svg?branch=master)](https://travis-ci.org/blindpirate/gogradle)
[![Coverage Status](https://coveralls.io/repos/github/blindpirate/gogradle/badge.svg?branch=master)](https://coveralls.io/github/blindpirate/gogradle?branch=master)
[![Java 8+](https://img.shields.io/badge/java-8+-4c7e9f.svg)](http://java.oracle.com)
[![Apache License 2](https://img.shields.io/badge/license-APL2-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt)

Gogradle是一个提供Go语言构建支持的Gradle插件。

> 2017-02-12 现在Gogradle可以在**不做任何额外设置**的情况下构建[Github's top 1000](http://github-rank.com/star?language=Go)中的526个！

## 功能特性

- 除`JDK 8+`外无需预先安装任何东西（包括Go本身）
- 支持所有版本的Go且允许多版本共存
- 完美支持几乎所有平台（只要能够运行`Java`，本项目的所有测试在OS X 10.11/Ubuntu 12.04/Windows 7上通过）
- 项目级的依赖隔离，无需设置`GOPATH`
- 完善的包管理
  - 无需手工安装依赖包，只需指定版本
  - 无需安装即可支持Go语言默认支持的四种版本控制工具：Git/Svn/Mercurial/Bazzar （当前只实现了Git）
  - 支持传递性依赖
  - 支持自定义传递性依赖策略
  - 自动解决冲突 
  - 支持依赖锁定
  - 支持glide/glock/godep/gom/gopm/govendor/gvt/gbvendor/trash等外部依赖的导入（基于[这份报告](https://github.com/blindpirate/report-of-go-package-management-tool)）
  - 支持[submodule](https://git-scm.com/book/zh/v2/Git-%E5%B7%A5%E5%85%B7-%E5%AD%90%E6%A8%A1%E5%9D%97)
  - 支持[语义化版本](http://semver.org/)
  - 支持[vendor](https://docs.google.com/document/d/1Bz5-UB7g2uPBdOx-rw5t9MxJwkfpx90cqG9AFL0JAYo)
  - 支持依赖的扁平化 （受[glide](https://github.com/Masterminds/glide)启发）
  - 支持本地包重命名
  - 支持私有仓库
  - 构建、测试依赖分别管理
  - 支持依赖树可视化
- 支持构建、测试、单个/通配符测试、交叉编译  
- 现代的、生产级别的自动化构建支持，添加自定义任务极其简单
- 原生的Gradle语法
- 额外为中国大陆开发者提供的特性，你懂的
- Shadowsocks支持
- IDE插件支持（对IDEA的支持正在内测，对VSCode和Gogland的支持正在开发中）
- 增量构建（开发中）

## 优势

- 完善的跨平台支持
- 支持所有主流外部依赖管理工具
- 完善的测试覆盖
- 长期维护
- 众多Gradle插件

## 入门指南

Gogradle是[Gradle](https://gradle.org/)的一个插件。Gradle是一个使用Groovy作为DSL的自动化构建工具，允许用户通过自定义的构建脚本来完成构建工作。
大多数情况下，你无需了解Gradle本身，只需要阅读本文档即可。

### 准备工作

- 安装[JDK 8+](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
- 将**本项目**中的`gradle`目录、`gradlew`文件、`gradlew.bat`文件拷贝到**欲构建的Go语言项目**目录下。
- 在欲构建的Go语言项目下新建`build.gradle`构建脚本，内容如下：

```groovy
plugins {
    id 'com.github.blindpirate.gogradle' version '0.2.3'
}

golang {
    packagePath = 'your/package/path' // 欲构建项目的path
}
```
如果你之前使用的是glide/glock/godep/gom/gopm/govendor/gvt/gbvendor/trash之一，那么无需任何设置，Gogradle会自动读取这些包管理工具保存在项目目录中的依赖锁定文件。此外，可以令Gogradle生成自己的锁定文件`gogradle.lock`。一旦该文件生成，原先的包管理工具的依赖锁定文件就不会再生效，你可以删除之。详见[依赖锁定](#依赖锁定)一节。

### 构建Go项目

进入项目目录，运行

```
./gradlew build # *nix

gradlew build # Windows
```

在下文中，`gradlew`命令将以统一的`gradlew <task>`形式给出，不再区分平台。

以上命令等价于在当前项目目录下运行`go build`，区别在于，Gogradle自动完成了依赖解析、安装等一系列过程。注意，Gogradle**不使用全局的`GOPATH`**，它会将所有的依赖安装在当前项目目录下并自动设置与构建相关的环境变量——这意味着构建是完全隔离的、可复现的。


### 测试Go项目

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

### 添加依赖

欲添加一个指定版本的依赖，只需要在`build.gradle`中的`dependencies`中，添加对应的包和版本即可：

```groovy
dependencies {
    build 'github.com/a/b@v1.0.0' 
    test 'github.com/c/d#d3fbe10ecf7294331763e5c219bb5aa3a6a86e80'
}
```

其中，build和test分别是构建和测试的依赖，二者是独立的。构建时的环境中只包含声明为`build`的依赖包，测试时的环境中包含声明为`build`和`test`的依赖包。

有关依赖的详细解释，参考[依赖管理](#依赖管理)一节

### 查看依赖

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

### 依赖锁定

```
gradlew lock
```

这会在项目目录下生成一个`gogradle.lock`文件，其中记录了本项目的所有的依赖包。
`gogradle.lock`是Gogradle推荐的依赖锁定方式。
锁定依赖包版本是稳定构建（Reproducible build）的重要因素。与[其他包管理工具](https://github.com/golang/go/wiki/PackageManagementTools)类似，
Gogradle能够锁定当前的所有依赖包版本。有所不同的是，Gogradle做的更加彻底，它甚至能够锁定`vendor`目录中的依赖包！

Gogradle支持传递性依赖、依赖排除、自定义仓库URL等多种功能，详见[依赖文档](#依赖管理)。

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

## 依赖管理

依赖包管理是所有包管理工具的噩梦。幸运的是，Gogradle的包管理机制非常优秀，足以面对极端复杂的情况。
众所周知，Go语言本身不参与代码包的管理；它假设所有的包都位于一个或者多个[Workspace](https://golang.org/doc/code.html#Workspaces)中，
这些Workspace由`GOPATH`指定。`GOPATH`可以包含多个路径，在构建时，Go语言构建工具会依次在这些路径中寻找所需的代码包。这就带来了许多问题：

- Go项目缺乏依赖包的版本信息，难以进行稳定、可复现的构建
- 同一台计算机（或者构建服务器）上可能同时进行多个构建，这些构建可能依赖同一个代码包的不同版本
- 由于传递性依赖的存在，同一个项目可能依赖同一代码包的多个版本

饱受包管理问题困扰的Go语言不得已在1.5之后引入了[vendor](https://docs.google.com/document/d/1Bz5-UB7g2uPBdOx-rw5t9MxJwkfpx90cqG9AFL0JAYo)机制，
允许一个Go项目与自身的依赖包一起进入源代码管理系统。这一定程度上缓解了上述情况，却引入了新的问题：

- 冗余代码的存在使得项目臃肿不堪
- 同一个工程中存在同一个代码包的多个版本，这迟早会带来[问题](https://github.com/blindpirate/golang-broken-vendor)
- 众多的[外部包管理工具](https://github.com/golang/go/wiki/PackageManagementTools)互不兼容，参差不齐

Gogradle致力于改善这种情况。它不遵守Go语言的全局Workspace约定，而是实现了完全隔离的、项目级的Workspace。
解析后的所有代码包都位于项目目录下的临时文件夹中，因此无需预先设置`GOPATH`。

### 声明依赖

Gogradle管理的依赖包声明于`dependencies`中。当前只支持Git管理的依赖包，其他源代码管理系统的支持正在开发中。
下面是一些示例：

```groovy
dependencies {
    build 'github.com/user/project'  // 未指定版本，将获取最新版本
    build name:'github.com/user/project' // 与上一行等价
    
    build 'github.com/user/project@1.0.0-RELEASE' // 指定版本（Git的tag）
    build name:'github.com/user/project', tag:'1.0.0-RELEASE' // 与上一行等价
    build name:'github.com/user/project', version:'1.0.0-RELEASE' // 与上一行等价
    
    test 'github.com/user/project#d3fbe10ecf7294331763e5c219bb5aa3a6a86e80' // 指定commit
    test name:'github.com/user/project', commit:'d3fbe10ecf7294331763e5c219bb5aa3a6a86e80' // 与上一行等价
}
```

依赖声明支持[语义化版本](http://semver.org/)。在Git中，"版本"即Git的tag。不过，考虑到构建的第一要务是保证稳定，Gogradle并不推荐使用它们。

```groovy
dependencies {
    build 'github.com/user/project@1.*'  // 等价于 >=1.0.0 & <2.0.0
    build 'github.com/user/project@1.x'  // 与上一行等价
    build 'github.com/user/project@1.X'  // 与上一行等价
    
    build 'github.com/user/project@~1.5' // 等价于 >=1.5.0 & <1.6.0
    build 'github.com/user/project@1.0-2.0' // 等价于 >=1.0.0 & <=2.0.0
    build 'github.com/user/project@^0.2.3' // 等价于 >=0.2.3 & <0.3.0
    build 'github.com/user/project@1' // 等价于 1.X 或者 >=1.0.0 & <2.0.0
    build 'github.com/user/project@!(1.x)' // 等价于 <1.0.0 & >=2.0.0
    build 'github.com/user/project@ ~1.3 | (1.4.* & !=1.4.5) | ~2' //复杂表达式
}
```

可以在声明时指定仓库的url。这尤其适用于私有仓库。有关私有仓库的权限验证请参考[仓库管理](#仓库管理)。

```groovy
dependencies {
    build name: 'github.com/user/project', url:'https://github.com/user/project.git', tag:'v1.0.0'
    build name: 'github.com/user/project', url:'git@github.com:user/project.git', tag:'v2.0.0'
}
```

可以同时声明多个依赖：

```groovy
dependencies {
    build 'github.com/a/b@1.0.0', 'github.com/c/d@2.0.0', 'github.com/e/f#commitId'
    
    build([name: 'github.com/g/h', version: '2.5'],
          [name: 'github.com/i/j', commit: 'commitId'])
}
```

Gogradle支持对传递性依赖的管理。例如，下列声明禁止了`github.com/user/project`的传递性依赖。

```groovy
dependencies {
    build('github.com/user/project') {
        transitive = false
    }
}
```

此外，还可以排除指定条件的传递性依赖，例如，下列声明从`github.com/a/b`的后代中排除了全部`github.com/c/d`依赖包和指定版本的`github.com/e/f`依赖包。

```groovy
dependencies {
    build('github.com/a/b') {
        exclude name:'github.com/c/d'
        exclude name:'github.com/c/d', tag: 'v1.0.0'
    }
}
```

若依赖包位于本地，可以使用如下方式予以声明：

```groovy
dependencies {
    build name: 'a/local/package', dir: 'path/to/local/package' // 必须为绝对路径
}
```

### build依赖与test依赖

你可能注意到了，上面的依赖声明中始终包含`build`和`test`字样。它们是Gradle构建模型中的一个概念，称为[Configuration](https://docs.gradle.org/current/userguide/dependency_management.html#sub:configurations)。
Gogradle预定义了两个名为`build`和`test`的Configuration。无需深究其细节，你可以将它们理解成两组完全独立的依赖包集合。
在构建中，只有`build`依赖会生效；在测试中，`build`和`test`依赖同时生效，且`build`中的优先级更高。

### 依赖包管理

Gogradle将依赖包分为四种：

- 受源代码管理系统管理的代码包
- 位于本地的代码包
- 上述二者中的vendor目录中的代码包
- 源代码`import`语句中声明的代码包

Go语言本身没有依赖包的概念，一个包就是一个普通的文件夹。

在Gogradle中，依赖包通常以被源代码管理系统所管理的仓库为最小单位，例如，一个被Git管理的仓库中的所有go文件属于同一个依赖包。
Gogradle按照[Go语言默认的方式](https://golang.org/cmd/go/#hdr-Relative_import_paths)解析包的路径，将原本散乱的代码包看作一个个的依赖包。

### 依赖解析

依赖解析即将依赖包解析成实际代码的过程。这个过程通常需要借助源代码管理系统，如Git。
Gogradle的目标是采用纯Java方式支持Go语言原生支持的全部四种（Git/Mercurial/Svn/Bazaar）源代码管理工具，
不过当前只实现了Git。由于是纯Java实现，因此安装Git不是必需的。

### 传递性依赖

一个项目的依赖包（传递性依赖）可以由以下途径产生：

- vendor目录中的依赖包
- 项目目录下，外部包管理工具（包括Gogradle本身）的依赖锁定文件
- 源代码中的`import`声明

默认情况下，Gogradle会读取前两者作为传递性依赖。若这样得到的结果为空，Gogradle会扫描`.go`源代码中的`import`语句，
提取其中的代码包当作传递性依赖。

### 依赖冲突

由于传递性依赖的存在，在实际的构建中，依赖关系可能错综复杂。
当一个项目依赖了同一个代码包的不同版本（无论它们位于何处），我们认为这些版本处于冲突状态，需要解决。例如，A依赖了B的版本1和C，
C依赖了B的版本2，此时，B的版本1和版本2就存在冲突。Go语言的vendor机制允许这些版本同时存在，这是Gogradle所反对的。
因为这样做迟早会带来[问题](https://github.com/blindpirate/golang-broken-vendor)。Gogradle会尝试解决所有的依赖冲突（扁平化），
并将解决后的结果放在项目目录下的临时文件夹中，作为最终的构建环境。

Gogradle解决依赖的策略是：

- 一级依赖优先级最高：声明在待构建项目中的依赖（vendor目录/build.gradle/依赖锁定文件）具有最高的优先级
- 越新的依赖包优先级越高：较新的代码包比较旧的代码包优先级高

具体来说，Gogradle会识别每个依赖包的"更新时间"，并将这些更新时间作为解决冲突的依据。

- 受源代码管理系统管理的代码包的更新时间为特定版本的提交时间，如Git的commit time
- 位于本地文件夹中的代码包的更新时间为该文件夹的最后修改时间
- 上述二者的vendor目录中的代码包的更新时间为其"宿主"代码包的更新时间

### 依赖锁定

你可以令Gogradle锁定当前构建的依赖，这会在项目目录下生成一个名为`gogradle.lock`的文件，记录了构建所需的全部依赖的详细版本，以便进行稳定的、可重现的构建。无论何时，此文件都不应被手动修改。

Gogradle推荐将此文件提交到源代码管理系统中。可以通过

```
gradlew lock
```
生成依赖锁定文件。

### 依赖安装到vendor目录

Go语言1.5之后支持vendor机制，Gogradle也提供了支持（尽管并不推荐）。欲将当前构建的依赖安装到vendor目录，可运行

```
gradlew vendor
```

这会将解析完成的`build`依赖拷贝到vendor目录中。注意，`test`依赖不会被拷贝。

## Gogradle的任务

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

### prepare

进行一些准备工作，例如`build.gradle`中配置的合法性校验、指定Go语言版本的下载与安装。

### resolveBuildDependencies/resolveTestDependencies

分别解析`build`和`test`的依赖，生成依赖树。在这个过程中会解决相关依赖之间的冲突。

### dependencies

显示当前项目的依赖树。这对于包冲突的解决非常有用。

### installBuildDependencies/installTestDependencies

将解析完成的`build`和`test`进行扁平化，然后安装到项目目录的`.gogradle`文件夹中，以备构建使用。

### build

执行构建工作。这等价于：

```
cd <project path>
export GOPATH=<build dependencies path>
go build -o <output path> 
```

### test

执行测试工作。这等价于：

```
cd <project path>
export GOPATH=<build dependencies path>:<test dependencies path>
go test
```

### check

通常该任务被CI系统调用，用于执行代码检查，例如覆盖率、代码风格等。默认依赖test任务。

### clean

清除项目中的临时文件。

### lock

生成依赖锁定文件。详见[依赖锁定](#依赖锁定)。

### vendor

将解析后的`build`依赖安装到vendor目录。详见[依赖安装到vendor目录](#依赖安装到vendor目录)。

## 构建输出与交叉编译

默认情况下，Gogradle会将构建的输出放置在`${projectRoot}/.gogradle`目录下，命名为`${os}_${arch}_${packageName}`。
你可以通过相应配置改变输出位置和命名约定，详见[配置](#配置)。

Go1.5之后引入了方便的[交叉编译](https://dave.cheney.net/2015/08/22/cross-compilation-with-go-1-5)，因此，Gogradle能够在一次构建中输出多个平台下的构建结果。

```
golang {
    ...
    targetPlatform = 'windows-amd64, linux-amd64, linux-386'
    ...
}
```

上述配置指明，需要当前的构建输出3份结果。`targetPlatform`字符串必须遵循以上格式。

## 仓库管理

Gogradle支持私有仓库。你可以在`build.gradle`文件的`repositories`中声明仓库的相关设置。

默认情况下，Gogradle在执行Git相关操作时会读取本机的ssh相关目录。如果你的ssh文件没有放在默认目录`~/.ssh`，则需要通过以下设置：

```
repositories {
    git {
        all()
        credentials {
            privateKeyFile '/path/to/private/key'
        }
    }
}
```

你可能希望对某些Git仓库应用不同的身份验证信息，那么可以这样：

```
repositories{
    git {
        url 'http://my-repo.com/my/project.git'
        credentials {
            username ''
            password ''
        }

    git {
        name 'import/path/of/anotherpackage'
        credentials {
            privateKeyFile '/path/to/private/key'
        }
    }    
}
```

其中，`name`和`url`中的参数并非只能是字符串，还可以是任何对象。Gogradle通过Groovy语言内建的[`Object.isCase()`](http://mrhaki.blogspot.jp/2009/08/groovy-goodness-switch-statement.html)方法判定一个仓库声明是否生效。
例如，你可以在其中使用正则：

```
    git {
        url ~/.*github\.com.*/
        credentials {
            privateKeyFile '/path/to/private/key'
        }
    }
```

甚至闭包：

```
    git {
        name {it->it.startsWith('github.com')}
        credentials {
            privateKeyFile '/path/to/private/key'
        }
    }
```

若一个仓库匹配某个仓库声明，那么该声明中的身份验证信息将会被用于拉取代码。Gogradle当前只支持Git仓库，身份验证信息可以使用户名/密码（http协议）或者ssh私钥（ssh协议）。

对其他版本控制系统仓库的开发正在进行中，敬请期待。

## 为构建设置代理

若需要为拉取代码设置代理，可以在`gradlew`命令中增加参数（以Shadowsocks为例）：

```./gradlew build -DsocksProxyHost=127.0.0.1 -DsocksProxyPort=1080```

其他命令类似。

同时，你可以通过在`~/.gradle/gradle.properties`或`${projectRoot}/gradle.properties`中增加

```
org.gradle.jvmargs=-DsocksProxyHost=127.0.0.1 -DsocksProxyPort=1080
```

来将此设置持久化，有关更多环境和代理的信息，详见[Gradle构建环境](https://docs.gradle.org/current/userguide/build_environment.html#sec:gradle_properties_and_system_properties)与[Java代理](http://docs.oracle.com/javase/6/docs/technotes/guides/net/proxies.html)

## IDE集成

Go语言是一门静态类型语言，因此许多IDE对其提供了支持，如[VSCode](https://github.com/Microsoft/vscode-go)、[IDEA](https://github.com/go-lang-plugin-org/go-lang-idea-plugin)和[Gogland](https://www.jetbrains.com/go/)。
通常，这些IDE需要用户在使用之前手工设置`GOPATH`并在其中准备好依赖的代码包。Gogradle计划简化这个流程，希望能够让用户无需进行任何配置即可进行开发。
对此的设计正在进行中。


## 向Gogradle贡献提出建议或贡献代码

若觉得不错，请Star。

有问题和需求请直接提[issue](https://github.com/blindpirate/gogradle/issues/new)。

欲和我一起改进Gogradle，请提交[PR](https://github.com/blindpirate/gogradle/pulls)。





