# ActorCompressor

This is a practice of implementing web RESTful microservice with AKKA, this work is modified from HumbleLu example (https://github.com/HumbleLu/scala-challenge.git).

####Main Changes:
- compressor and decompressor services were transformed into actor model instead of class model
- algorithm modified (program can compress and decompress all file content, not only one line as it was before)

The compressing algorithm is based on the count of consecutive characters, for example: aaaabbc => a4-b2-c1 The algorithm is implemented in /src/main/scala/compressor, the compression related package

## Usage:

Start service

```
$ sbt
> ~re-start
```

Sending request (with specifying the file path)

```
curl -X POST -H 'Content-Type: application/json' http://localhost:9000/compress -d '{"inputFile": "text_file.txt"}'
```

or

```
curl -X POST -H 'Content-Type: application/json' http://localhost:9000/decompress -d '{"inputFile": "text_file.txt"}'
```

The compressed/decompressed file can be found in the same directory of the inputFile

### Run examples
```
cd example/
```

####Compress file
```
./post-compress.sh
200 OK
filename: ~/example/test.txt.cmps
```

####Decompress file
```
./post-decompress.sh
200 OK
filename: ~/example/test_compressed.txt.dcmps
```

####Decompress file with wrong format
```
./post-decompress.sh
500 Internal Server Error
Format not match! Empty file: ~/example/test_compressed_wrong_format.txt.dcmps generated.
```
