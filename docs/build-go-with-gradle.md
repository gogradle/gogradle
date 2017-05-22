# 使用Gradle构建Go语言项目

厌倦了全局`GOPATH`，觉得`Makefile`太难写，想要Java那样完整的`IDE`支持？来试试Gogradle吧。

本文的目标读者是Go语言开发者，部分链接需要翻墙访问。

## Gogradle是什么

[Gradle](https://gradle.org)是现代构建(build)工具，类似于GNU Make。它允许用户于DSL（Domain Specific Language）编写自己的构建逻辑。
Java和Android的开发者应该很熟悉这货，因为Gradle在Java世界里跑的比谁都快（同时也是Android官方钦点的）。下图是我进行[构建工具调查](https://github.com/blindpirate/report-of-build-tools-for-java-and-golang)
后得到的结果：在2017年1月，[Github Top 1000 Java Projects](http://github-rank.com/star?language=Java)中，有62.7%使用了Gradle，
而仅有26.4%使用了Maven。之前[Gradle项目](http://github.com/gradle/gradle)的简介是"Powerful Build Tool for JVM"，现在已经改成了"Adaptable, fast automation for all"，
充分显示出了Gradle在非JVM领域的野心。Gradle的开发非常活跃，现在的速度是1~2个月一个小版本，一年一个大版本。

![1](https://raw.githubusercontent.com/blindpirate/report-of-build-tools-for-java-and-golang/master/trending.png)

Gradle拥有良好的插件机制，这是`make`所缺乏的（似乎[Make 4.0](https://debian-administration.org/article/706/GNU_Make_4.0_released_including_support_for_plugins)已经支持了插件，但是悲剧的是只能拿C写）。在`make`的世界里，如果你希望复用一些构建逻辑抽取出来，通常的实现方案是`Shell`脚本——当然跨平台性能就比较差（Windows用户表示要砍人）。我发邮件问了Gradle core team leader Eric Wendelin，当前Gradle社区大约有3000个插件可供使用，还有不计其数的非公开插件在全世界的公司内部使用。

Gogradle就是一个支持构建Go语言的插件。简单而言，你可以将Gogradle理解为`glide`+`make`。它实现了`glide`的几乎全部功能，并且额外提供了许多功能特性。

## 为什么使用Gogradle



- 基于Groovy和JVM，容易上手，同时JVM平台有大量轮子可用——我实在不想写Shell T^T
- 完全的跨平台能力，因为是基于JVM的，所以Windows毫无压力
- Gradle拥有众多Feature：
  - 允许自定义任务依赖，自动生成DAG并执行
  - 允许自定义任务input/output，由Gradle进行UP-TO-DATE检查，跳过不需要重复执行的任务，提高性能。详见[文档](https://docs.gradle.org/current/userguide/more_about_tasks.html#sec:up_to_date_checks)
  - Gradle wrapper机制，自动下载指定的Gradle版本，方便进行可复现的构建
  - ...
- 优秀的插件机制，方便进行通用逻辑的分发。

## 为什么要在Go项目中使用Gradle

Go的包管理机制和Java很像，不是小像，是大像。因此，我相信年轻的Go可以从历史悠久的Java中借鉴一些东西。

在Go中，包名是正斜杠分割的字符串，代表了`$GOPATH/src`下的路径。例如`github.com/gebi/laowang`包的代码位于`$GOPATH/src/github.com/gebi/laowang`。

在Java中，包名是点分割的字符串，代表了`$CLASSPATH`下的jar包中的路径。例如`com.github.gebilaowang`包的字节码位于`$CLASSPATH/<some jar>/com/github/gebilaowang`。

在Go中，`GOPATH`可以包含若干个路径，用分隔符（POSIX系统下是`:`，Windows系统下是`;`）分开。

在Java中，`CLASSPATH`可以包含若干个路径，用分隔符（POSIX系统下是`:`，Windows系统下是`;`）分开。

一个微小的差异在于，在Go中，一个包通常对应了一个URL（详细规则在[这里](https://golang.org/cmd/go/#hdr-Remote_import_paths)）。例如，你有一个名为`github.com/gebi/laowang`的包，那么Go就可以去`https://github.com/gebi/laowang.git`处获取此包的源代码。而在Java中，包路径通常不代表任何URL，官方仅仅是建议包的作者以自己控制的域名为自己的包命名，以避免冲突。至于包的代码如何分发和获取，官方完全没有涉及，这部分是由外部工具管理的，例如[`Ant`](http://ant.apache.org/)/[`Maven`](https://maven.apache.org/)/[`Gradle`](https://gradle.org)。

在Go中，所有依赖包都是放在全局的`GOPATH`中的，这一点让相当多的Java开发者感到不习惯。其实，在若干年前，Java也是[这么做的](https://docs.oracle.com/javase/tutorial/essential/environment/paths.html)。这种做法现在已经很少有人使用了，为什么？

一个很明显的缺点是，没有版本管理机制。例如，我的机器上有两个项目，分别依赖了同一个包的不同版本，但是`GOPATH`是全局的，怎么办？假如我的机器上有两百个项目，依赖了两千个包的两千个版本呢？

于是Java的全局`CLASSPATH`机制被迅速地抛弃，2000年，`Ant`出现了。`Ant`的一种典型做法是，把一个项目所需的依赖包放在项目文件夹下的`lib`目录中，然后将这个目录的路径用`-classpath`参数传给`java`命令。`lib`目录会随项目代码一起被提交到VCS（Version Control System）中。

与之相似，2015年，饱受版本管理困扰的Go 1.5提出了[vendor机制](https://docs.google.com/document/d/1Bz5-UB7g2uPBdOx-rw5t9MxJwkfpx90cqG9AFL0JAYo/edit)，核心思想是将一个项目的依赖包放在项目文件夹下的`vendor`目录，然后把此目录提交到VCS中。迄今为止，这是Go社区的标准做法——不过即使在Go社区内部，也有相当多的人不赞同`vendor`机制。

让我们仔细思考一下，这件事情是对的么？`vendor`机制的优点在于能够保证稳定的构建（废话，所有的代码都在这里了），缺点在于：

- 引起代码库臃肿。你的vendor包里的代码可能是你的项目代码的好几倍甚至几十上百倍。当然，`Git`足够强大，这无非是多占点空间而已。当然，`git submodule`没有这个问题，只是略显复杂。
- `vendor`中的代码缺乏追踪。当你拷贝代码到自己的vendor中之后，一旦进行修改，它就很难和上游进行合并。
- 嵌套`vendor`存在潜在风险。这种机制非常容易被破坏，引发[问题](https://github.com/blindpirate/golang-broken-vendor)。

可以看到，Go的包管理之路几乎和Java如出一辙。随着Java语言的发展和Java项目的增长，2004年，`Maven`出现了。`Maven`引入了依赖包坐标的概念，可以通过一个坐标进行检索到一个特定版本的依赖包，以及该包的依赖包（传递性依赖）。例如，A包的1.0版本依赖了B包的2.0版本，而B包的2.0版本依赖了C包的3.0版本，因此我的项目只要声明自己依赖了A包的1.0版本，`Maven`会自动从中央仓库下载B2.0和C3.0并将它们放在合适的位置上。然而`Maven`的一个明显缺点在于，它的构建是基于xml的，不够灵活。随后，2008年，`Gradle`出现了，以全面兼容`Maven`和脚本化配置的理念，迅速地占领了Java世界中`Maven`的领地。

回到Go的构建上来。随着Go的发展，有理由认为，"将项目本身的依赖库全部提交"这种机制很可能重蹈Java的覆辙。我个人是不喜欢`vendor`机制的，但是Go本身没有中央仓库的概念（或者说VCS就是中央仓库），代码的包名就对应了VCS的URL。这种机制有利有弊，优点非常明显，省去了中央仓库；缺点在于，很难对包的获取进行定制。例如，设立墙内镜像仓库，为某些包提供免VPN访问；或者企业内部对外部包的获取进行控制。

为此，我写了一个名为[Gogradle](https://github.com/gogradle/gogradle)的Gradle插件，提供全面的Go语言构建支持，包括包管理、自动化构建、测试/覆盖率HTML报告、IDE集成等。我不敢说它更优越，但希望至少为开发者提供一种思路。

## 使用Gogradle构建Go语言项目 

### 无需设置GOPATH

Gogradle不遵守Go的全局GOPATH约定。它会将所有的依赖放在项目目录下，因此无需预先设置`GOPATH`。你可以将你的Go项目放在任何位置。

### 安装JRE/JDK或使用JetBrains自带的JDK

Gradle是基于JVM的，因此你需要安装JDK或者JRE 8+。好消息是，如果你安装了JetBrains系列的IDE（IntellijIDEA/Gogland/WebStorm/PhpStorm/PyCharm/RubyMine/CLion）之一，那么你可以利用其自带的JDK，而无需额外安装。详见[Gogradle IDE支持](https://github.com/gogradle/gogradle/blob/master/docs/ide-cn.md)。

### 拷贝Gradle脚本

拷贝[Gogradle](https://github.com/gogradle/gogradle)项目下的`gradle`目录/`gradlew`/`gradlew.bat`到你的项目目录。这是Gradle提供的一种名为wrapper的机制，在运行wrapper脚本时，它会自动下载与当前构建一致的Gradle版本，因此我们实际上无需安装Gradle。

### 初始化

在项目文件夹下新建一个`build.gradle`文件，内容如下：

```
plugins {
    id 'com.github.blindpirate.gogradle' version '0.4.10' // 当前最新版本
}

golang {
    packagePath = 'github.com/your/package' // 欲构建项目的go import path，注意不是本地目录的路径！
}
```

然后在项目文件夹下执行

```
./gradlew goInit # *nix

gradlew goInit # Windows
```

在下文中，`gradlew`命令将以统一的`gradlew <task>`形式给出，不再区分平台。这会自动扫描你的项目并识别其依赖包。特别地，如果你之前使用过`glide/glock/godep/gom/gopm/govendor/gvt/gbvendor/trash/gpm`之一的依赖锁定工具，Gogradle能够自动识别它们生成的锁定文件。

`build.gradle`文件以一种基于[`Groovy`](http://groovy-lang.org/)的DSL写成，它指定了构建所需的步骤。Groovy语言可以看做是Java的超集，扩展了Java的一些语法，暂时可以不用深究其细节。

在墙内访问Gradle的插件仓库可能会碰到问题，若遇到问题，请参阅[离线使用Gogradle插件](https://github.com/gogradle/gogradle/blob/master/docs/getting-started-cn.md#离线使用gogradle插件)。

### 构建

在项目目录下运行`gradlew goBuild`或者`gradlew gB`。它会自动解析所有的依赖、传递性依赖，解决依赖包冲突，然后将依赖包安装到项目目录下并调用命令行执行`go build`。在执行时，它传递给go进程的环境变量`GOPATH`值为项目目录下的路径，因此全局的`GOPATH`**完全不起作用**。这也是为什么无需设置全局`GOPATH`的原因。

你甚至无需预先安装go，Gogradle如果发现你的机器上没有安装go，会自动下载安装go的最新版本。

### 测试

在项目目录下运行`gradlew goTest`或者`gradlew gT`。它会逐个包执行测试并生成HTML格式的测试/覆盖率报告。下图是[`gogs`](https://github.com/gogits/gogs)项目在我的Mac上的测试结果：


![1](https://raw.githubusercontent.com/blindpirate/gogradle/master/docs/images/index.png)
![1](https://raw.githubusercontent.com/blindpirate/gogradle/master/docs/images/classes.png)
![1](https://raw.githubusercontent.com/blindpirate/gogradle/master/docs/images/failedtest.png)

覆盖率报告

![1](https://raw.githubusercontent.com/blindpirate/gogradle/master/docs/images/coverage.png)
![1](https://raw.githubusercontent.com/blindpirate/gogradle/master/docs/images/coveragepackage.png)

### 自定义任务

上文的`goInit`/`goBuild`/`goTest`都是Gradle中的任务（Task），你可以定义自己的任务或者修改当前已经存在的任务，并自定义任务间的依赖。例如，你希望自定义一个任务来执行`go lint`（Gogradle已经内置了对gofmt/go vet的支持，详见[这里](https://github.com/gogradle/gogradle/blob/master/docs/tasks-cn.md#gocheck)）：

```
//确保golint已经存在于你的$PATH中
task golint(type: Go){
    doLast {
        run('golint github.com/my/package')
    }
}

goCheck.dependsOn golint // 令Gogradle预定义的goCheck任务依赖golint任务
```

完整的示例代码戳[这里](https://github.com/gogradle/examples/tree/master/gofmt-vet-golint)。

### 依赖包管理

依赖包管理是Gogradle的重点。你可以在`build.gradle`中声明所需的依赖，Gogradle会自动检索、下载所有的依赖包以及传递性依赖。下列代码给出了一些声明依赖的方式（位于`build.gradle`中）：

```
dependencies {
    golang {
        build 'github.com/user/project'  // No specific version, the latest will be used
        build name:'github.com/user/project' // Equivalent to last line
    
        build 'github.com/user/project@1.0.0-RELEASE' // Specify a version(tag in Git)
        build name:'github.com/user/project', tag:'1.0.0-RELEASE' // Equivalent to last line

        build name: 'github.com/user/project', url:'https://github.com/user/project.git', tag:'v1.0.0' // 指定url，例如镜像仓库
    
        test 'github.com/user/project#d3fbe10ecf7294331763e5c219bb5aa3a6a86e80' // Specify a commit
        test name:'github.com/user/project', commit:'d3fbe10ecf7294331763e5c219bb5aa3a6a86e80' // Equivalent to last line
    }
}
```

如果你曾在Java世界中使用过Gradle，那么你应该对这种语法很熟悉。其中，build和test依赖分别是两个隔离的依赖包空间，在`go build`时go进程只能看到声明为`build`的依赖，在`go test`时go进程能同时看到`build`和`test`的依赖。提供依赖包的隔离机制有利于减少依赖包的数量。例如，你依赖了一个第三方库，你只会关心这个库的`build`依赖，而不会关心它的`test`依赖。

Gogradle还支持许多的高级依赖管理机制，如语义化版本、传递性依赖排除、指定的传递性依赖排除、本地文件夹依赖等。更多细节可参阅[依赖管理](https://github.com/gogradle/gogradle/blob/master/docs/dependency-management-cn.md)。

### 镜像仓库或自定义包来源

在当前的Go语言标准工作空间中，有些事情是难以方便地完成的。考虑以下两种场景：

你fork了`github.com/gebi/laowang`到自己的仓库`github.com/my/laowang`，并做了修改。你对你自己的修改是如此的满意，以至于你希望在任何时候，都使用自己的版本，这意味着你所有的项目中依赖的包、你依赖的包中的`vendor`目录中任何地方，只要引用了`github.com/gebi/laowang`，一律替换成你修改后的版本。

要完成这件事情，我们需要在`build.gradle`中加入：

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

另外一个场景，一个企业希望为github设置墙内镜像站，使得企业内部在任何时候，都使用自建的github镜像`my-repo.com`，以达到提速和内控的目的。据此，我们需要在`build.gradle`中加入：

```
repositories {
    golang {
        root ~/github\.com\/[\w-]+\/[\w-]+/
        url { path->
            def split = path.split('/')
           "https://my-repo.com/${split[1]}/${split[2]}" 
        }
    }
}   
```
其中`root`接收任何参数，包括字符串、正则表达式，以及闭包。在这个例子中，所有的包路径都会和`~/github\.com\/[\w-]+\/[\w-]+/`比较，如果匹配，那么该路径就会被传入`url`所指定的闭包，生成最终的地址返回。

有关仓库管理的更多内容，请查阅[仓库管理](https://github.com/gogradle/gogradle/blob/master/docs/repository-management-cn.md)。

你可能会说，这有什么简单的，还不是要在每个项目里都加上这么多配置？有一种方法可以简化这个过程：编写一个Gradle插件，将这个逻辑移至该插件，这样，任何需要应用这个逻辑的地方都只需要`apply plugin:'my.custom.repositories.management'`即可。

### 查看依赖

欲查看本项目的依赖包，只需在项目目录下执行`gradlew goDependencies`或者`gradlew gD`。这是[`gogs`](https://github.com/gogits/gogs)项目v0.9.113的依赖树。在其项目目录下存在glide.lock文件，这是glide工具生成的，因此Gogradle自动识别并导入了它。

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

Gogradle仍在活跃的开发过程中，如果有任何意见和建议欢迎反馈！





在Gogradle中，你可以声明

```

gradlew goBuild # Windows
或者

./gradlew gB # *nix

gradlew gB # Windows








除此之外，我们不妨来看这样一个场景：在你自己的Go项目中，你使用了`github.com/gebi/laowang`这个包，但是你觉得它不够好，因此你fork了一份到自己的仓库`github.com/my/laowang`中，并进行了一些修改。你对自己的修改很满意，因此你希望在你自己的工程中全部使用`github.com/my/laowang`这个包。然而，众多的依赖包都在`vendor`里自带了`github.com/gebi/laowang`的包，你没有办法影响它们。





类似于Make，二者的优缺点如下：

### Make

优点：

- GNU老牌项目，品质保证
- 借助shell script，功能强大
- 全世界人民都在用，POSIX平台的事实标准

缺点：
- 难。Makefile和shell script学习曲线陡峭
- 跨平台能力差（尤其是Windows）
- 难以调试，缺乏IDE支持
- 缺乏插件机制，代码难以共享

### Gradle

优点：

- 
- 优秀的跨平台能力
- Gradle拥有众多Feature和插件

缺点：

- Heavy

## 为什么要在Go项目中使用Gradle


在开始之前，我想请大家回顾一段历史