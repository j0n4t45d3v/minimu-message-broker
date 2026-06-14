package com.jonatasrocha;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FrameDecoderTest {

    private static final byte[] PAYLOAD = "Test content".getBytes(StandardCharsets.UTF_8);
    private FrameDecoder frameDecoder;

    @BeforeEach
    void setUp() {
        this.frameDecoder = new FrameDecoder(ByteBuffer.allocateDirect(1024));
    }

    @Test
    void shouldReturnFrameWhenHasACompletedFrame() throws IOException {
        ByteBuffer bytesStream = ByteBuffer.allocate(Long.BYTES + PAYLOAD.length);

        bytesStream.putInt(Integer.BYTES + PAYLOAD.length);
        bytesStream.putShort(Opcodes.HANDSHAKE);
        bytesStream.putShort((short) 1);
        bytesStream.put(PAYLOAD);

        List<Frame> frames = this.frameDecoder.feed(bytesStream);

        assertFalse(frames.isEmpty());
        assertEquals(1, frames.size());
        assertEquals(Opcodes.HANDSHAKE, frames.getFirst().opcode());
        assertEquals(1, frames.getFirst().version());
        assertEquals(PAYLOAD.length, frames.getFirst().body().capacity());
    }

    @Test
    void shouldReturnEmptyWhenNotHasACompletedFrame() throws IOException {
        ByteBuffer bytesStream = ByteBuffer.allocate(Integer.BYTES);

        bytesStream.putInt(Integer.BYTES + PAYLOAD.length);

        List<Frame> frames = this.frameDecoder.feed(bytesStream);

        assertTrue(frames.isEmpty());
    }
    
    @Test
    void shouldReturnEmptyWhenHasPartialFrameSize() throws IOException {
        ByteBuffer bytesStream = ByteBuffer.allocate(Short.BYTES);
        bytesStream.putShort((short) 1);
        List<Frame> frames = this.frameDecoder.feed(bytesStream);
        assertTrue(frames.isEmpty());
    }

    @Test
    void shouldReturnEmptyWhenNotHasBytesToRead() throws IOException {
        ByteBuffer bytesStream = ByteBuffer.allocate(Integer.BYTES);
        List<Frame> frames = this.frameDecoder.feed(bytesStream);
        assertTrue(frames.isEmpty());
    }

    @Test
    void shouldDecodeFrameWithPartialPayload() throws IOException {
        int payloadLength = PAYLOAD.length;
        int firstPayloadPartSize = PAYLOAD.length / 2;
        ByteBuffer partialStream = ByteBuffer.allocate(Long.BYTES + firstPayloadPartSize);

        partialStream.putInt(Integer.BYTES + payloadLength);
        partialStream.putShort(Opcodes.HANDSHAKE);
        partialStream.putShort((short) 1);
        partialStream.put(sliceArray(PAYLOAD, 0, firstPayloadPartSize));

        List<Frame> frames = this.frameDecoder.feed(partialStream);

        assertTrue(frames.isEmpty());

        ByteBuffer restPayload = ByteBuffer.allocate(payloadLength - firstPayloadPartSize);
        restPayload.put(sliceArray(PAYLOAD, firstPayloadPartSize, payloadLength - firstPayloadPartSize));

        frames = this.frameDecoder.feed(partialStream);

        assertFalse(frames.isEmpty());
        assertEquals(1, frames.size());
        assertEquals(Opcodes.HANDSHAKE, frames.getFirst().opcode());
        assertEquals(1, frames.getFirst().version());
        assertEquals(payloadLength, frames.getFirst().body().capacity());
    }

    private byte[] sliceArray(byte[] payload, int offset, int length) {
        byte[] sliced = new byte[length]; 
        System.arraycopy(payload, offset, sliced, 0, length);
        return sliced;
    }

    @Test
    void shouldDecodeFrameWithPartialHeader() throws IOException {
        ByteBuffer partialHeader = ByteBuffer.allocate(Integer.BYTES + Short.BYTES);

        partialHeader.putInt(Integer.BYTES + PAYLOAD.length);
        partialHeader.putShort(Opcodes.HANDSHAKE);

        List<Frame> frames = this.frameDecoder.feed(partialHeader);
        assertTrue(frames.isEmpty());

        ByteBuffer restHeader = ByteBuffer.allocate(Short.BYTES + PAYLOAD.length);
        restHeader.putShort((short) 1);
        restHeader.put(PAYLOAD);

        frames = this.frameDecoder.feed(restHeader);

        assertFalse(frames.isEmpty());
        assertEquals(1, frames.size());
        assertEquals(Opcodes.HANDSHAKE, frames.getFirst().opcode());
        assertEquals(1, frames.getFirst().version());
        assertEquals(PAYLOAD.length, frames.getFirst().body().capacity());
    }

    @Test
    void shouldReturnManyFramesWhenHasMoreOneCompletedFrameInSameStream() throws IOException {
        int capacity = Long.BYTES + PAYLOAD.length;
        ByteBuffer bytesStream = ByteBuffer.allocate(2 * capacity);

        bytesStream.putInt(Integer.BYTES + PAYLOAD.length);
        bytesStream.putShort(Opcodes.HANDSHAKE);
        bytesStream.putShort((short) 1);
        bytesStream.put(PAYLOAD);

        bytesStream.putInt(Integer.BYTES + PAYLOAD.length);
        bytesStream.putShort(Opcodes.HANDSHAKE);
        bytesStream.putShort((short) 1);
        bytesStream.put(PAYLOAD);

        List<Frame> frames = this.frameDecoder.feed(bytesStream);

        assertFalse(frames.isEmpty());
        assertEquals(2, frames.size());
        for (Frame frame : frames) {
            assertEquals(Opcodes.HANDSHAKE, frame.opcode());
            assertEquals(1, frame.version());
            assertEquals(PAYLOAD.length, frame.body().capacity());
        }
    }

    @Test
    void shouldReturnOneFrameWhenHasOneCompletedFrameAndPartialFrame() throws IOException {
        ByteBuffer bytesStream = ByteBuffer.allocate(Long.BYTES + PAYLOAD.length + Integer.BYTES + Short.BYTES);

        bytesStream.putInt(Integer.BYTES + PAYLOAD.length);
        bytesStream.putShort(Opcodes.HANDSHAKE);
        bytesStream.putShort((short) 1);
        bytesStream.put(PAYLOAD);

        bytesStream.putInt(Integer.BYTES + PAYLOAD.length);
        bytesStream.putShort(Opcodes.HANDSHAKE);

        List<Frame> frames = this.frameDecoder.feed(bytesStream);

        assertFalse(frames.isEmpty());
        assertEquals(1, frames.size());
        assertEquals(Opcodes.HANDSHAKE, frames.getFirst().opcode());
        assertEquals(1, frames.getFirst().version());
        assertEquals(PAYLOAD.length, frames.getFirst().body().capacity());
    }

}
