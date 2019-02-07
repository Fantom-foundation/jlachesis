# jLachesis

A Java-version of Lachesis implementation (available at https://github.com/Fantom-foundation/go-lachesis)

The main goal of the project is to provide a prototype of a DAG-based consensus protocol written in Java.
By using an OO language, it hopes to give a clear design and architecture (to those who are fans of Java :))
of the consensus protocol.

Check source code at: https://github.com/Fantom-foundation/jlachesis

# Roadmap

jLachesis aims to deliver a prototype that preserves the semantics of Lachesis protocol.
We use this as a base for our other research projects.

For the technical side, jLachesis uses Java 8+, Maven, Protocol Buffers, Grpc, Springboot and JUnit.

# Requirements

You will need the followings to compile:

Java jdk 1.8+
Maven 2+

Code has been developed and tested mainly on Ubuntu 18.04 LTS.


# Compile & run

Compile command:

mvn clean compile

For code development, it's recommended to use an IDE (such as Eclipse).

# Progress

The conversion from GoLang code to Java is complete. The journey was extremely fun.

The technical challenge is mainly due to the syntatic sugar, the idioms
and the libraries used between the two languages.

We will shortly put a tutorial to showcase as well as to give lessons learnt through the process.

# Contact

The project is still in the early state. Any feedbacks or contributions are all welcome.

For questions or suggestions about this project, please directly contact:
Quan Nguyen: [quan.nguyen]@fantom.foundation

