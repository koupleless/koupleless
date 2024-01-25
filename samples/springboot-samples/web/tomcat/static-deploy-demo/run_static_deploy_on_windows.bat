@echo off
REM Step 1: Navigate to each project directory and build the projects
SET BASE_DIR=%~dp0..

echo Building Maven projects...
call mvn -f "%BASE_DIR%" clean package -Dmaven.test.skip=true

REM Step 2: Move the JAR files to the specified locations
echo Moving JAR files...
if not exist "%BASE_DIR%\static-deploy-demo\biz" mkdir "%BASE_DIR%\static-deploy-demo\biz"
move "%BASE_DIR%\base\bootstrap\target\*.jar" "%BASE_DIR%\static-deploy-demo\"
move "%BASE_DIR%\biz1\target\*-biz.jar" "%BASE_DIR%\static-deploy-demo\biz\"
move "%BASE_DIR%\biz2\target\*-biz.jar" "%BASE_DIR%\static-deploy-demo\biz\"

REM Step 3: Navigate to the deploy directory and launch the base jar
cd "%BASE_DIR%\static-deploy-demo"
echo Launching base application...
echo java -Dcom.alipay.sofa.ark.static.biz.dir=./biz -jar base-web-single-host-bootstrap-0.0.1-SNAPSHOT.jar
java -Dcom.alipay.sofa.ark.static.biz.dir=./biz -jar base-web-single-host-bootstrap-0.0.1-SNAPSHOT.jar