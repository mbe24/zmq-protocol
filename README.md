# zmq-protocol
POC client/server application based on the Java implementation of ZeroMQ, namely JeroMQ, enhanced with message (de)serialization powered by protobuf.

Further, this POC uses Spring and picocli.

### Usage ###

```bash

./gradlew bootRun --args='--endpoint=tcp://*:5555,tcp://*:5556'

```
