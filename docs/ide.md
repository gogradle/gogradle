# IDE Integration

There are many IDEs supporting golang since it is static-type, e.g., [VSCode](https://github.com/Microsoft/vscode-go)/[IDEA](https://github.com/go-lang-plugin-org/go-lang-idea-plugin)/[GoLand](https://www.jetbrains.com/go/).

Theoretically, any IDE which supports standard vendor mechanism can integrate Gogradle.

Steps to integrate Gogradle with IDE:

- Install IDE
- Run `gradlew goVendor` to install all dependencies to vendor
- Run `gradlew showGopathGoroot` or `gradlew sGG` to print `GOPATH` and `GOROOT` of your project
- Configure your IDE with `GOROOT` and `GOPATH` above
- Start work!

> Attention: although it's supported by Gogradle to put your project at any place, some IDEs can't support this scenario fully. Therefore, it's highly recommended to conform Go's GOPATH convention and place your project at corresponding location when using an IDE.

## Install IDE and Golang Plugin

At first, you need to have IDE and Golang plugin installed. 

- [IntelliJ IDEA (Ultimate and Community)](https://www.jetbrains.com/idea/)
- [VSCode](https://code.visualstudio.com/)
- [GoLand](https://www.jetbrains.com/go/)
- [WebStorm](https://www.jetbrains.com/webstorm)
- [PhpStorm](https://www.jetbrains.com/phpstorm)
- [PyCharm](https://www.jetbrains.com/pycharm)
- [RubyMine](https://www.jetbrains.com/ruby)
- [CLion](https://www.jetbrains.com/clion)
- [Vim](http://www.vim.org/)

Click the corresponding link and download your favorite IDE.

On IntelliJ IDEA/WebStorm/PhpStorm/PyCharm/RubyMine/CLion, [go-lang-idea-plugin](https://github.com/go-lang-plugin-org/go-lang-idea-plugin) is required。

On Mac:

```
Preferences > plugins > Browse Repositories > Search for 'Go' and install
```

On Windows:
```
File > Settings > Plugins > Browse Repositories > Search for 'Go' and install
```

And VSCode needs [vscode-go](https://github.com/Microsoft/vscode-go)。

Search `Go` and install the plugin as documented [here](https://code.visualstudio.com/docs/editor/extension-gallery).

On Vim, [vim-go](https://github.com/fatih/vim-go) is required, just follow the documentation.

## Install JRE

Check your Java version with `java -version`. Gogradle requires JRE 8+, so update it if necessary.

Fortunately, there are JREs shipped with JetBrains IDE, which you can use without extra installation.

On Mac, it is located in
```
/Applications/<PRODUCT>.app/Contents/jdk/Contents/Home/jre
```

Where `<PRODUCT>` can be IntelliJ IDEA/GoLand/WebStorm/PhpStorm/PyCharm/RubyMine/CLion

On Windows, it is located in

```
<INSTALLATION PATH OF IDE>/jre/jre
```

Set your environment variable `JAVA_HOME` to it and that's it!