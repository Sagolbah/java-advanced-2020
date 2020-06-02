#!/bin/bash

cd "../../../../.." || exit

rmiregistry &
java ru.ifmo.rain.boger.bank.Server