@echo off
chcp 65001>nul
java -fullversion > nul 2>nul
if %ERRORLEVEL% GTR 18 (
	echo Не найдена JAVA
	exit
)
for /f ^tokens^=2-3^ delims^=^". %%j in ('java -fullversion 2^>^&1') do set jver=%%j%%k
if %jver% LSS 18 (
	echo Необходима версия java 1.8 и выше
	echo Текущая версия java:
	java -fullversion
	exit
)
echo Запуск программы с командой restore %1

IF "%PROCESSOR_ARCHITECTURE%"=="x86" (set arch=x86) else (set arch=x64)
java -Duser.timezone=GMT+3 -Dfile.encoding=UTF8 -Djava.library.path=lib\native\%arch% -jar "%~dp0\inav-backup.jar" restore %1%
