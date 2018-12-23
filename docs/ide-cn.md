# IDE集成

Go语言是一门静态类型语言，因此许多IDE对其提供了支持，如[VSCode](https://github.com/Microsoft/vscode-go)、[IDEA](https://github.com/go-lang-plugin-org/go-lang-idea-plugin)和[GoLand](https://www.jetbrains.com/go/)。
由于Gogradle使用的是标准的vendor机制，因此理论上任何支持vendor的IDE都可以和Gogradle集成。

将Gogradle与IDE集成的步骤是：

- 安装IDE
- 运行`gradlew goVendor`安装依赖到项目中
- 运行`gradlew showGopathGoroot`或者`gradlew sGG`打印项目的`GOPATH`和`GOROOT`
- 使用上述`GOROOT`和`GOPATH`配置IDE
- 开始开发！

> 注意，尽管Gogradle支持项目放在任意目录下，许多IDE仍然支持有限。因此，为了避免不必要的麻烦，强烈建议在使用IDE时候遵守Go的全局`GOPATH`约定，即将项目放在`GOPATH`的对应路径下。

## 安装IDE和Golang插件
 
在开始前，你需要首先安装IDE和Golang插件。
 
- [IntelliJ IDEA (Ultimate and Community)](https://www.jetbrains.com/idea/)
- [VSCode](https://code.visualstudio.com/)
- [GoLand](https://www.jetbrains.com/go/)
- [WebStorm](https://www.jetbrains.com/webstorm)
- [PhpStorm](https://www.jetbrains.com/phpstorm)
- [PyCharm](https://www.jetbrains.com/pycharm)
- [RubyMine](https://www.jetbrains.com/ruby)
- [CLion](https://www.jetbrains.com/clion)
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
其中`<PRODUCT>`可以是IntelliJ IDEA/GoLand/WebStorm/PhpStorm/PyCharm/RubyMine/CLion
 
在Windows下，自带的JRE位于
 
```
<IDE的安装路径>/jre/jre
```

设置环境变量`JAVA_HOME`为以上路径即可。