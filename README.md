# PNG Message Reader

Client-server project for encoding/decoding messages inside PNG files

Created as an assignment for Network I, course of Computer Science from the Universidade Estadual de Santa Cruz - Bahia, Brazil

## Protocol

- The type of the private chunk is "reDe"
- The first byte of the private chunk defines the encryption details:
  - The first 3 bits represents the encryption used - CAESAR(0bXXXXX111) | XOR(0bXXXXX000)
  - If the encryption uses the CAESAR cipher, then the last 5 bits represents the offset used to encrypt the message
- The following bytes of the chunk represents the encrypted message
- After establishing a connection, the Client waits for a "ready" message to begin sending the file
  - The server looks for a hidden message by searching for a chunk with the previous cited chunk type
  - If there is any hidden message it is decrypted and sent back to the Client
- The Server sends a "bye" message to end the communication

## TODO List

- [x] Finish PNG decoder
- [x] Finish `Server` side socket communication
- [x] Finish `Client` side socket communication
- [x] Define a `Client` <-> `Server` application protocol
- [x] Implement the protocol on both ends
- [x] Add host option to client CLI
- [x] Fix Broken Pipe when server reaches maximum connections
- [ ] Finish docs

## Limitations

- Encryption only works on ASCII characters
- It only finds 1 hidden message, no order guaranteed
- Hardcoded new image name [ORIGINAL_NAME_(*ENCRYPTION_NAME*).PNG]
