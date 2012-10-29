#/bin/bash

find . -type f -name ".classpath" | xargs rm -rf 
find . -type f -name ".cache" | xargs rm -rf 
find . -type f -name ".project" | xargs rm -rf
find . -type f -name ".settings" | xargs rm -rf
find . -type f -name ".target" | xargs rm -rf

