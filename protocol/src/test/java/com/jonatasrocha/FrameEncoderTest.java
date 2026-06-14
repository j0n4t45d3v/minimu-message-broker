package com.jonatasrocha;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FrameEncoderTest {

    private static final byte[] PAYLOAD = "Test content".getBytes(StandardCharsets.UTF_8);

    private FrameEncoder frameEncoder;

    @BeforeEach
    void setUp() {
        this.frameEncoder = new FrameEncoder();
    }

    @Test
    void shouldEncodeFrame() {

        Frame frame = Frame.of(Opcodes.PUSH, (short)1, PAYLOAD);
        ByteBuffer frameEncoded = this.frameEncoder.encode(frame);

        assertNotNull(frameEncoded);
        frameEncoded.flip();
        assertEquals(frame.size(), frameEncoded.getInt());
        assertEquals(frame.opcode(), frameEncoded.getShort());
        assertEquals(frame.version(), frameEncoded.getShort());
        assertEquals(frame.getBodySize(), frameEncoded.remaining());
    }

}
