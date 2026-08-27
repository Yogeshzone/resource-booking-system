@echo off
setlocal

rem If JAVA_HOME is not set, attempt standard JDK discovery
if "%JAVA_HOME%"=="" (
    for /d %%J in ("%ProgramFiles%\Java\jdk*" "%ProgramFiles(x86)\Java\jdk*" "%ProgramFiles(x86)\jdk*" "%ProgramFiles%\jdk*" "%USERPROFILE%\.jdks\*" "%USERPROFILE%\Downloads\*jdk*") do (
        if exist "%%J\bin\javac.exe" set "JAVA_HOME=%%J"
    )
)

if not "%JAVA_HOME%"=="" (
    set "PATH=%JAVA_HOME%\bin;%PATH%"
)

rem 1. Check MAVEN_HOME
if not "%MAVEN_HOME%"=="" (
    if exist "%MAVEN_HOME%\bin\mvn.cmd" (
        "%MAVEN_HOME%\bin\mvn.cmd" %*
        exit /b %ERRORLEVEL%
    )
)

rem 2. Check installed Maven wrapper distribution in user home
for /f "delims=" %%F in ('dir /b /s "%USERPROFILE%\.m2\wrapper\dists\mvn.cmd" 2^>nul') do (
    if exist "%%F" (
        "%%F" %*
        exit /b %ERRORLEVEL%
    )
)

rem 3. Check system PATH
for /f "delims=" %%I in ('where mvn.cmd 2^>nul') do (
    "%%I" %*
    exit /b %ERRORLEVEL%
)

for /f "delims=" %%I in ('where mvn 2^>nul') do (
    "%%I" %*
    exit /b %ERRORLEVEL%
)

mvn %*

