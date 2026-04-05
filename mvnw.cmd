@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper startup batch script, version 3.3.2
@REM ----------------------------------------------------------------------------

@echo off
@setlocal

set ERROR_CODE=0
@setlocal

@REM ==== START VALIDATION ====
@REM Nếu JAVA_HOME sai (vd. Oracle javapath), ghi đè bằng java.home từ JVM
if exist "%JAVA_HOME%\bin\java.exe" goto OkJHome
for /f "tokens=2 delims==" %%H in ('java -XshowSettings:properties -version 2^>^&1 ^| findstr /c:"java.home"') do set "JAVA_HOME=%%H"
if defined JAVA_HOME (
  if "%JAVA_HOME:~0,1%"==" " set "JAVA_HOME=%JAVA_HOME:~1%"
)
if not "%JAVA_HOME%" == "" if exist "%JAVA_HOME%\bin\java.exe" goto OkJHome
echo. >&2
echo Error: JAVA_HOME invalid or java.home could not be detected. >&2
goto error

:OkJHome
if exist "%JAVA_HOME%\bin\java.exe" goto init
echo. >&2
echo Error: JAVA_HOME is invalid: "%JAVA_HOME%" >&2
goto error

@REM ==== END VALIDATION ====

:init
set MAVEN_PROJECTBASEDIR=%MAVEN_BASEDIR%
IF NOT "%MAVEN_PROJECTBASEDIR%"=="" goto endDetectBaseDir

set EXEC_DIR=%CD%
set WDIR=%EXEC_DIR%
:findBaseDir
IF EXIST "%WDIR%"\.mvn goto baseDirFound
cd ..
IF "%WDIR%"=="%CD%" goto baseDirNotFound
set WDIR=%CD%
goto findBaseDir

:baseDirFound
set MAVEN_PROJECTBASEDIR=%WDIR%
cd "%EXEC_DIR%"
goto endDetectBaseDir

:baseDirNotFound
set MAVEN_PROJECTBASEDIR=%EXEC_DIR%
cd "%EXEC_DIR%"

:endDetectBaseDir

IF NOT EXIST "%MAVEN_PROJECTBASEDIR%\.mvn\jvm.config" goto endReadAdditionalConfig

@setlocal EnableExtensions EnableDelayedExpansion
for /F "usebackq delims=" %%a in ("%MAVEN_PROJECTBASEDIR%\.mvn\jvm.config") do set JVM_CONFIG_MAVEN_PROPS=!JVM_CONFIG_MAVEN_PROPS! %%a
@endlocal & set JVM_CONFIG_MAVEN_PROPS=%JVM_CONFIG_MAVEN_PROPS%

:endReadAdditionalConfig

SET MAVEN_JAVA_EXE="%JAVA_HOME%\bin\java.exe"
set WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
set WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

set WRAPPER_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar"

FOR /F "usebackq tokens=1,2 delims==" %%A IN ("%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties") DO (
 IF "%%A"=="wrapperUrl" SET WRAPPER_URL=%%B
)

if exist %WRAPPER_JAR% goto runWrapper

if "%MVNW_VERBOSE%" == "true" (
 echo Couldn't find %WRAPPER_JAR%, downloading it ...
 echo Downloading from: %WRAPPER_URL%
)

powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "$u='%WRAPPER_URL:'=''%'; $o='%WRAPPER_JAR:'=''%'; [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; (New-Object Net.WebClient).DownloadFile($u, $o)"

if not exist %WRAPPER_JAR% (
 echo Failed to download maven-wrapper.jar >&2
 goto error
)

:runWrapper
set MAVEN_CMD_LINE_ARGS=%*

%MAVEN_JAVA_EXE% ^
 %JVM_CONFIG_MAVEN_PROPS% ^
 %MAVEN_OPTS% ^
 %MAVEN_DEBUG_OPTS% ^
 -classpath %WRAPPER_JAR% ^
 "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" ^
 %WRAPPER_LAUNCHER% %MAVEN_CONFIG% %*
if ERRORLEVEL 1 goto error
goto end

:error
set ERROR_CODE=1

:end
@endlocal & set ERROR_CODE=%ERROR_CODE%
cmd /C exit /B %ERROR_CODE%
