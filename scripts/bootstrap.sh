#!/bin/bash

./setup-environment-for-tests.sh
cd ..
./sbt clean-files test
