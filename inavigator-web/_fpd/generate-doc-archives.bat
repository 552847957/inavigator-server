call rmdir /s /q doc_generated
mkdir "build/syncserver/output/doc_generated"
call "C:\Program Files (x86)\WinRAR\winrar.exe" a build/syncserver/output/doc_generated/i-NavigatorInstallation.zip build/syncserver/docs/i-NavigatorInstallation.docx -ep build/syncserver/_fpd/drivers/sqljdbc.jar
call "C:\Program Files (x86)\WinRAR\winrar.exe" a build/syncserver/output/doc_generated/i-PassportInstallation.zip build/syncserver/docs/i-PassportInstallation.docx -ep build/syncserver/_fpd/drivers/sqlite-jdbc-3.7.15-M1.jar
call "C:\Program Files (x86)\WinRAR\winrar.exe" a build/syncserver/output/doc_generated/Load_testing.zip build/syncserver/docs/Load_testing.docx -ep build/syncserver/_fpd/tests/apache-jmeter-2.8.zip