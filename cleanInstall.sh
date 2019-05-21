#!/bin/bash

function setMavenOpts() {
	export MAVEN_OPTS='-Xms4096m -Xmx4096m -XX:+CMSClassUnloadingEnabled'
}

function downgradeToJDK12() {
	export JAVA_HOME=`/usr/libexec/java_home -v 12`
}

function doCleanInstall() {
	mvn clean install
}

setMavenOpts
downgradeToJDK12

doCleanInstall
