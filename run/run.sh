#!/bin/bash

echo "====================================="
echo "Run filter:"
echo "====================================="

cat search.json


echo "====================================="
echo "Start processing"
echo "====================================="

java -jar big-brother-app-*.jar

echo "====================================="
echo "Finished"
echo "====================================="
