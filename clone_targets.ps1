$JRE_PATH = "C:\PortableApps\Java\jdk\jdk-21"
$EXECUTABLE_JAR = ".\targetcloner-jar-with-dependencies.jar"
$PROGRAM_ARGS = # add or remove arguments as per your needs

#Start-Transcript -path .\console_output.log -append

& "$JRE_PATH\bin\java.exe" -jar $EXECUTABLE_JAR $PROGRAM_ARGS

#Stop-Transcript

Write-Host "Execution completed. Press any key to close..."
$null = $host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")