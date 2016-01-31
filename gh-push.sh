#!/usr/bin/env bash

git config --global user.email "nonylene.app@gmail.com"
git config --global user.name "nonybot"

git branch gh-pages origin/gh-pages
git checkout gh-pages -- repository/

./gradlew --stacktrace clean :photolinkviewer-core:uploadArchives

git symbolic-ref HEAD refs/heads/gh-pages
git reset

git add repository/
if [ -n "$(git diff --cached --exit-code)" ]; then
    CI_RELEASE_DATE=`date +"%Y%m%d%H%M%S"`
    CI_REMOTE_REPOSITORY="git@github.com:${CIRCLE_PROJECT_USERNAME}/${CIRCLE_PROJECT_REPONAME}.git"

    git commit -m "[auto] SNAPSHOT-${CI_RELEASE_DATE}"
    git push ${CI_REMOTE_REPOSITORY} "gh-pages"
fi

