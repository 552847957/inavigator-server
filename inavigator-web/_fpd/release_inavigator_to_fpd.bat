echo on
call setpath.bat
call "%ANT_HOME%\bin\ant" -f release-inavigator-to-fpd.xml

cd "..\output\fpd\distrib\syncserver"
echo %cd%
call "C:\Program Files (x86)\WinRAR\winrar.exe" a -r syncserver.rar
cd %~dp0/
copy ..\output\fpd\distrib\syncserver\syncserver.rar ..\output\fpd\distrib\sources\
rmdir /s /q ..\output\fpd\distrib\syncserver
