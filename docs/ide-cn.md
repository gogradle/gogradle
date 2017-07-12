# IDE集成

> 注意，尽管Gogradle支持项目放在任意目录下，许多IDE仍然支持有限。因此，为了避免不必要的麻烦，我推荐在使用IDE时候遵守Go的全局GOPATH约定，即将项目放在GOPATH的对应路径下。

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
- 解析依赖并安装至项目目录下的`vendor`目录
- 为IDE生成项目的配置文件，将Go SDK和GOPATH指向正确的位置

> 注意

## 安装IDE和Golang插件

在开始前，你需要首先安装IDE和Golang插件。Gogradle分别在Mac 10.11和Windows 7下的IntelliJ IDEA 2016.3/Gogland 1.0 EAP/WebStorm 2016.3/PhpStorm 2016.3/PyCharm 2016.3/RubyMine 2016.3/CLion 2016.3/VSCode 1.12.2下完成了测试。理论上，它适用于Windows/Mac/Linux平台上的以下产品：

- [IntelliJ IDEA 2016.1+ (Ultimate and Community)](https://www.jetbrains.com/idea/)
- [VSCode 1.12+](https://code.visualstudio.com/)
- [Gogland 1.0 EAP](https://www.jetbrains.com/go/)
- [WebStorm 2016.1+](https://www.jetbrains.com/webstorm)
- [PhpStorm 2016.1+](https://www.jetbrains.com/phpstorm)
- [PyCharm 2016.1+](https://www.jetbrains.com/pycharm)
- [RubyMine 2016.1+](https://www.jetbrains.com/ruby)
- [CLion 2016.1+](https://www.jetbrains.com/clion)
- [Vim](http://www.vim.org/)

你可以点击相应的链接下载之。

IntelliJ IDEA/WebStorm/PhpStorm/PyCharm/RubyMine/CLion需要安装[Golang插件](https://github.com/go-lang-plugin-org/go-lang-idea-plugin)。

Mac下：

```
Preferences > plugins > Browse Repositories > 搜索'Go'并安装
```

Windows下：
```
File > Settings > Plugins > Browse Repositories > 搜索'Go'并安装
```

VSCode需要安装[vscode-go](https://github.com/Microsoft/vscode-go)。

按[这里](https://code.visualstudio.com/docs/editor/extension-gallery)，搜索`Go`并安装即可。


Vim需要安装[vim-go](https://github.com/fatih/vim-go)，详情请参考其文档。


## 准备JRE

使用`java -version`来检查你的JRE版本。Gogradle运行需要JRE 8以上，若你的JRE低于这个版本，请升级。

若你尚未安装JRE，可以使用JetBrains系列IDE自带的JRE 8（VSCode没有带）。在Mac下，自带的JRE位于

```
/Applications/<PRODUCT>.app/Contents/jdk/Contents/Home/jre
```
其中`<PRODUCT>`可以是IntelliJ IDEA/Gogland/WebStorm/PhpStorm/PyCharm/RubyMine/CLion

在Windows下，自带的JRE位于

```
<IDE的安装路径>/jre/jre
```

设置环境变量`JAVA_HOME`为以上路径即可。

## 准备构建脚本并初始化

按照[入门指南](./getting-started-cn.md)放置`gradlew`脚本、`gradle`目录，以及`build.gradle`构建脚本。如果你的项目之前不是用Gogradle构建的，请执行`gradlew init`来执行初始化。

## 导入项目到IntelliJIDEA

- 方式一：在项目目录下，执行`gradlew idea`，完成后使用IDEA的`File`-`Open`打开项目所在目录即可。
- 方式二：使用IDEA的`File`-`Open`，打开项目目录下的`build.gradle`文件，此时IDEA会弹出对话框，选择`Open as Project`即可。

![1](https://raw.githubusercontent.com/blindpirate/gogradle/master/docs/images/openproject.png)

IDEA集成了Gradle，因此，你可以通过`View > ToolWindows > Gradle`来开启之，并通过Gradle工具栏上的按钮来执行同步操作：

![1](https://raw.githubusercontent.com/blindpirate/gogradle/master/docs/images/gradle.png)

额外地，你可以利用IDEA的Gradle支持来运行相关任务，如上图所示。


>
> **注意** 在上述过程中，如果你是第一次使用Gogradle，你可能需要重启IDEA来使其生效。Gogradle会在构建的最后提示：
>
> `you need to restart the IDE to make it come into effect`

## 导入项目到VSCode

在项目目录下执行`gradlew vscode`，然后使用`File`-`Open`打开项目目录即可。

## 导入项目到Gogland

在项目目录下执行`gradlew gogland`，然后使用`File`-`Open`打开项目目录即可。

## 导入项目到WebStorm

在项目目录下执行`gradlew webStorm`或者`gradlew wS`，然后使用`File`-`Open`打开项目目录即可。

## 导入项目到PhpStorm

在项目目录下执行`gradlew phpStorm`或者`gradlew pS`，然后使用`File`-`Open`打开项目目录即可。

## 导入项目到PyCharm

在项目目录下执行`gradlew pyCharm`或者`gradlew pC`，然后使用`File`-`Open`打开项目目录即可。

## 导入项目到RubyMine

在项目目录下执行`gradlew rubyMine`或者`gradlew rM`，然后使用`File`-`Open`打开项目目录即可。

## 导入项目到CLion

在项目目录下执行`gradlew cLion`或者`gradlew cL`，然后使用`File`-`Open`打开项目目录即可。

## Vim

由于Vim是直接通过环境变量来读取GOPATH的，因此可以通过`vendor`来安装依赖，然后通过`gradlew showGopathGoroot`或者`gradlew sGG`命令获取项目级的GOPATH和GOROOT，使用这些环境变量启动Vim即可。
