@echo off

:: get orginal directory
set now_dir=%cd%
:: get work directory
set work_dir=%~dp0
:: res dir
set res_dir=res

:: goto work directory
cd /d "%work_dir%"

set /p ratel_version=<%res_dir%\ratel_version.txt
set builder_jar=%res_dir%\%ratel_version%

if exist %builder_jar% (
    echo use %builder_jar%
) else (
    echo can not find container build jar in %work_dir%%builder_jar%
    echo -1
)

:: reset absolute path
set builder_jar=%work_dir%%builder_jar%
set res_dir=%work_dir%%res_dir%

set has_certificate=false
set is_extract_apk_task=false
set is_help=false


:: loop parameters
for /l %%x in (1,1,9) do (
    shift /%%x
    if "%1"=="" (
        goto :params_end
    ) else if /I "%1"=="-c" (
        set has_certificate=true
    ) else if /I "%1"=="--certificate" (
        set has_certificate=true
    ) else if /I "%1"=="-x" (
        set is_extract_apk_task=true
    ) else if /I "%1"=="--extract" (
        set is_extract_apk_task=true
    ) else if /I "%1"=="-h" (
        set is_help=true
    ) else if /I "%1"=="--help" (
        set is_help=true
    )
)
:params_end

:: go back to orginal directory
cd /d "%now_dir%"

::for /F %%i in ('java -jar "%builder_jar%" -t -s -w "%USERPROFILE%\.ratel-working-repkg" -c "%res_dir%\monthly_temp.txt" %*') do ( set out_apk_path=%%i )
if %errorlevel% neq 0 (
  echo call builder jar failed %errorlevel%
  exit /b %errorlevel%
)

if "%is_help%"=="false" (
  echo assemble new apk for %*
)

if "%has_certificate%"=="true" (
  java -jar "%builder_jar%" -s -w "%USERPROFILE%\.ratel-working-repkg" %*
) else (
  if "%is_help%"=="false" (
    echo build with default certificate
  )
  java -jar "%builder_jar%" -s -w "%USERPROFILE%\.ratel-working-repkg" -c "%res_dir%\monthly_temp.txt" %*
)

if %errorlevel% neq 0 (
  echo assemble ratel apk failed
  exit /b %errorlevel%
)

if "%is_extract_apk_task%"=="true" (
  goto :exit0_end
)
if "%is_help%"=="true" (
  goto :exit0_end
)

:exit0_end
exit /b 0
