package com.jonatasrocha;

import java.nio.ByteBuffer;

public class FrameEncoder {

    public FrameEncoder() {}

    public ByteBuffer encode(Frame frame) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES + frame.getBodySize());
        buffer.putInt(Integer.BYTES + frame.getBodySize());
        buffer.putShort(frame.opcode());
        buffer.putShort(frame.version());
        buffer.put(frame.body());
        return buffer;
    }

}
