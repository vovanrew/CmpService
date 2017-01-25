#!/usr/bin/env bash
pwd=`pwd`
pwd+="/test_compressed_wrong_format.txt"

curl -X POST -H 'Content-Type: application/json' http://localhost:9000/decompress -d '{"inputFile": "'"$pwd"'"}'
echo
