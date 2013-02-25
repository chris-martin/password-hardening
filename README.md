SecLogin
========

Authors
-------

- Kelsey Francis     francis@gatech.edu

- Chris Martin       chris.martin@gatech.edu

Requirements
------------

- Java 6 or greater (tested with Java 7 update 13)

- Linux (tested on Fedora Core 18)

Usage
-----

To run, use the provided `seclogin.sh` Bash script from the directory in which it resides.

The first time you run `seclogin.sh`, it will automatically build SecLogin (target/seclogin.jar).

To create a user:
 $ ./seclogin.sh -a usernamehere

To log in:
 $ ./seclogin.sh

Dependencies
------------
- sbt (for build)
- Guava (for general Java utilities)
- Argparse4J (for command-line argument parsing)
- JLine (for console input)
- Commons Math (for statistics)
- JSR 305 (for @Nullable annotation)