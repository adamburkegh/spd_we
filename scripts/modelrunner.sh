set -e

# Default to 3g Memory
if [[ -z ${JMEM+epsilon} ]]; then set JMEM=3g; fi

LDP=lib/lp_solve_5.5_java/lib/ux64
JOPTS=-"Xmx${JMEM} -Xms${JMEM}"
CONFIG_PARAMS="-Djava.library.path=$LDP -Dlog4j.configurationFile=config/static/log4j2.xml"

mkdir -p var

${JAVA_HOME}/bin/java ${CONFIG_PARAMS} ${JOPTS} -jar lib/spndiscover.jar
