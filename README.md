# record

[![Build Status](https://travis-ci.com/zuqq/record.svg?branch=master)](https://travis-ci.com/zuqq/record)

An implementation of the git object model.

## Usage

Note that record doesn't use an index, so you can only commit your entire
working directory at once.

**Example**:

```
$ mvn compile
$ export CLASSPATH="$(pwd)/target/classes"

# Navigate to the working directory.
$ mkdir test-repo
$ cd test-repo

# Initialize the repository.
$ java record.Main init

# Create a file.
$ echo 'a' > a

# Set committer name and email.
$ export GIT_COMMITTER_NAME="Jane Doe"
$ export GIT_COMMITTER_EMAIL="jane@example.com"

# Commit!
$ java record.Main commit -m "Initial commit"
```

Verify that a commit was created by running `git log`.

If you look at the output of `git status`, you will see that git thinks that the
working directory has changed; that's because record doesn't maintain the index.
Run `git add .` to let the index catch up.

**Help page:**

```
Usage:	record init
	record commit -m MESSAGE
	record checkout COMMIT
```
