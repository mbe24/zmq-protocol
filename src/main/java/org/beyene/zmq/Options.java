package org.beyene.zmq;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.ArrayList;
import java.util.List;

@Command(name = "java -jar zmq-protocol.jar", mixinStandardHelpOptions = true, version = "zmq-protocol 1.0-SNAPSHOT")
public class Options {

    @Option(names = {"-e", "--endpoint"}, required = true, arity = "1", split = ",", description = "Define ZMQ endpoints. " +
            "Only the tcp protocol is supported.", converter = EndpointConverter.class)
    List<String> endpoints = new ArrayList<>();
}
