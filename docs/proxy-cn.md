# 为构建设置代理

# 注意！！！，本节仅仅描述如何为`Gogradle`进程设置代理，而非`git`和`hg`。若希望为它们设置代理，请参阅[Getting git to work with a proxy server](http://stackoverflow.com/questions/783811/getting-git-to-work-with-a-proxy-server)和[using hg through a proxy](http://bayo.opadeyi.net/2012/08/using-hg-through-proxy.html)

若需要为Gradle设置代理，可以在`gradlew`命令中增加参数（以Shadowsocks为例）：

```./gradlew goBuild -DsocksProxyHost=127.0.0.1 -DsocksProxyPort=1080```

其他命令类似。

同时，你可以通过在`~/.gradle/gradle.properties`或`${projectRoot}/gradle.properties`中增加

```
org.gradle.jvmargs=-DsocksProxyHost=127.0.0.1 -DsocksProxyPort=1080
```

此外，你可以通过以下参数设置HTTP代理：

```
-Dhttp.proxyHost=<host> -Dhttp.proxyPort=<port>
```

以及HTTPS代理：

```
-Dhttps.proxyHost=<host> -Dhttps.proxyPort=<port>
```

来将此设置持久化，有关更多环境和代理的信息，详见[Gradle构建环境](https://docs.gradle.org/current/userguide/build_environment.html#sec:gradle_properties_and_system_properties)与[Java代理](http://docs.oracle.com/javase/8/docs/technotes/guides/net/proxies.html)

