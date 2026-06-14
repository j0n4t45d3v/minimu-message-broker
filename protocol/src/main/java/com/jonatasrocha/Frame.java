package com.jonatasrocha;

import java.nio.ByteBuffer;

public record Frame(short opcode, short version, ByteBuffer body) {

}