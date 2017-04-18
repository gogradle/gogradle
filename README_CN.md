# Gogradle - 完善的Go语言构建工具

[![Build Status](https://travis-ci.org/blindpirate/gogradle.svg?branch=master)](https://travis-ci.org/blindpirate/gogradle)
[![Build Status](https://ci.appveyor.com/api/projects/status/github/blindpirate/gogradle?branch=master&svg=true&)](https://ci.appveyor.com/api/projects/status/github/blindpirate/gogradle?branch=master&svg=true&passingText=windows%20build%20passing&failingText=windows%20build%20failing)
[![Coverage Status](https://coveralls.io/repos/github/blindpirate/gogradle/badge.svg?branch=master)](https://coveralls.io/github/blindpirate/gogradle?branch=master)
[![Java 8+](https://img.shields.io/badge/java-8+-4c7e9f.svg)](http://java.oracle.com)
[![Apache License 2](https://img.shields.io/badge/license-APL2-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt)

Gogradle是一个提供Go语言构建支持的Gradle插件。

> # 注意：0.4.2版本中包含breaking change。一些任务的名字发生了变更以避免冲突。请参阅详细文档。
>
> 2017-04-06 现在Gogradle可以在**不做任何额外设置**的情况下构建[Github's top 1000](http://github-rank.com/star?language=Go)中的666个！
>
> 2017-03-20 现在Gogradle已经能够生成HTML格式的测试/覆盖率报告了！
>
> 2017-02-26 现在Gogradle已经与IDE深度集成，安装IDE后，无需设置GOPATH，无需预先安装Go即可开始开发！

## 功能特性

- 除`JDK 8+`外无需预先安装任何东西（包括Go本身），若使用JetBrains系IDE可无需安装JDK
- 支持所有版本的Go且允许多版本共存
- 完美支持几乎所有平台（只要能够运行`Java`，本项目的所有测试在OS X 10.11/Ubuntu 12.04/Windows 7上通过）
- 项目级的依赖隔离，无需设置`GOPATH`
- 完善的包管理
  - 无需手工安装依赖包，只需指定版本
  - 安装两种版本控制工具：Git/Mercurial
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
  - 支持仓库url替换
  - 构建、测试依赖分别管理
  - 支持依赖树可视化
- 支持构建、测试、单个/通配符测试、交叉编译  
- 现代的、生产级别的自动化构建支持，添加自定义任务极其简单
- 原生的Gradle语法
- 额外为中国大陆开发者提供的特性，你懂的
- Shadowsocks支持
- IDE支持（IntelliJIDEA/WebStorm/PhpStorm/PyCharm/RubyMine/CLion/Gogland/Vim）
- 测试和覆盖率报告生成
- 增量构建

## 优势

- 完善的跨平台支持
- 支持所有主流外部依赖管理工具
- 完善的测试、覆盖率功能以及报告生成
- 长期维护
- 众多Gradle插件

猛戳[这里](https://github.com/gogradle/samples)查看Gogradle的示例。

## 目录

- [入门](./docs/getting-started-cn.md)
- [依赖管理](./docs/dependency-management-cn.md)
- [Gogradle的任务](./docs/tasks-cn.md)
- [仓库管理](./docs/repository-management-cn.md)
- [HTTP与Socks代理](./docs/proxy-cn.md)
- [构建输出与交叉编译](./docs/cross-compile-cn.md)
- [IDE支持](./docs/ide-cn.md)

> **注意** Gogradle不遵循Golang的全局[工作空间的约定](https://golang.org/doc/code.html#Workspaces)。它不使用全局的GOPATH，而是为每个项目生成一个独立的GOPATH。

测试报告截图

![1](https://raw.githubusercontent.com/blindpirate/gogradle/master/docs/images/index.png)
![1](https://raw.githubusercontent.com/blindpirate/gogradle/master/docs/images/classes.png)
![1](https://raw.githubusercontent.com/blindpirate/gogradle/master/docs/images/packages.png)
![1](https://raw.githubusercontent.com/blindpirate/gogradle/master/docs/images/failedtest.png)

覆盖率报告截图

![1](https://raw.githubusercontent.com/blindpirate/gogradle/master/docs/images/coverage.png)
![1](https://raw.githubusercontent.com/blindpirate/gogradle/master/docs/images/coveragepackage.png)

## 向Gogradle贡献提出建议或贡献代码

若觉得不错，请Star。

有问题和需求请直接提[issue](https://github.com/blindpirate/gogradle/issues/new)。

欲和我一起改进Gogradle，请提交[PR](https://github.com/blindpirate/gogradle/pulls)。





