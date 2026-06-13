# Minimus Message Broker 

This project is a basic implementation of an event broker with persistent event storage organized by topics.
I use concorrent programming and Reactor architecture to handle event publishing and consuming, the goal of this project is to build a fast and resilient message broker. 

## Summary
- [Tecnologies](#tecnologies)
- [Concepts](#concepts)
- [Architecture](#architecture)
    - [Project struct](#project-struct)
    - [Protocol](#protocol)
        - [1. Data frame struct](#1-data-frame-struct)
        - [2. Opcodes & Standart](#2-opcodes--standart-response)
            - [2.1 Standart data frame response](#21-standart-data-frame-response)
            - [2.3 Operations reference](#23-operations-reference)

## Tecnologies
- Java 21
- Maven
- Java New I/O (java.nio)
- Slf4j - Logger interface
- Logback - Logger provider
- JUnit5

## Concepts
- Spacer index
- Append-only log
- Concorrent Programming
- Reactor Achitecture
- Multiplexed Connections
- TCP Handshake
- Bufferized I/O operations
- Upgrade connection

## Architecture

## Project struct

this project is organized into the followings modules: 

* **protocol:** Defines how client-server communication works, including opcodes, response status codes and frame encoding and decoding 

* **broker-core:** Implement broker operations defined by the protocol,manages files I/O,
topic storage and connection state. 

* **producer-client:** Producer Client API that abstracts the protocol implementations and 
provides a simple interface for publishing events.

* **consumer-client:** Consumer Client API that abstracts the protocol implementations and 
provide a simple interface for subscribing and consuming events.

```text 
minimus-message-broker
    ├── consumer-client
    │   └── pom.xml
    ├── broker-core
    │   └── pom.xml
    ├── producer-client
    │   └── pom.xml
    ├── protocol
    │   └── pom.xml
    └── pom.xml
```


## Protocol

A binary protocol built on top of the TCP transport protocol, designed to maximize data transfer performance through direct byte serialization, minimizing network overhead, data processing, and conversion.

### 1. Data frame struct
```text
+----------------------------------+
|           frame size             |
|             int32                |
+----------------------------------+
|             HEADER               |
+----------------------------------+
|      opcode    |    version      |
|      int16     |     int16       |
+----------------------------------+
|              BODY                |
+----------------------------------+
```

---

### 2. Opcodes & Standart Response 

### 2.1 Standart data frame response
All Operation response follow this data frame struct
```text
 +----------------------------------+
 |           frame size             |
 |             int32                |
 +----------------------------------+
 |             HEADER               |
 +----------------------------------+
 |      opcode    |    version      |
 |     <opcode>   |       1         |
 +----------------------------------+
 |              BODY                |
 +----------------------------------+
 |             status               |
 |              int16               |
 +----------------------------------+
```
### 2.2 Response status definition 

* **status (int16):**
    * **SUCCESS (int16):** `0`
    * **FAILED (int16):** `1`
    * **INVALID_VERSION (int16):** `2`
    * **INVALID_OPCODE (int16):** `3`
    * **INVALID_STATE (int16):** `4`
    * **NOT_ALLOWED_OPCODE (int16):** `5`
    * **FAIL_HANDSHAKE (int16):** `6`

---

### 2.3 Operations reference

#### 2.3.1 Establish connection: `HANDSHAKE (1)`
Make initial handshake and upgrade connection to consumer or producer 

> **Warning**: this operation is only valid if the connection is not established 

#### Data types
* **HANDSHAKE_TYPE (int8):** `CONSUMER` | `PRODUCER`
* **CONSUMER_CONNECTION (int8):** `1`
* **PRODUCER_CONNECTION (int8):** `2`

#### Request payload (Received Handshake Payload)
```text
+----------------------------------+
|           frame size             |
|             int32                |
+----------------------------------+
|             HEADER               |
+----------------------------------+
|     opcode     |    version      |
|    HANDSHAKE   |       1         |
+----------------------------------+
|              BODY                |
+----------------------------------+
|         HANDSHAKE_TYPE           |
+----------------------------------+
```
*  Response `HANDSHAKE_RESPONSE`, see [Standart Data Frame Response](#21-standart-data-frame-response) 

#### 2.3.2 Subscribe in topic: `SUB (2)`
Subscriber connection to topic given in payload

> **Warning**: this operation only valid if connection is a consumer

- Receive subscribe frame
#### Request payload (Received subscribe payload)
```text
+----------------------------------+
|           frame size             |
|             int32                |
+----------------------------------+
|             HEADER               |
+----------------------------------+
|      opcode    |    version      |
|    SUBSCRIBE   |       1         |
+----------------------------------+
|              BODY                |
+----------------------------------+
|            topic id              |
|             int32                |
+----------------------------------+
```
*  Response `SUBSCRIBE_RESPONSE`, see [Standart Data Frame Response](#21-standart-data-frame-response) 

#### 2.3.3 Publish event in topic: `PUSH (3)`
Publish a new event into topic

> **Warning**: this operation only valid if connection is established and is a `PRODUCER_CONNECTION`, see [handshake operation](#231-establish-connection-op_handshake-1) 

#### Request payload (Received publish event payload)
```text
+----------------------------------------+
|               frame size               |
|                 int32                  |
+----------------------------------------+
|                 HEADER                 |
+----------------------------------------+
|       opcode      |       version      |
|        PUSH       |          1         |
+----------------------------------------+
|                  BODY                  |
+----------------------------------------+
| topic id  | payload length |  payload  |
|  int32    |     int32      |  byte[]   |
+----------------------------------------+
```
*  Response `PUSH_RESPONSE`, see [Standart Data Frame Response](#21-standart-data-frame-response) 

#### 2.3.4 Create topic: `CREATE_TOPIC (4)`
Create a topic

> **Note**: Case already exists topic with same name result ok and not create topic 

> **Warning**: This operation only is valid if the connection is established and is a `PRODUCER_CONNECTION, see [handshake operation](#231-establish-connection-op_handshake-1) 

#### Request payload (Received create topic payload)
```text
+-----------------------------------+
|            frame size             |
|              int32                |
+-----------------------------------+
|              HEADER               |
+-----------------------------------+
|     opcode      |    version      |
| OP_CREATE_TOPIC |       1         |
+-----------------------------------+
|               BODY                |
+-----------------------------------+
|            topic name             |
+-----------------------------------+
|      length    |       name       |
|      int32     |      byte[]      |
+-----------------------------------+
```
*  Response `CREATE_TOPIC_RESPONSE`, see [Standart Data Frame Response](#21-standart-data-frame-response) 

