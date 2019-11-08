@echo on >> build_all.log
@set WinRarPath="C:\Program Files (x86)\WinRAR\winrar.exe"
@if not exist Source_Code_All\Source_Code_All.rar (
  @if exist Source_Code_All\Source_Code_All.rar rmdir Source_Code_All /S /Q
  @mkdir Source_Code_All
  @cd sources
  @call %WinRarPath% a ..\Source_Code_All\Source_Code_All.rar syncserver.rar
  @cd ..
  @if exist Source_Code_All\Source_Code_All.rar (
    @echo First complete > build_all.log
  )else (
    @echo First failed > build_all.log
    rmdir Source_Code_All /S /Q
  )
) else (
  @if exist Distrib_All rmdir Distrib_All /S /Q >> build_all.log
  @mkdir Distrib_All >> build_all.log
  @if exist build rmdir build /S /Q  >> build_all.log
  @mkdir build >> build_all.log
  @call %WinRarPath% x Source_Code_All/Source_Code_All.rar build >> build_all.log
  @echo Source_Code_All.rar unpacked to build >> build_all.log
  @cd build
  @mkdir syncserver >> ..\build_all.log
  @call %WinRarPath% x syncserver.rar syncserver >> ..\build_all.log
  @echo syncserver.rar unpacked to syncserver >> ..\build_all.log
  @del /s /q syncserver.rar >> ..\build_all.log
 
  @cd syncserver/source >> ..\build_all.log
  @echo Start building  >> ..\..\..\build_all.log
  @call mvn clean install >> ..\..\..\build_all.log

  @echo Start collecting distrib >> ..\..\..\build_all.log
  @copy generator\target\generator*.zip ..\..\..\Distrib_All >> ..\..\..\build_all.log
  @copy proxyserver\target\proxyserver*.zip ..\..\..\Distrib_All >> ..\..\..\build_all.log
  @copy monitor-alpha\target\monitor-alpha*.zip ..\..\..\Distrib_All >> ..\..\..\build_all.log
  @copy cacheserver\target\syncserver*.zip ..\..\..\Distrib_All >> ..\..\..\build_all.log
  @copy confserver\target\confserver*.zip ..\..\..\Distrib_All >> ..\..\..\build_all.log
  @copy monitor-sigma\target\monitor-sigma*.zip ..\..\..\Distrib_All >> ..\..\..\build_all.log
  @copy utils\admin_tool\target\admin-tool*.zip ..\..\..\Distrib_All >> ..\..\..\build_all.log
  @copy utils\util_backup\target\inav-backup*.zip ..\..\..\Distrib_All >> ..\..\..\build_all.log

  @cd %~dp0
  @echo Start generating docs >> build_all.log
  @call build\syncserver\_fpd\generate-doc-archives.bat >> build_all.log

  @mkdir Distrib_All\docs >> build_all.log
  @copy build\syncserver\output\doc_generated\i-NavigatorInstallation.zip Distrib_All\docs\i-NavigatorInstallation.zip >> build_all.log
  @copy build\syncserver\docs\i-NavigatorApplicationSettings.docx Distrib_All\docs\i-NavigatorApplicationSettings.docx >> build_all.log

  @echo Second complete >> build_all.log
)
