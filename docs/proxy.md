# Set Proxy For Build 

To set a proxy, you can add arguments in `gradlew`: 

```./gradlew build -DsocksProxyHost=127.0.0.1 -DsocksProxyPort=1080```

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