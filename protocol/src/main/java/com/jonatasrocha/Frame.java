package com.jonatasrocha;

import java.nio.ByteBuffer;

public record Frame(short opcode, short version, ByteBuffer body) {

    public static Frame of(short opcode, short version, byte[] body) {
        return new Frame(opcode, version, ByteBuffer.wrap(body));
    }

    public int getBodySize() {
        return body.capacity();
    }

    public int size() {
        return Integer.BYTES + body.capacity();
    }

}