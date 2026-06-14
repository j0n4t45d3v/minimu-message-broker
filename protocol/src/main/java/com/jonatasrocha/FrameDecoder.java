package com.jonatasrocha;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class FrameDecoder {

    private final ByteBuffer buffer;

    public FrameDecoder(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public List<Frame> feed(ByteBuffer bytes) throws IOException {
        bytes.flip();
        this.buffer.put(bytes);

        List<Frame> frames = new ArrayList<>();

        this.buffer.flip();

        while (hasFrameHeader()) {
            this.buffer.mark();
            int frameSize = buffer.getInt();
            short opcode = buffer.getShort();
            short version = buffer.getShort();
            int expectedPayloadLength = calculatePayloadSize(frameSize);
            if (isPartialPayload(expectedPayloadLength)) {
                this.buffer.reset();
                break;
            }
            byte[] payload = new byte[expectedPayloadLength];
            this.buffer.get(payload);
            frames.add(makeFrame(opcode, version, payload));
        }
        buffer.compact();

        return frames;
    }

    private boolean hasFrameHeader() {
        return buffer.remaining() > Long.BYTES;
    }

    private boolean isPartialPayload(int expectedPayloadLength) {
        return buffer.remaining() < expectedPayloadLength;
    }

    private int calculatePayloadSize(int frameSize) {
        return frameSize - Integer.BYTES;
    }

    private Frame makeFrame(short opcode, short version, byte[] payload) {
        return new Frame(opcode, version, ByteBuffer.wrap(payload));
    }

}
