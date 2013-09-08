Migratable Process
=========

Mirgratable Process is project a course project of CMU 15440 - Distributed Systems. The goal of this project is migrating a process from a node to another node without losing any data. We defined a migratable process is a process which *only* handles the file input and file output. This project's components are listed below:

  - MigratableProcess
  - ProcessManager
  - TransactionalFileIOStream

This project is built by *[Apache Maven]("http://maven.apache.org/", "Apache Maven")*.  

How to Build
-----------

```sh
mvn compile
```

How to Run
----------

```sh
mvn exec:java
```

How to Package
----------

```sh
mvn assembly:single
```
The executable jar package is located at `target/mprocess-VERSION-jar-with-dependencies.jar`

How to Generate Doc
----------

```sh
mvn javadoc:javadoc
```
The generated HTML doc is located at `doc/`

Cheers! You're on board


  

