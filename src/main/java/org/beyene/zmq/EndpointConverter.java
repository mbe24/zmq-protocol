package org.beyene.zmq;

import picocli.CommandLine;

import java.util.regex.Pattern;

public class EndpointConverter implements CommandLine.ITypeConverter<String> {

    private final Pattern pattern = Pattern.compile("tcp://.+:(\\d+)");

    @Override
    public String convert(String value) throws Exception {
        if (pattern.matcher(value).matches())
            return value;
        else {
            String message = "Format needs to be tcp://<host>:<port>. Parameter does not match: %s";
            throw new Exception(String.format(message, value));
        }
    }
}
