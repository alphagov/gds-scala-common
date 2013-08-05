#!/bin/bash

if [ ! -d ~/alphagov.github.com/.git ]; then
    echo "Cloning alphagov to home dir"
    cd ~
    git clone git@github.com:alphagov/alphagov.github.com.git
    cd -
fi

#update the local repo
cd ~/alphagov.github.com/
git pull
cd -

#publish to local repo
./sbt publish
cd ~/alphagov.github.com/
./update-directory-index.sh

#push the local repo to remote
git add .
git commit -m "Successful CI build"
git push


