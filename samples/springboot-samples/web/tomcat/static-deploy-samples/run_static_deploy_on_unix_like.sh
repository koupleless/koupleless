#!/bin/bash

# Step 1: Navigate to each project directory and build the projects
BASE_DIR="$(dirname "$(pwd)")"

echo "Building Maven projects..."
cd $BASE_DIR && mvn clean package -Dmaven.test.skip=true

# Step 2: Move the JAR files to the specified locations
echo "Moving JAR files..."
mkdir -p $BASE_DIR/static-deploy-samples/biz
mv $BASE_DIR/base/bootstrap/target/*.jar $BASE_DIR/static-deploy-samples/
mv $BASE_DIR/biz1/target/*-biz.jar $BASE_DIR/static-deploy-samples/biz/
mv $BASE_DIR/biz2/target/*-biz.jar $BASE_DIR/static-deploy-samples/biz/

# Step 3: Navigate to the deploy directory and launch the base jar
cd $BASE_DIR/static-deploy-samples
echo "Launching base application..."
echo "java -Dcom.alipay.sofa.ark.static.biz.dir=./biz -jar base-web-single-host-bootstrap-0.0.1-SNAPSHOT.jar"
java -Dcom.alipay.sofa.ark.static.biz.dir=./biz -jar base-web-single-host-bootstrap-0.0.1-SNAPSHOT.jar

echo "Script completed."
