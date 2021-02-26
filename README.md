# record

![tests](https://github.com/zuqq/record/actions/workflows/tests.yml/badge.svg)

An implementation of the Git object model. As it uses POSIX file permissions,
it's unlikely to work on Windows.

## Usage

Note that Record doesn't use an index, so you can only commit your entire
working directory at once.

**Example**:

```bash
mvn compile
export CLASSPATH="$(pwd)/target/classes"

# Navigate to the working directory.
mkdir test-repo
cd test-repo

# Initialize the repository.
java record.Main init

# Create a file.
echo "a" > a

# Set committer name and email.
export GIT_COMMITTER_NAME="Jane Doe"
export GIT_COMMITTER_EMAIL="jane@example.com"

# Commit!
java record.Main commit -m "Initial commit"
```

**Help page:**

```
$ java record.Main
Usage:	record init
	record commit -m <message>
	record branch <branch>
	record checkout <branch or commit>
```
