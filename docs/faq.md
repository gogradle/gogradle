# FAQ

## How to tackle 'Cannot recognized package: appengine' ?

`appengine` and `appengine_internal` are GCE packages so they can't be fetched. You can add the following code in your `build.gradle`:

```
repositories {
    golang {
        root { it.startsWith('appengine') }
        emptyDir()
    }
}
```

## How to tackle `java.nio.file.FileSystemException: A required privilege is not held by the client` on Windows?

The way Gogradle supports project-level `GOPATH` is creating a symbolic link in project directory so it needs this privilege. You can try one of the following solution:
 
- Run as administrator or disable UAC
- Grant current user the privilege: [here](https://stackoverflow.com/questions/23217460/how-to-create-soft-symbolic-link-using-java-nio-files)
- Use global `GOPATH` in [canonical way](https://golang.org/doc/code.html#Workspaces)


## How to tackle conflict of Gogradle task names with other plugin task names?

See [here](https://github.com/gogradle/gogradle/blob/master/docs/tasks.md#task-name-conflict)

## Dose Gogradle support multi-project?

The answer is: partially support.