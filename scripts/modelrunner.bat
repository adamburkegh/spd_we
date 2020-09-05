

set LDP=lib\lp_solve_5.5_java\lib\win64

set JMEM=50g

set JOPTS=-Xmx%JMEM% -Xms%JMEM%
set CONFIG_PARAMS=-Djava.library.path=%LDP% -Dlog4j.configurationFile=config\static\log4j.xml

if not exist var mkdir var


REM Main class is au.edu.qut.pm.spn_discover.ModelRunner

"%JAVA_HOME%\bin\java" %CONFIG_PARAMS% %JOPTS% -jar lib\spndiscover.jar > out.log 2>&1


