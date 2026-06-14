package com.jonatasrocha;

public interface OpcodeStatusCode {
    public static final short SUCCESS = 0;
    public static final short FAILED = 0X01;
    public static final short INVALID_VERSION = 0X02;
    public static final short INVALID_OPCODE = 0X03;
    public static final short INVALID_STATE = 0X04;
    public static final short NOT_ALLOWED_OPCODE = 0X05;
    public static final short FAIL_HANDSHAKE = 0X06;
}
