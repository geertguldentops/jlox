#!/bin/bash

function setMavenOpts() {
	export MAVEN_OPTS='-Xms4096m -Xmx4096m'
}

function setJavaHome() {
	export JAVA_HOME=`/usr/libexec/java_home -v 14`
}

function doCleanInstall() {
	mvn clean install
}

setMavenOpts
downgradeToJDK12

doCleanInstall
