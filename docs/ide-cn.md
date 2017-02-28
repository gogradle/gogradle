# IDE集成

Go语言是一门静态类型语言，因此许多IDE对其提供了支持，如[VSCode](https://github.com/Microsoft/vscode-go)、[IDEA](https://github.com/go-lang-plugin-org/go-lang-idea-plugin)和[Gogland](https://www.jetbrains.com/go/)。
通常，这些IDE需要用户在使用之前手工设置`GOPATH`并在其中准备好依赖的代码包。Gogradle简化了这个流程，希望能够让用户无需进行任何配置即可进行开发。

有了Gogradle，你的开发过程会变成：

- 安装IDE（只需进行一次）
- Clone项目到本地
- 使用IDE打开项目
- 开始开发！

在这个过程中，你无需设置包括GOROOT和GOPATH在内的任何东西，也无需执行`go get`/`glide install`等各种解析依赖包的命令！

Gogradle帮你完成的事情有：
- 下载正确的Go版本（若其不存在的话）
- 解析依赖并安装至项目目录下的`.gogradle`目录
- 生成项目的配置文件，将Go SDK指向正确的位置，并设置IDE的依赖包路径为上述`.gogradle`目录

## 安装IDE和Golang插件

在开始前，你需要首先安装IDE和Golang插件。Gogradle分别在Mac 10.11和Windows 7下的IntelliJ IDEA 2016.3/Gogland 1.0 EAP/WebStorm 2016.3/PhpStorm 2016.3/PyCharm 2016.3/RubyMine 2016.3/CLion 2016.3下完成了测试。理论上，它适用于Windows/Mac/Linux平台上的以下产品：

- IntelliJ IDEA 2016.1+ (Ultimate and Community)
- Gogland 1.0 EAP
- WebStorm 2016.1+
- PhpStorm 2016.1+
- PyCharm 2016.1+
- RubyMine 2016.1+
- CLion 2016.1+

以上IDE除Gogland之外都需要安装[Golang插件](https://github.com/go-lang-plugin-org/go-lang-idea-plugin)。

Mac下：

```
Preferences > plugins > Browse Repositories > 搜索'Go'并安装
```

Windows下：


## 准备JRE

使用`java -version`来检查你的JRE版本。Gogradle运行需要JRE 8以上，若你的JRE低于这个版本，请升级。

若你尚未安装JRE，可以使用IDE自带的JRE。JetBrains系列的IDE都自带了JRE 8。在Mac下，自带的JRE位于

```
/Applications/<PRODUCT>.app/Contents/jdk/Contents/Home/jre
```
其中`<PRODUCT>`可以是IntelliJ IDEA/Gogland/WebStorm/PhpStorm/PyCharm/RubyMine/CLion

在Windows下，自带的JRE位于


设置环境变量`JAVA_HOME`为以上路径即可。

## 准备构建脚本

按照[入门指南](./getting-started-cn.md)放置`gradlew`脚本、`gradle`目录，以及`build.gradle`构建脚本。

## IntelliJIDEA

- 方式一：在项目目录下，执行`gradlew idea`，完成后使用IDEA的`File`-`Open`打开项目所在目录即可。
- 方式二：使用IDEA的`File`-`Open`，打开项目目录下的`build.gradle`文件，此时IDEA会弹出对话框，选择`Open as Project`即可。

![1](https://raw.githubusercontent.com/blindpirate/gogradle/master/docs/openproject.png)

IDEA集成了Gradle，因此，你可以通过`View > ToolWindows > Gradle`来开启之，并通过Gradle工具栏上的按钮来执行同步操作：

![1](https://raw.githubusercontent.com/blindpirate/gogradle/master/docs/gradle.png)

## Gogland

在项目目录下执行`gradlew gogland`

## WebStorm

在项目目录下执行`gradlew webStorm`或者`gradlew wS`

## PhpStorm

在项目目录下执行`gradlew phpStorm`或者`gradlew pS`

## PyCharm

在项目目录下执行`gradlew pyCharm`或者`gradlew pC`

## RubyMine

在项目目录下执行`
/RubyMine/CLion


> **注意** 如果你是第一次使用，或者在上述过程中你下载了一个新版的Go，你可能需要重启IDE来使其生效。Gogradle会在构建的最后提示：
>
> `you need to restart the IDE to make it come into effect`

## Vim

由于Vim是直接通过环境变量来读取GOPATH的，因此可以通过`gradlew showGopathGoroot`或者`gradlew sGG`命令获取项目级的GOPATH和GOROOT：



