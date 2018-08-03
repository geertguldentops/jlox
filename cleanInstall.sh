#!/bin/bash

function setMavenOpts() {
	export MAVEN_OPTS='-Xms4096m -Xmx4096m -XX:+CMSClassUnloadingEnabled'
}

function doCleanInstall() {
	mvn clean install
}

setMavenOpts

doCleanInstall