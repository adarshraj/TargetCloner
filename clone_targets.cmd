@echo off
set JRE_PATH=C:\PortableApps\Java\jdk\jdk-21
set EXECUTABLE_JAR=.\targetcloner-jar-with-dependencies.jar
set PROGRAM_ARGS=

"%JRE_PATH%\bin\java.exe" -jar "%EXECUTABLE_JAR%" %PROGRAM_ARGS%

echo Execution completed. Press any key to close...
pause > nul