@echo off
REM fastjson3 示例运行脚本 (Windows)

setlocal enabledelayedexpansion

echo fastjson3 示例代码运行脚本
echo.

REM 检查 Java
echo [INFO] 检查 Java 版本...
java -version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] 未检测到 Java，请安装 JDK 21+
    exit /b 1
)

echo [INFO] Java 已安装 OK
echo.

REM 检查 Maven
echo [INFO] 检查 Maven...
mvn -version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] 未安装 Maven
    echo   请从 https://maven.apache.org/download.cgi 下载
    exit /b 1
)

echo [INFO] Maven 已安装 OK
echo.

REM 切换到脚本所在目录
cd /d "%~dp0"

REM 显示菜单
:menu
echo 请选择要运行的示例：
echo   1. BasicExample - 基础示例
echo   2. AnnotationExample - 注解示例
echo   3. GenericExample - 泛型示例
echo   4. JSONPathExample - JSONPath 示例
echo   5. JSONPathRealWorldExample - JSONPath 实际应用
echo   6. PerformanceExample - 性能示例
echo   7. HighPerformanceExample - 高性能示例
echo   a. 运行所有示例
echo   b. 仅编译
echo   q. 退出
echo.

set /p choice=选择:

if "%choice%"=="1" goto example1
if "%choice%"=="2" goto example2
if "%choice%"=="3" goto example3
if "%choice%"=="4" goto example4
if "%choice%"=="5" goto example5
if "%choice%"=="6" goto example6
if "%choice%"=="7" goto example7
if /i "%choice%"=="a" goto run_all
if /i "%choice%"=="b" goto compile_only
if /i "%choice%"=="q" goto quit

echo [ERROR] 无效选择: %choice%
goto menu

:compile_only
echo [INFO] 编译示例代码...
call mvn clean compile
if errorlevel 1 goto error
echo [INFO] 编译完成
goto menu

:example1
call :compile
call :run com.alibaba.fastjson3.samples.basic.BasicExample
goto menu

:example2
call :compile
call :run com.alibaba.fastjson3.samples.basic.AnnotationExample
goto menu

:example3
call :compile
call :run com.alibaba.fastjson3.samples.basic.GenericExample
goto menu

:example4
call :compile
call :run com.alibaba.fastjson3.samples.jsonpath.JSONPathExample
goto menu

:example5
call :compile
call :run com.alibaba.fastjson3.samples.jsonpath.JSONPathRealWorldExample
goto menu

:example6
call :compile
call :run com.alibaba.fastjson3.samples.performance.PerformanceExample
goto menu

:example7
call :compile
call :run com.alibaba.fastjson3.samples.performance.HighPerformanceExample
goto menu

:run_all
echo [INFO] 运行所有示例...
echo.
call :run com.alibaba.fastjson3.samples.basic.BasicExample
echo.
call :run com.alibaba.fastjson3.samples.basic.AnnotationExample
echo.
call :run com.alibaba.fastjson3.samples.basic.GenericExample
echo.
call :run com.alibaba.fastjson3.samples.jsonpath.JSONPathExample
echo.
call :run com.alibaba.fastjson3.samples.jsonpath.JSONPathRealWorldExample
echo.
call :run com.alibaba.fastjson3.samples.performance.PerformanceExample
echo.
call :run com.alibaba.fastjson3.samples.performance.HighPerformanceExample
echo.
echo [INFO] 所有示例运行完成！
goto menu

:compile
echo [INFO] 编译...
call mvn clean compile
if errorlevel 1 goto error
echo [INFO] 编译完成
goto :eof

:run
echo [INFO] 运行 %~2...
call mvn exec:java -Dexec.mainClass="%~2"
if errorlevel 1 goto error
echo.
goto :eof

:error
echo [ERROR] 运行失败
goto menu

:quit
echo [INFO] 退出
goto :eof
