# Gogradle - 完善的Go语言构建工具

## 功能特性

- 除JDK 8+外无需预先安装任何东西（包括Go本身）
- 支持所有版本的Go且允许多版本共存
- 完美支持几乎所有平台（只要能够运行Java）
- 项目级的依赖隔离，无需设置GOPATH
- 完善的包管理
  - 无需手工安装依赖包，只需指定版本
  - 无需安装即可支持Go语言默认支持的四种版本控制工具：Git/Svn/Mercurial/Bazzar （当前只实现了Git）
  - 支持传递性依赖
  - 支持自定义传递性依赖策略
  - 自动解决冲突 
  - 支持依赖锁定
  - 支持glide/glock/godep/gom/gopm/govendor/gvt/gbvendor/trash等外部依赖的导入（基于[这份报告](https://github.com/blindpirate/report-of-go-package-management-tool)）
  - 支持[语义化版本](http://semver.org/)
  - 支持[vendor](https://docs.google.com/document/d/1Bz5-UB7g2uPBdOx-rw5t9MxJwkfpx90cqG9AFL0JAYo)
  - 支持依赖的扁平化 （受[glide](https://github.com/Masterminds/glide)启发）
  - 支持本地包重命名
  - 支持私有仓库
  - 构建、测试的依赖分开管理
  - 支持依赖树可视化
- 支持构建、测试、单个/通配符测试、交叉编译  
- 现代的、生产级别的自动化构建支持，添加自定义任务极其简单
- 原生的Gradle语法
- 额外为中国开发者提供的特性，你懂的
- IDE插件支持（开发中）

## 优势

- 全平台
- 支持几乎所有的外部依赖管理工具
- 100%测试覆盖率 （当然，这一点还在努力中）
- 长期维护
- 众多Gradle插件

## 入门指南

Gogradle是[Gradle](https://gradle.org/)的一个插件。Gradle是一个使用Groovy作为DSL的自动化构建工具，允许用户通过自定义的构建脚本来完成构建工作。
大多数情况下，你无需了解Gradle本身，只需要遵循本文档即可。

### 准备工作

- 安装[JDK 8+](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
- 将wrapper相关的文件拷贝到项目目录
- 修改`build.gradle`中的`golang`部分，改成相应的项目路径

```groovy
golang {
    packagePath = 'your/package/path'
}
```

### 构建Go项目

进入项目目录，执行

```
./gradlew build # *nix

gradlew build # Windows
```

以上命令等价于在当前项目目录下运行`go build`，区别在于，Gogradle自动完成了依赖解析、安装等一系列过程，稍后我们会详细解释。

### 测试Go项目

进入项目目录，在`*nix`上，运行 

```
./gradlew test # *nix

gradlew test # Windows
```

### 添加依赖

欲添加一个指定版本的依赖，只需要在`build.gradle`中的`dependencies`块中，添加对应的包和版本即可：

```groovy
dependencies {
    build 'github.com/a/b@v1.0.0' 
    test 'github.com/c/d#d3fbe10ecf7294331763e5c219bb5aa3a6a86e80'
}
```

有关依赖的详细解释，参考[依赖](xxx)一节

其中，build和test分别是构建和测试的依赖，二者是独立的。构建时的环境中只包含声明为`build`的依赖包，测试时的环境中包含声明为`build`和`test`的依赖包。

### 查看依赖

```
./gradlew dependencies # *nix

gradlew dependencies # Windows 
```

输出大概长这个样子：

```

```

这是著名项目[gogs](https://github.com/gogits/gogs)某个版本的依赖树。其中，对号(√)代表该依赖包即最终的依赖包；
箭头(->)代表该依赖包与其他依赖包冲突，因此被解析成了另外一个版本；星号(*)代表本节点与之前的节点重复，因此忽略其后代。

### 依赖锁定

```
./gradlew lock # *nix

gradlew lock # Windows 
```

这会在项目目录下生成一个`gogradle.lock`文件，其中记录了本项目的所有的依赖包。
锁定依赖包版本是稳定构建（Reproducible build）的重要因素。与[其他包管理工具](https://github.com/golang/go/wiki/PackageManagementTools)类似，
Gogradle能够锁定当前的所有依赖包版本。有所不同的是，Gogradle做的更加彻底，它甚至能够锁定`vendor`目录中的依赖包！更多信息详见[]()。


Gogradle支持传递性依赖、依赖排除、自定义仓库URL等多种功能，详见[依赖文档](docs/dependency_cn.md)

目前，Gogradle仅支持Git依赖，对其他版本控制工具的支持正在开发中。

## 配置

下面是完整的配置，位于`golang`块中。

```groovy
golang {
    
    // 当前构建的包路径
    packagePath = 'github.com/user/project'
    
    // 指明当前的模式。有两个可选值：DEVELOP/REPRODUCIBLE，默认为DEVELOP
    // 在DEVELOP模式下，Gogradle会优先使用build.gradle中声明的依赖（这些依赖包可能是以模糊方式声明的）
    // 然后依次使用被锁定的依赖包（gogradle.lock或者其他的包管理工具）、vendor目录中的依赖包
    // 在REPRODUCIBLE模式下，这个顺序是vendor目录中的依赖包、被锁定的依赖包、build.gradle中声明的依赖包
    mode = REPRODUCIBLE
    
    // 构建所需的Go版本。参阅 https://golang.org/dl/
    // 若不指定此值，且go在本机的PATH中存在，则使用之；否则，使用最新的Stable版本
    goVersion = '1.7.1'
    
    // 即build constraint。参阅 https://golang.org/pkg/go/build/#hdr-Build_Constraints
    buildTags = ['appengine','anothertag']
    
    // 由于Go语言的官方下载地址在墙外，
    // 开启此设置时，会使用墙内地址下载Go语言安装包
    // 默认为false
    fuckGfw = true
    
    extraBuildArgs = ['','']
    extraTestArgs = ['','']

    // 输出文件的位置，默认为./.gogradle
    // 可以为绝对路径或者相对项目目录的相对路径
    outputLocation = ''
    // 输出文件的格式
    outputPattern = '${os}_${arch}_${projectName}'
    // 交叉编译的输出选项
    target = [['windows','amd64'],['linux','amd64'],['darwin','amd64']]
    
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
- 众多的[外部包管理工具](https://github.com/golang/go/wiki/PackageManagementTools))互不兼容，参差不齐

Gogradle致力于改善这种情况。它不遵守Go语言的全局Workspace约定，而是实现了完全隔离的、项目级的Workspace。
解析后的所有代码包都位于项目目录下的临时文件夹中，因此无需预先设置`GOPATH`。

### 声明依赖

Gogradle管理的依赖包声明于`dependencies`块中。当前只支持Git管理的依赖包，其他源代码管理系统的支持正在开发中。
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

依赖声明支持语义化版本。在Git中，"版本"即Git的tag。

xxxx

可以在声明时指定仓库的url。这尤其适用于私有仓库。有关私服的权限验证请参考[]()一节
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

此外，还可以排除指定条件的传递性依赖，例如，下列声明从`github.com/a/b`的后代中排除了全部`github.com/c/d`
依赖包和指定版本的`github.com/e/f`依赖包。

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
Gogradle按照[Go语言默认的方式](https://golang.org/cmd/go/#hdr-Relative_import_paths)解析包的路径，将原本散乱的代码包看作
一个个的依赖包。

### 依赖解析

依赖解析即将依赖包解析成实际代码的过程。这个过程通常需要借助源代码管理系统，如Git。
Gogradle的目标是采用纯Java方式支持Go语言原生支持的全部四种（Git/Mercurial/Svn/Bazaar）源代码管理工具，
不过当前只实现了Git。由于是纯Java实现，因此不要求宿主机上安装Git。

### 传递性依赖

一个项目的依赖包（传递性依赖）可以由以下途径产生：

- vendor目录中的依赖包
- 项目目录下，外部包管理工具（包括Gogradle本身）的依赖锁定文件
- 源代码中的`import`声明

默认情况下，Gogradle会读取前两者作为传递性依赖。若这样得到的结果为空，Gogradle会扫描`.go`源代码中的`import`语句，
提取这些代码包当作传递性依赖。

### 依赖冲突

由于传递性依赖的存在，在实际的构建中，依赖关系可能错综复杂。
当一个项目依赖了同一个代码包的不同版本（无论它们位于何处），我们认为这些版本处于冲突状态，需要解决。例如，A依赖了B的版本1和C，
C依赖了B的版本2，此时，B的版本1和版本2就存在冲突。Go语言的vendor机制允许这些版本同时存在，这是Gogradle所反对的。
这样做迟早会带来[问题](https://github.com/blindpirate/golang-broken-vendor)。Gogradle会尝试解决所有的依赖冲突（扁平化），
并将解决后的结果放在项目目录下的临时文件夹中，作为最终的构建环境。

Gogradle解决依赖的策略是：

- 一级依赖优先级最高：声明在待构建项目中的依赖（vendor目录/build.gradle/依赖锁定文件）具有最高的优先级
- 越新的依赖包优先级越高：较新的代码包比较旧的代码包优先级高

具体来说，Gogradle会识别每个依赖包的更新时间，并将这些更新时间作为解决冲突的依据。

- 受源代码管理系统管理的代码包的更新时间



在实际的构建中，依赖关系可能错综复杂，同一个依赖包可能会存在多个版本。对此，Gogradle的解决方案是：扁平化，并保证同一个依赖包在同一




## 仓库管理
## 向Gogradle贡献代码

