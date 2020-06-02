#!/bin/bash

cd "../../../../.." || exit

rmiregistry &

sleep 1 

java ru.ifmo.rain.boger.bank.Server