# loadbalancer

Learning project for building load balancing component in kotlin

## Disclaimer

It's my first attempt to create something by myself in kotlin, so some of the code 
could be non-idiomatic or not aligned with kotlin best practices.  

## Main functionality

`ua.kiev.tinedel.loadbalancer.balancer.LoadBalancer` - see KDoc
`ua.kiev.tinedel.loadbalancer.Provider` - see KDoc

## Tests

Project is fully covered with tests both Unit and Integration.
Convention for names - unit tests ending is Test and Integration tests ending is IT.

## Build

Gradle wrapper is used to bootstrap gradle. To build and test the project it is enough to execute
```
./gradlew clean build
``` 

Docs are generated as following
```
./gradlew dokkaHtml
```

## CI

Github's actions mechanism is used to run CI. Source code for actions is located in `.github/workflows` and it 
contains one workflow only - full build triggered on push to master or when pull request for master is created.

Continuous deployment is not required for this project.
