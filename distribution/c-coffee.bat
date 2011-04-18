@echo off
rem
rem Define program home is not already defined
rem
if not defined CLOUDCOFFEE_HOME set "CLOUDCOFFEE_HOME=%cd%"
set "CLOUDCOFFEE_LIB=%CLOUDCOFFEE_HOME%\c-coffee.jar" 

rem
rem Check if the application library exists, otherwise exist
rem 
if exist "%CLOUDCOFFEE_LIB%" goto update
echo Cannot find "%CLOUDCOFFEE_LIB%"
echo This file is needed to run this program
goto end

:update

rem 
rem Check if an uodate has been downloaded 
rem 
if not exist "%CLOUDCOFFEE_LIB%.new" goto ok
echo Installing update 
del "%CLOUDCOFFEE_LIB%"
move "%CLOUDCOFFEE_LIB%.new" "%CLOUDCOFFEE_LIB%"

:ok
rem
rem Get remaining unshifted command line arguments and save them in the
rem
set CMD_LINE_ARGS=
:setArgs
if ""%1""=="""" goto doneSetArgs
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
shift
goto setArgs
:doneSetArgs

rem 
rem Invoke the application client
rem 
call java -jar "%CLOUDCOFFEE_LIB%" %CMD_LINE_ARGS%

:end