# PNG Message Reader

Client-server project for encoding/decoding messages inside PNG files

Created as an assignment for Network I, course of Computer Science from the Universidade Estadual de Santa Cruz - Bahia, Brazil

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
