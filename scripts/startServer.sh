#!/bin/bash

cd "../java-solutions" || exit

rmiregistry & 

sleep 1

java ru.ifmo.rain.boger.bank.server.Server

killall rmiregistry