#!/usr/bin/env bash
pwd=`pwd`
pwd+="/test.txt"

curl -X POST -H 'Content-Type: application/json' http://localhost:9000/compress -d '{"inputFile": "'"$pwd"'"}'
echo 
