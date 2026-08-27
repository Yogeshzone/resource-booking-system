@echo off
setlocal

rem Detect and set JAVA_HOME if not set or pointing to old version
if "%JAVA_HOME%"=="" (
    if exist "C:\Program Files (x86)\jdk-23.0.2" set "JAVA_HOME=C:\Program Files (x86)\jdk-23.0.2"
    if exist "C:\Users\%USERNAME%\Downloads\oracleJdk-26" set "JAVA_HOME=C:\Users\%USERNAME%\Downloads\oracleJdk-26"
)

if not "%JAVA_HOME%"=="" set "PATH=%JAVA_HOME%\bin;%PATH%"

rem Look for Maven in .m2 wrapper dists
if exist "%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.16\0daed3be3ebd1c706f0e69e8b07c6b73f5cc4ea3dfce72a8d0ec2e849ca2ddb0\bin\mvn.cmd" (
    "%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.16\0daed3be3ebd1c706f0e69e8b07c6b73f5cc4ea3dfce72a8d0ec2e849ca2ddb0\bin\mvn.cmd" %*
    exit /b %ERRORLEVEL%
)

rem Fallback to PATH mvn
mvn %*

