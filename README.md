SecLogin
========

Authors
-------

- Kelsey Francis     francis@gatech.edu

- Chris Martin       chris.martin@gatech.edu

Collaborators
-------------

We had some discussions with Curtis Free about this project and the paper.

Requirements
------------

- Java 6 or greater (tested with Java 7 update 13)
- Linux (tested on Fedora Core 18)
- Internet connection (for building)

- The following dependencies are provided with the source or downloaded automatically at
  compilation time:

    - sbt (for build)
    - Guava (for general Java utilities)
    - Argparse4J (for command-line argument parsing)
    - JLine (for console input)
    - Commons Math (for statistics)
    - JSR 305 (for @Nullable annotation)
    - Logback and SLF4J (for logging)

Usage
-----

To create a user:

    $ ./seclogin.sh -a usernamehere

To log in:

    $ ./seclogin.sh

Add the `-v` flag to run in verbose mode, which will output a large amount of debug information.

Do not worry about compiling; the first time you run `seclogin.sh`, it will automatically
build SecLogin (target/seclogin.jar).

SecLogin will create a `.seclogin` directory in the directory where you run the it, so your
user will need permission to do so.

Demo Setup
-------------------

For the purposes of demoing SecLogin, it asks the following 3 questions, where t is the system-wide
response mean parameter:
- How far (in miles) are you from the Georgia Tech campus? (t = 1)
- How long (in minutes) do you anticipate being logged in during this session? (t = 20)
- How many emails will you send during this session? (t = 2)

For flexibility, our implementation supports a distinct k parameter (standard deviation factor)
for each question, but we use k = 2 for all questions in the demo.

Again for simplicity of demoing, the default history file size is 2 entries. To change the history
file size to `h`, delete all previous state in the `.seclogin` directory and run:

    $ ./seclogin.sh --historyfilesize h

