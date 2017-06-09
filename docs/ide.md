# IDE Integration

There are many IDEs supporting golang since it is static-type, e.g., [VSCode](https://github.com/Microsoft/vscode-go)/[IDEA](https://github.com/go-lang-plugin-org/go-lang-idea-plugin)/[Gogland](https://www.jetbrains.com/go/).

Usually, these IDEs ask user to set `GOPATH` and prepare dependent package there before developing. Gogradle try to make it easier. Ideally, a user should be able to start development after checking out the code immediately, without understanding or setting anything.

With Gogradle, what you need to do is:

- Install IDE (only once)
- Clone code locally
- Open the project in IDE
- Develop!

In this process, you need not to set anything including GOROOT and GOPATH, nor run commands which resolve dependencies such as `go get`/`glide install`!

Gogradle can help you to:

- Download Go in correct version (if it does not exist)
- Resolve the dependencies and install them into `.gogradle` directory of project root
- Generate configuration file of project for IDE where Go SDK and `$GOPATH` is set correctly

## Install IDE and Golang Plugin

At first, you need to have IDE and Golang plugin installed. Gogradle has been tested in IntelliJ IDEA 2016.3/Gogland 1.0 EAP/WebStorm 2016.3/PhpStorm 2016.3/PyCharm 2016.3/RubyMine 2016.3/CLion 2016.3/VSCode 1.12.2 on Mac 10.11 and Windows 7.
Theoretically, it is applicable for:

- [IntelliJ IDEA 2016.1+ (Ultimate and Community)](https://www.jetbrains.com/idea/)
- [VSCode 1.12+](https://code.visualstudio.com/)
- [Gogland 1.0 EAP](https://www.jetbrains.com/go/)
- [WebStorm 2016.1+](https://www.jetbrains.com/webstorm)
- [PhpStorm 2016.1+](https://www.jetbrains.com/phpstorm)
- [PyCharm 2016.1+](https://www.jetbrains.com/pycharm)
- [RubyMine 2016.1+](https://www.jetbrains.com/ruby)
- [CLion 2016.1+](https://www.jetbrains.com/clion)
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

Where `<PRODUCT>` can be IntelliJ IDEA/Gogland/WebStorm/PhpStorm/PyCharm/RubyMine/CLion

On Windows, it is located in

```
<INSTALLATION PATH OF IDE>/jre/jre
```

Set your environment variable `JAVA_HOME` to it and that's it!

## Write Build Script and Initialization

Place `gradlew`/`gradle`/`build.gradle` as documented in [Getting Started](./getting-started.md), if your project is not managed by Gogradle previously, please run `gradlew init` now.

## Import project into IntelliJIDEA

- First way: run `gradlew goIdea` in project root directory, then do `File`-`Open` to the project root.
- Second way: use `File`-`Open` to open `build.gradle` and select `Open as Project` as follows.

![1](https://raw.githubusercontent.com/blindpirate/gogradle/master/docs/images/openproject.png)

IDEA has Gradle integrated, so you can enable it via `View > ToolWindows > Gradle` and sync your project with button on that tool window:

![1](https://raw.githubusercontent.com/blindpirate/gogradle/master/docs/images/gradle.png)

Moreover, you can use IDEA's Gradle Tool View to run tasks as illustrated above.


>
> **NOTE** If you're using Gogradle for the first time,
> you probably need to restart your IDEA to make it come into effect. Gogradle will display a tip line at the end of output:
>
> `you need to restart the IDE to make it come into effect`

## Import project into VSCode

Run `gradlew vscode` in project root directory and use `File`-`Open` to open project root directory.

## Import project into Gogland

Run `gradlew gogland` in project root directory and use `File`-`Open` to open project root directory.

## Import project into WebStorm

Run `gradlew webStorm` or `gradlew wS` in project root directory and use `File`-`Open` to open project root directory.

## Import project into PhpStorm

Run `gradlew phpStorm` or `gradlew pS` in project root directory and use `File`-`Open` to open project root directory.

## Import project intoPyCharm

Run `gradlew pyCharm` or `gradlew pC` in project root directory and use `File`-`Open` to open project root directory.

## Import project intoRubyMine

Run `gradlew rubyMine` or `gradlew rM` in project root directory and use `File`-`Open` to open project root directory.

## Import project intoCLion

Run `gradlew cLion` or `gradlew cL` in project root directory and use `File`-`Open` to open project root directory.


Since Vim reads GOPATH from environment variable directly, you can get project-scoped GOPATH and GOROOT via `gradlew showGopathGoroot` or `gradlew sGG` and run Vim with these environment variables.





