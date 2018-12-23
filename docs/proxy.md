# Set Proxy For Build 

To set a proxy, you can add arguments in `gradlew`: 

```./gradlew goBuild -DsocksProxyHost=127.0.0.1 -DsocksProxyPort=1080```

And it is the same for other `gradlew` command.

Also, you can persist the arguments via `~/.gradle/gradle.properties` or `${projectRoot}/gradle.properties`:

```
org.gradle.jvmargs=-DsocksProxyHost=127.0.0.1 -DsocksProxyPort=1080
```

And HTTP proxy:

```
-Dhttp.proxyHost=<host> -Dhttp.proxyPort=<port>
```

And HTTPS proxy:

```
-Dhttps.proxyHost=<host> -Dhttps.proxyPort=<port>
```

To learn more about environment and proxy, see [Gradle environment](https://docs.gradle.org/current/userguide/build_environment.html#sec:gradle_properties_and_system_properties) and [Java proxy](http://docs.oracle.com/javase/8/docs/technotes/guides/net/proxies.html)

NOTE this only sets proxy for `Gogradle` process, not for `git` and `hg`. If you want to set proxy for `git` and `hg`, please visit [Getting git to work with a proxy server](http://stackoverflow.com/questions/783811/getting-git-to-work-with-a-proxy-server) and [using hg through a proxy](http://bayo.opadeyi.net/2012/08/using-hg-through-proxy.html) 