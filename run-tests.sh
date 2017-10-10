#!/bin/sh
cd `dirname $0`
SCRIPT_DIR=`pwd`

DATABASE_NAME=irida_test
DATABASE_USER=test
DATABASE_PASSWORD=test
JDBC_URL=jdbc:mysql://localhost:3306/$DATABASE_NAME
TMP_DIRECTORY=/tmp
GALAXY_DOCKER=phacnml/galaxy-irida-17.01:0.17.0-it
GALAXY_DOCKER_NAME=irida-galaxy-test
GALAXY_PORT=48889
GALAXY_URL=http://localhost:$GALAXY_PORT
GALAXY_INVALID_URL=http://localhost:48890
GALAXY_INVALID_URL2=http://localhost:48891

DO_CLEANUP=0

check_dependencies() {
	mvn --version 1>/dev/null 2>/dev/null
	if [ $? -ne 0 ];
	then
		echo "Command 'mvn' does not exist.  Please install Maven (e.g., 'apt-get install maven') to continue."
		exit 1
	fi

	xvfb-run -h 1>/dev/null 2>/dev/null
	if [ $? -ne 0 ];
	then
		echo "Command 'xvfb-run' does not exist.  Please install xvfb (e.g., 'apt-get install xvfb') to continue."
		exit 1
	fi

	docker --version 1>/dev/null 2>/dev/null
	if [ $? -ne 0 ];
	then
		echo "Command 'docker' does not exist.  Please install Docker (e.g., 'curl -sSL https://get.docker.com/ | sh') to continue."
		exit 1
	fi

	mysql --version 1>/dev/null 2>/dev/null
	if [ $? -ne 0 ];
	then
		echo "Command 'mysql' does not exist.  Please install MySQL/MariaDB (e.g., 'apt-get install mariadb-client mariadb-server') to continue."
		exit 1
	fi
}

clean_database_docker() {
	set -x
	echo "drop database $DATABASE_NAME; create database $DATABASE_NAME;" | mysql -u$DATABASE_USER -p$DATABASE_PASSWORD
	mysql -u$DATABASE_USER -p$DATABASE_PASSWORD $DATABASE_NAME < $SCRIPT_DIR/ci/irida_latest.sql
	if [ "$(docker ps | grep $GALAXY_DOCKER_NAME)" != "" ];
	then
		docker rm -f -v $GALAXY_DOCKER_NAME
	fi
}

test_unit() {
	set -x
	if [ $DO_CLEANUP -ne 0 ]; then clean_database_docker; fi
	mvn clean test $@
}

test_service() {
	set -x
	if [ $DO_CLEANUP -ne 0 ]; then clean_database_docker; fi
	mvn clean verify -B -Pservice_testing -Djdbc.url=$JDBC_URL -Dirida.it.rootdirectory=$TMP_DIRECTORY $@
}

test_rest() {
	set -x
	if [ $DO_CLEANUP -ne 0 ]; then clean_database_docker; fi
	mvn clean verify -Prest_testing -B $@
}

test_ui() {
	set -x
	if [ $DO_CLEANUP -ne 0 ]; then clean_database_docker; fi
	xvfb-run --auto-servernum --server-num=1 mvn clean verify -B -Pui_testing -Dwebdriver.chrome.driver=./src/main/webapp/node_modules/chromedriver/lib/chromedriver/chromedriver -Dirida.it.nosandbox=true -Djdbc.url=$JDBC_URL -Dirida.it.rootdirectory=$TMP_DIRECTORY $@
}

test_galaxy() {
	set -x
	if [ $DO_CLEANUP -ne 0 ]; then clean_database_docker; fi
	docker run -d -p $GALAXY_PORT:80 --name $GALAXY_DOCKER_NAME -v $TMP_DIRECTORY:$TMP_DIRECTORY -v $SCRIPT_DIR:$SCRIPT_DIR $GALAXY_DOCKER
	mvn clean verify -B -Pgalaxy_testing -Djdbc.url=$JDBC_URL -Dirida.it.rootdirectory=$TMP_DIRECTORY -Dtest.galaxy.url=$GALAXY_URL -Dtest.galaxy.invalid.url=$GALAXY_INVALID_URL -Dtest.galaxy.invalid.url2=$GALAXY_INVALID_URL2 $@
	docker rm -f -v $GALAXY_DOCKER_NAME
}

############
### MAIN ###
############

if [ $# -eq 0 ];
then
	echo -e "Usage: $0 [-c|--clean-database-docker] [test_type] [Maven options]"
	echo -e "Options:"
	echo -e "\t-c|--clean-database-docker: Clean out test database ($JDBC_URL) and previous Docker containers ($GALAXY_DOCKER_NAME) before running tests."
	echo -e "\t[test_type]:                One of the IRIDA test types {service_testing, ui_testing, rest_testing, galaxy_testing, unit, all}."
	echo -e "\t[Maven options]:            Additional options to pass to 'mvn'.  In particular, can pass '-Dtest.it=ca.corefacility.bioinformatics.irida.fully.qualified.name' to run tests from a particular class.\n"
	echo -e "Example:\n"
	echo -e "$0 -c service_testing"
	echo -e "\tThis will test the Service layer of IRIDA, cleaning up the test database/docker containers first.\n"
	echo -e "$0 -c galaxy_testing"
	echo -e "\tRuns the Galaxy integration tests for IRIDA"
	echo -e "\tThis will also attempt to launch Galaxy Docker on $GALAXY_URL\n"
	echo -e "$0 rest_testing -Dtest.it=ca.corefacility.bioinformatics.irida.web.controller.test.integration.analysis.RESTAnalysisSubmissionControllerIT"
	echo -e "\tThis will run IRIDA REST API tests found in the class 'RESTAnalysisSubmissionControllerIT'. This will *not* clean up the previous tests database files."
	exit 0
fi

check_dependencies

cd $SCRIPT_DIR

if [ "$1" = "--clean-database-docker" -o "$1" = "-c" ];
then
	DO_CLEANUP=1
	shift
fi

case "$1" in
	service_testing)
		shift
		test_service $@
	;;
	ui_testing)
		shift
		test_ui $@
	;;
	rest_testing)
		shift
		test_rest $@
	;;
	galaxy_testing)
		shift
		test_galaxy $@
	;;
	unit)
		shift
		test_unit $@
	;;
	all)
		shift
		test_unit
		test_service $@
		test_ui $@
		test_rest $@
		test_galaxy $@
	;;
	*)
		echo "Unrecogized test [$1]"
		exit 1
	;;
esac
