#!/bin/sh

# Run in config dir, not else
cd ..
mvn clean package -DskipTests
cp ../../../../arkctl/bin/arkctl_linux_amd64 config/arkctl_linux_amd64
docker build .
rm config/arkctl_linux_amd64