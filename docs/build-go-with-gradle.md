# 使用Gradle构建Go语言项目

厌倦了全局`GOPATH`，觉得`Makefile`太难写，想要Java那样完整的`IDE`支持？来试试Gogradle吧。

本文的目标读者是Go语言开发者，部分链接需要翻墙访问。

## Gogradle是什么

Gogradle是Gradle的一个插件。[Gradle](https://gradle.org)是现代构建(build)工具，类似于GNU Make。它允许用户以DSL（Domain Specific Language）编写自己的构建逻辑。
Java和Android的开发者应该很熟悉这货，因为Gradle在Java世界里跑的比谁都快（同时也是Android官方钦点的）。下图是我进行[构建工具调查](https://github.com/blindpirate/report-of-build-tools-for-java-and-golang)
后得到的结果：在2017年1月，[Github Top 1000 Java Projects](http://github-rank.com/star?language=Java)中，有62.7%使用了Gradle，
而仅有26.4%使用了Maven。之前[Gradle项目](http://github.com/gradle/gradle)的简介是"Powerful Build Tool for JVM"，现在已经改成了"Adaptable, fast automation for all"，
充分显示出了Gradle在非JVM领域的野心。Gradle背后是一个公司，因此开发非常活跃，现在的速度是1~2个月一个小版本，一年一个大版本。

![1](https://raw.githubusercontent.com/blindpirate/report-of-build-tools-for-java-and-golang/master/trending.png)

Gradle拥有良好的插件机制，这是`make`所缺乏的（似乎[Make 4.0](https://debian-administration.org/article/706/GNU_Make_4.0_released_including_support_for_plugins)已经支持了插件，但是悲剧的是只能拿C写）。在`make`的世界里，如果你希望复用一些构建逻辑，通常的实现方案是`Shell`脚本——当然跨平台性能就比较差（Windows用户表示要砍人）。我发邮件问了Gradle core team leader Eric Wendelin，当前Gradle社区大约有3000个插件可供使用，还有不计其数的非公开插件在全世界的公司内部使用。

![1](http://gogradle.oss-cn-hongkong.aliyuncs.com/make.png)

Gogradle就是一个支持构建Go语言的插件。简单而言，你可以将Gogradle理解为`glide`+`make`。它实现了`glide`的几乎全部功能，并且额外提供了许多功能特性。

## 为什么使用Gogradle

- Gradle基于Groovy和JVM，平台兼容性好，容易上手，同时JVM生态系统（Java/Groovy/Scala/Kotlin）有大量轮子可用
- [Gradle生态系统](https://plugins.gradle.org/)有很多插件可用
- Gradle拥有众多Feature：
  - 允许自定义任务依赖，自动生成DAG并执行
  - 允许自定义任务input/output，由Gradle进行UP-TO-DATE检查，跳过不需要重复执行的任务，提高性能。详见[文档](https://docs.gradle.org/current/userguide/more_about_tasks.html#sec:up_to_date_checks)
  - Gradle wrapper机制，自动下载指定的Gradle版本，方便进行可复现的构建
  - ...
- Gogradle支持项目级的`GOPATH`，如果你喜欢的话
- Gogradle无需预先安装Go，能够自动下载安装Go，且支持多Go版本共存和切换
- Go社区的各种依赖管理工具众多，且互不兼容
  - Gogradle提供了导入命令，从而使你能够方便地从其他工具迁移到Gogradle
  - Gogradle兼容`glide/glock/godep/gom/gopm/govendor/gvt/gbvendor/trash/gpm`工具。在查找一个依赖包的传递性依赖时，它能够自动识别这些工具的锁定文件
- Gogradle提供了许多的额外Feature
  - 测试和覆盖率HTML报告生成   
  - IDE支持
  - 使用动态语言特性完成仓库的声明和替换，可轻易地实现镜像仓库

Gogradle的项目地址在这里：[https://github.com/gogradle/gogradle](https://github.com/gogradle/gogradle)。它的目标不是取代其他的工具，只是为开发者提供一些额外的选项。如果你曾被上述问题困扰，或者你曾是Java开发者，熟悉Gradle，那么Gogradle是你不二的选择。

下图是[`gogs`](https://github.com/gogits/gogs)项目在我的Mac上的测试结果：

![1](https://raw.githubusercontent.com/blindpirate/gogradle/master/docs/images/classes.png)

以及覆盖率报告

![1](https://raw.githubusercontent.com/blindpirate/gogradle/master/docs/images/coverage.png)
![1](https://raw.githubusercontent.com/blindpirate/gogradle/master/docs/images/coveragepackage.png)

Gogradle会自动解析该包及所有的传递性依赖，解决所有可能的冲突，然后将其安装到`vendor`目录中。其他的IDE没有原生的Gradle支持，因此需要一些命令行操作。Gogradle支持IDE，详见[IDE支持](https://github.com/gogradle/gogradle/blob/master/docs/ide-cn.md)。

## 从头开始

假设你现在手头有一台刚安装完操作系统和`Git`的电脑，我们从头开始描述如何使用Gogradle搭建Go开发环境并完成开发。

### 安装JRE及IDE

Gogradle所需的一切仅仅是一个JVM。现在你需要安装JDK或者JRE 8+，在[这里](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)下载。不过，如果你决定使用JetBrains系列的IDE（IntellijIDEA/GoLand/WebStorm/PhpStorm/PyCharm/RubyMine/CLion）之一，那么你可以利用其自带的JRE，而无需额外安装。详见[Gogradle IDE支持](https://github.com/gogradle/gogradle/blob/master/docs/ide-cn.md)设置使用其自带的JRE。同样，如果你决定使用VSCode或者Vim，也按照该文档描述，安装相应的插件。

### 拷贝Gradle脚本

拷贝[Gogradle](https://github.com/gogradle/gogradle)项目下的`gradle`目录/`gradlew`/`gradlew.bat`到你的项目目录。这是Gradle提供的一种名为wrapper的机制，在运行wrapper脚本时，它会自动下载与当前构建一致的Gradle版本，因此我们实际上无需安装Gradle。考虑到你懂的因素，我把能搬的包都搬到墙内了。

### 初始化

在你的Go项目文件夹下新建一个`build.gradle`文件，内容如下：

```
plugins {
    id 'com.github.blindpirate.gogradle' version '0.10' // 请使用当前的最新版本
}

golang {
    packagePath = 'github.com/your/package' // 欲构建项目的go import path，注意不是本地目录的路径！
}
```

然后在项目文件夹下执行

```
./gradlew init # *nix

gradlew init # Windows
```

在下文中，`gradlew`命令将以统一的`gradlew <task>`形式给出，不再区分平台。这会自动扫描你的项目并识别其依赖包。特别地，如果你之前使用过`glide/glock/godep/gom/gopm/govendor/gvt/gbvendor/trash/gpm`之一的依赖锁定工具，Gogradle能够自动识别它们生成的锁定文件。

`build.gradle`文件以一种基于[`Groovy`](http://groovy-lang.org/)的DSL写成，它指定了构建所需的步骤。Groovy语言可以看做是Java的超集，扩展了Java的一些语法，暂时可以不用深究其细节。

在墙内访问Gradle的插件仓库可能会碰到你懂的问题，若遇到网络问题，请参阅[离线使用Gogradle插件](https://github.com/gogradle/gogradle/blob/master/docs/getting-started-cn.md#离线使用gogradle插件)。

### 构建

在项目目录下运行`gradlew build`。

它会自动解析所有的依赖、传递性依赖，解决依赖包冲突，然后将依赖包安装到项目目录下并调用命令行执行`go build`。

你可能会疑惑，蛤，我还没安装Go呢！没关系，Gogradle如果发现你的机器上没有安装go，会自动下载安装go的最新版本。如果你在墙内，可能遇到Go的二进制包下载不下来的问题，可移步[这里](https://github.com/gogradle/gogradle/blob/master/docs/getting-started-cn.md#自定义gogradle配置)配置`fuckGfw`参数使用墙内的镜像。

同样，你也无需预先设置GOPATH。如果Gogradle发现你没有设置`GOPATH`，会自动在项目目录下的`.gogradle`隐藏目录中新建一个项目级的`GOPATH`并使用它作为构建时的环境变量。因为所有的依赖包都会被安装在`vendor`内，所以不会发生找不到依赖包的情况。

当然，如果你机器上已经安装了Go并设置了`GOPATH`，Gogradle就会直接使用它们。

更多细节请阅读[build任务](https://github.com/gogradle/gogradle/blob/master/docs/tasks-cn.md#build)。

![1](http://gogradle.oss-cn-hongkong.aliyuncs.com/build.png)

这是`build`任务的截图，可以看到其中执行的任务。

### 测试

在项目目录下运行`gradlew test`。它会逐个包执行测试并生成之前我们看到的HTML格式的测试/覆盖率报告，是不是比原生的`go test`的简陋输出看上去好一点？

![1](https://raw.githubusercontent.com/blindpirate/gogradle/master/docs/images/failedtest.png)

![1](http://gogradle.oss-cn-hongkong.aliyuncs.com/test.png)

这次构建包含若干失败的测试，因此构建失败了。输出结果显示了测试报告的位置。

### Check

Gogradle将常用的代码检查任务封装在了`check`任务中。默认情况下，它依赖`vet`任务、`fmt`任务和`cover`任务，开箱即用，如图所示：

![1](http://gogradle.oss-cn-hongkong.aliyuncs.com/check.png)

在这次构建中，`build`依赖了`check`任务，因此相关任务得到了执行。

更多细节请阅读[Gogradle的任务](https://github.com/gogradle/gogradle/blob/master/docs/tasks-cn.md)。

### 依赖包管理

我们可以在`build.gradle`中声明所需的依赖，Gogradle会自动检索、下载所有的依赖包以及传递性依赖。下列代码给出了一些声明依赖的方式（位于`build.gradle`中）：

```
dependencies {
    golang {
        build 'github.com/user/project'  // 不指定版本，默认使用最新版本
        build name:'github.com/user/project' // 和上一行等价
    
        build 'github.com/user/project@1.0.0-RELEASE' // 指定tag
        build name:'github.com/user/project', tag:'1.0.0-RELEASE' // 和上一行等价

        build name: 'github.com/user/project', url:'https://github.com/user/project.git', tag:'v1.0.0' // 指定url，例如镜像仓库
    
        test 'github.com/user/project#d3fbe10ecf7294331763e5c219bb5aa3a6a86e80' // 指定commit
        test name:'github.com/user/project', commit:'d3fbe10ecf7294331763e5c219bb5aa3a6a86e80' // 和上一行等价

        // 语义化版本：
        build 'github.com/user/project@1.*'  // Equivalent to >=1.0.0 & <2.0.0
        build 'github.com/user/project@1.x'  // Equivalent to last line
        build 'github.com/user/project@1.X'  // Equivalent to last line
        build 'github.com/user/project@~1.5' // Equivalent to >=1.5.0 & <1.6.0
        build 'github.com/user/project@1.0-2.0' // Equivalent to >=1.0.0 & <=2.0.0
        build 'github.com/user/project@^0.2.3' // Equivalent to >=0.2.3 & <0.3.0
        build 'github.com/user/project@1' // Equivalent to 1.X or >=1.0.0 & <2.0.0
        build 'github.com/user/project@!(1.x)' // Equivalent to <1.0.0 & >=2.0.0
        build 'github.com/user/project@ ~1.3 | (1.4.* & !=1.4.5) | ~2' // Very complicated expression

        build 'github.com/a/b@1.0.0', 'github.com/c/d@2.0.0', 'github.com/e/f#commitId' // 同时声明多个依赖

        // 声明一个依赖，禁止其所有传递性依赖
        build('github.com/user/project') {
            transitive = false
        }

        // 声明一个依赖，排除部分传递性依赖
        build('github.com/a/b') {
            exclude name:'github.com/c/d'
            exclude name:'github.com/c/d', tag: 'v1.0.0'
        }

        build name: 'github.com/big/package', subpackages: ['.', 'sub1', 'sub2/subsub'] // 只依赖这个包的部分子包
    }
}
```

可以看到每个依赖前都有`build`或者`test`字样，Java开发者应该很熟悉这个概念。Gogradle提供了依赖包隔离的机制，在`build`任务中，只有`build`依赖生效；在`test`任务中，只有`test`依赖包生效。这样做的好处在于，假如我们有一个公用库A，它依赖了一些只在测试中使用的测试库，那么我们就可以将这些测试库声明为`test`依赖，这样，其他库依赖库A时，就能够清楚的知道，“哦，这些测试库是库A测试用的，所以我们没必要把它们拖到我们自己的`vendor`中来”，从而减少冗余的依赖包数量。

有关依赖管理的更多细节，请参阅[依赖管理](https://github.com/gogradle/gogradle/blob/master/docs/dependency-management-cn.md)。

### 依赖树查看

在管理依赖的过程中，我们不可避免地会遇到依赖包冲突、需要手工处理的情况。这个时候，可以使用：

```
gradlew dependencies
```

它会打印当前的依赖树：

```
build:

github.com/gogits/gogs
|-- github.com/Unknwon/cae:c6aac99
|-- github.com/Unknwon/com:28b053d
|-- github.com/Unknwon/i18n:39d6f27
|   |-- github.com/Unknwon/com:28b053d (*)
|   \- gopkg.in/ini.v1:766e555 -> 6f66b0e
|-- github.com/Unknwon/paginater:701c23f
|-- github.com/bradfitz/gomemcache:2fafb84
|-- github.com/go-macaron/binding:4892016
|   |-- github.com/Unknwon/com:28b053d (*)
|   \- gopkg.in/macaron.v1:ddb19a9
|       |-- github.com/Unknwon/com:28b053d (*)
|       |-- github.com/go-macaron/inject:d8a0b86 -> c5ab7bf
|       \- gopkg.in/ini.v1:766e555 -> 6f66b0e (*)
... 

```

例如，这是`gogs`项目的依赖树的一部分。其中，箭头表示某些依赖存在冲突，因此被Gogradle自动予以解决，解决的依据是：

- 一级依赖优先：定义在根项目中的依赖优先级高于传递性依赖
- 越新的依赖包优先级越高：例如，commit时间晚的依赖包会覆盖commit时间早的依赖包

最终，Gogradle会保证同名的依赖包在`vendor`中仅存在一份。这个过程和Java的依赖包解析过程非常相似。

### 自定义仓库与镜像仓库

为什么我们需要自定义仓库和镜像仓库呢？考虑以下场景：

- 你fork了`github.com/gebi/laowang`到自己的仓库`github.com/my/laowang`，并做了修改。你对你自己的修改是如此的满意，以至于你希望在任何时候，都使用自己的版本，这意味着你所有的项目中依赖的包、你依赖的包中的`vendor`目录中任何地方，只要引用了`github.com/gebi/laowang`，一律替换成你修改后的版本
- 一个企业希望为Github设置墙内镜像站，使得企业内部在任何时候，都使用自建的github镜像`my-repo.com`，以达到提速和内控的目的

在Go的机制中，这两个需求是难以方便的满足的。在Go中，包名通常代表了URL，指定了包的来源（详细规则在[这里](https://golang.org/cmd/go/#hdr-Remote_import_paths)）。这是一柄双刃剑，优点在于省去了类似Maven的中央仓库，缺点在于不够灵活，难以设置镜像或者自定义的包名。

Gogradle提供了非常灵活的机制来解决这些问题。

要解决第一个场景，全局替换的问题，我们需要在`build.gradle`中加入：

```
repositories {
  golang {
        root 'github.com/gebi/laowang'
        url 'https://github.com/my/laowang.git'
        vcs 'git' // 默认值是git,因此可省略
    }
}
```

这告诉Gogradle，任何时候，只要遇到`github.com/gebi/laowang`包，都转向'https://github.com/my/laowang.git'。

第二个场景中，我们需要在`build.gradle`中加入：

```
repositories {
    golang {
        root ~/github\.com\/[\w-]+\/[\w-]+/  // 任何匹配这个正则表达式的路径，都传递给url闭包处理，得到替换后的url。
        url { path->
            def split = path.split('/')
           "https://my-repo.com/${split[1]}/${split[2]}" 
        }
    }
}   
```

其中`root`接收任何参数，包括字符串、正则表达式，以及闭包。在这个例子中，所有的包路径都会和`~/github\.com\/[\w-]+\/[\w-]+/`比较，如果匹配，那么该路径就会被传入`url`所指定的闭包，生成最终的地址返回。

这是两个简单的例子。事实上，因为Gradle的构建脚本中可以编写任意代码，引用任何JVM生态系统（Java/Groovy/Scala/Kotlin）的类库，所以它可以轻易地实现复杂的逻辑。

你可能会说，这有什么简单的，还不是要在每个项目里都加上这么多配置？有一种方法可以简化这个过程：编写一个Gradle插件，将所有的逻辑移至该插件，这样，任何需要应用这些逻辑的地方都只需要`apply plugin:'my.custom.repositories.management'`即可，这非常适合企业进行内部控制。

有关仓库管理的更多内容，请查阅[仓库管理](https://github.com/gogradle/gogradle/blob/master/docs/repository-management-cn.md)。

### IDE支持

有关Gogradle对IDE支持的细节，请参考[Gogradle IDE支持](https://github.com/gogradle/gogradle/blob/master/docs/ide-cn.md)，该文档提供了详细的IDE设置步骤。


## 最后

需要强调的是，Gogradle不是一个玩具。在一个试验性项目中，我用它完成了`docker`的构建，代码在[这里](https://github.com/gogradle/moby)。Gogradle仍处于活跃的开发中，每周都会有新的Feature加入，也欢迎任何形式的Fork和Issue。在使用中遇到任何问题，欢迎加QQ群451434043讨论。

这篇文章的目的只是为Gogradle提供一个简单的介绍，有关详细文档，请戳[这里](https://github.com/gogradle/gogradle/blob/master/README_CN.md)。

![1](https://raw.githubusercontent.com/blindpirate/gogradle/master/docs/images/group.png)

最后，[这里](https://www.youtube.com/watch?v=Mvf3gY1MopE&t=350s)是在Gradle Summit 2017上有关Gogradle的演讲。
