# Gogradle - 完善的Go语言构建工具

[![Build Status](https://travis-ci.org/gogradle/gogradle.svg?branch=master)](https://travis-ci.org/gogradle/gogradle)
[![Build Status](https://ci.appveyor.com/api/projects/status/github/gogradle/gogradle?branch=master&svg=true&passingText=windows%20build%20passing&failingText=windows%20build%20failing)](https://ci.appveyor.com/api/projects/status/github/gogradle/gogradle?branch=master&svg=true&passingText=windows%20build%20passing&failingText=windows%20build%20failing)
[![Coverage Status](https://coveralls.io/repos/github/blindpirate/gogradle/badge.svg?branch=master)](https://coveralls.io/github/blindpirate/gogradle?branch=master)
[![Java 8+](https://img.shields.io/badge/java-8+-4c7e9f.svg)](http://java.oracle.com)
[![Apache License 2](https://img.shields.io/badge/license-APL2-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt)

![1](https://raw.githubusercontent.com/blindpirate/gogradle/master/docs/images/go-mini.png)

# Gogradle 0.5.0包含核心模型变更，请详细阅读文档以获取相关信息

> 2017-04-06 现在Gogradle可以在**不做任何额外设置**的情况下构建[Github's top 1000](http://github-rank.com/star?language=Go)中的666个！
>
> 2017-03-20 现在Gogradle已经能够生成HTML格式的测试/覆盖率报告了！
>
> 2017-02-26 现在Gogradle已经与IDE深度集成，安装IDE后，无需设置GOPATH，无需预先安装Go即可开始开发！

## Gogradle是什么?

Gogradle是一个为Go语言提供现代构建支持的[Gradle](https://gradle.org)插件。Gogradle深受[glide](https://github.com/Masterminds/glide)启发（这里需要向它致以崇高的敬意），可以简单地理解为`glide`+`make`。

## 为什么使用Gogradle？

- `make`的学习曲线陡峭，许多人（比如我）并不擅长；[Gradle](https://gradle.org)使用语法接近Java的DSL来完成构建工作，更加容易使用
- `Makefile`和Shell跨平台能力差，尤其对Windows用户不友好，而受益于Gradle和JVM，Gogradle提供了无与伦比的跨平台支持，并且有成熟的Java生态系统支持
- Gradle生态系统拥有很多成熟的[插件](https://plugins.gradle.org)，你也可以方便的实现自己的插件，以复用构建的相关代码
- Gogradle支持项目级的`GOPATH`，你可以自己决定使用全局`GOPATH`还是项目级别的`GOPATH`
- Gogradle支持多Go版本共存和切换
- 社区的各种[依赖管理工具]((https://github.com/blindpirate/report-of-go-package-management-tool))众多，且互不兼容
  - Gogradle提供了[导入命令](./docs/getting-started-cn.md#准备工作)，从而使你能够方便地从其他工具迁移到Gogradle
  - Gogradle兼容`glide/glock/godep/gom/gopm/govendor/gvt/gbvendor/trash/gpm`工具。在查找一个依赖包的传递性依赖时，它能够自动识别工具的锁定文件
- Gogradle会长期维护

Gogradle实现了`glide`的绝大部分功能，并添加了一些额外的功能特性：

- 测试和覆盖率报告生成
- Go的多版本管理
- 全系列IDE支持（IntelliJIDEA/VSCode/WebStorm/PhpStorm/PyCharm/RubyMine/CLion/Gogland/Vim），尤其是IntellijIDEA的深度集成
- 使用动态语言特性完成仓库的声明和替换——即镜像仓库

如果你曾被上述问题之一困扰，又或者你曾是Java开发者、熟悉Gradle，那么Gogradle是你不二的选择！

Gogradle的目标不是取代其他的工具，而是为开发者提供一个额外的选项。

## 功能特性

- 除`JDK 8+`外无需预先安装任何东西（包括Go本身），若使用JetBrains系IDE可无需安装JDK
- 支持Go 1.5+且允许多版本共存
- 完美支持几乎所有平台（只要能够运行`Java`，本项目的所有测试在OS X 10.11/Ubuntu 12.04/Windows 7上通过）
- 项目级的依赖隔离，无需设置`GOPATH`
- 完善的包管理
  - 无需手工安装依赖包，只需指定版本
  - 安装两种版本控制工具：Git/Mercurial
  - 支持传递性依赖
  - 支持自定义传递性依赖策略
  - 自动解决冲突 
  - 支持依赖锁定
  - 支持glide/glock/godep/gom/gopm/govendor/gvt/gbvendor/trash/gpm等外部依赖的读取和导入（基于[这份报告](https://github.com/blindpirate/report-of-go-package-management-tool)）
  - 支持[submodule](https://git-scm.com/book/zh/v2/Git-%E5%B7%A5%E5%85%B7-%E5%AD%90%E6%A8%A1%E5%9D%97)
  - 支持[语义化版本](http://semver.org/)
  - 支持依赖的扁平化 （受[glide](https://github.com/Masterminds/glide)启发）
  - 支持本地包重命名
  - 支持私有仓库
  - 支持仓库url替换
  - 构建、测试依赖分别管理
  - 支持依赖树可视化
  - 依赖一个特定包的某个子包
- 支持构建、测试、单个/通配符测试、交叉编译  
- 现代的、生产级别的自动化构建支持，添加自定义任务极其简单
- 原生的Gradle语法
- 额外为中国大陆开发者提供的特性，你懂的
- Shadowsocks支持
- IDE支持（IntelliJIDEA/WebStorm/PhpStorm/PyCharm/RubyMine/CLion/Gogland/Vim）
- 测试和覆盖率报告生成
- 增量构建

## Gogradle如何工作

Gogradle基于[vendor](https://docs.google.com/document/d/1Bz5-UB7g2uPBdOx-rw5t9MxJwkfpx90cqG9AFL0JAYo)机制，你通过在`build.gradle`中使用Gradle的DSL来声明依赖和构建逻辑。
Gogradle会按照声明进行依赖包和传递性依赖包的解析，并解决冲突，然后安装到`vendor`目录中，最后执行构建。在这个过程中，依赖会被扁平化以避免可能出现的[问题](https://github.com/blindpirate/golang-broken-vendor)。稍后，你可以将解析后的依赖锁定，以实现可复现的构建。`vendor`目录是否提交到代码仓库中由你自己决定。

Gogradle支持项目级的`GOPATH`。在构建一个项目时，你可将其clone到任何地方。如果你的项目不在`GOPATH`下的相应位置，Gogradle会在项目目录下创建一个符号链接，然后在构建中使用这个符号链接作为`GOPATH`——在很多场景下，这是很有用的。

猛戳[这里](https://github.com/gogradle/samples)查看Gogradle的示例。

## 目录

- [入门](./docs/getting-started-cn.md)
- [依赖管理](./docs/dependency-management-cn.md)
- [Gogradle的任务](./docs/tasks-cn.md)
- [仓库管理](./docs/repository-management-cn.md)
- [HTTP与Socks代理](./docs/proxy-cn.md)
- [IDE支持](./docs/ide-cn.md)

测试报告截图

![1](https://raw.githubusercontent.com/blindpirate/gogradle/master/docs/images/index.png)
![1](https://raw.githubusercontent.com/blindpirate/gogradle/master/docs/images/classes.png)
![1](https://raw.githubusercontent.com/blindpirate/gogradle/master/docs/images/packages.png)
![1](https://raw.githubusercontent.com/blindpirate/gogradle/master/docs/images/failedtest.png)

覆盖率报告截图

![1](https://raw.githubusercontent.com/blindpirate/gogradle/master/docs/images/coverage.png)
![1](https://raw.githubusercontent.com/blindpirate/gogradle/master/docs/images/coveragepackage.png)

## 获取帮助

有任何问题，可以加入QQ群Gogradle交流群：

![1](https://raw.githubusercontent.com/blindpirate/gogradle/master/docs/images/group.png)

## 向Gogradle贡献提出建议或贡献代码

若觉得不错，请Star。

有问题和需求请直接提[issue](https://github.com/blindpirate/gogradle/issues/new)。

欲和我一起改进Gogradle，请提交[PR](https://github.com/blindpirate/gogradle/pulls)。





