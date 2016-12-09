# gogradle - a full-featured gradle plugin for golang
Don't use me. I'm under development.


# Dependency Management

Gogradle provides nearly native gradle DSLs for dependency management.

For example, you can declare your dependencies with following statements.

```
dependencies {
    build 'github.com/user/project'
    build 'github.com/user/project@1.0.0-RELEASE'
    build 'github.com/user/project#d3fbe10ecf7294331763e5c219bb5aa3a6a86e80'
    
    build name: 'github.com/user/project', version: '2.5'
    build name: 'github.com/user/project', tag: 'v1.0.0'
    build name: 'github.com/user/project', commit: 'commitId'
    
    build name: 'github.com/user/project', url:'https://github.com/user/project.git', tag:'v1.0.0', vcs:'git'
    build name: 'github.com/user/project', url:'git@github.com:user/project.git', tag:'v1.0.0', vcs:'git'
    
    build 'github.com/user/project@1.0.0',
            'github.com/user/anotherproject#commitId'
             
    build(
        [name: 'github.com/user/project', version: '2.5'],
        [name: 'github.com/user/project', commit: 'commitId']
    )
    build('github.com/user/project') {
        transitive = true
    }
    build name: 'github.com/user/project', tag: 'v1.0.0', transitive: true
    
    build(name: 'github.com/user/project', tag: 'v1.0.0') {
        transitive = true
        excludeVendor = true
        exclude module: 'github.com/user/anotherproject'
    }
    
    build dir('${GOPATH}/a/b') as pkg('github.com/a/b')
}
```
