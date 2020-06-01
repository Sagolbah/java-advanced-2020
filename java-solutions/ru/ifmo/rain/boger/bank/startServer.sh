#!/bin/bash
export CLASSPATH=..

rmiregistry &
java Server
