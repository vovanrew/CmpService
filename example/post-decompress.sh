#!/usr/bin/env bash
pwd=`pwd`
pwd+="/test_compressed.txt"

curl -X POST -H 'Content-Type: application/json' http://localhost:9000/decompress -d '{"inputFile": "'"$pwd"'"}'
echo
