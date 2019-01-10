#! /bin/bash

function exit_on_error {
	[[ "$?" != 0 ]] && exit 1
}

cd `dirname $0`/..

# Check if master branch is active
branch=`git rev-parse --abbrev-ref HEAD`
if [ $branch != "master" ]; then
    echo "Not in master branch."
    exit 1
fi

# Test build
mvn clean install
exit_on_error

# generate doc
./lsql-doc/bin/generate_doc.sh
exit_on_error

# Set the current version
git tag "v`dev/get_current_version.sh`"
exit_on_error
dev/get_current_version.sh > LATEST_RELEASED_VERSION

# Commit and push
git add --all
git commit -m "new release `dev/get_current_version.sh`"
git push --tags origin master

# Deploy
mvn -Dmaven.test.skip=true deploy

VERSION=`dev/get_current_version.sh`

# Increment version and push
dev/increment_project_version.sh
git add --all
git commit -m "set development version to `dev/get_current_version.sh`"
git push origin master

echo --------------------------------------------------------------------
echo Released version $VERSION
echo --------------------------------------------------------------------
