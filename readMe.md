# Communication Application - Java

## Overview

This Java application enables communication between a client and a server. The project is structured into three modules:

1. **Model** - Contains common classes shared between the client and server.
2. **Server** - The server-side application.
3. **Client** - The client-side application.

Both the client and server applications include a graphical user interface.

## Why This Project?

This project is my final practical assignment for the Java courses at Ynov Toulouse. It demonstrates my ability to build a complete Java application, integrating networking, encryption, multithreading, and event-driven programming.

## Features

- Uses sockets for communication.
- Messages are encrypted using AES keys, with a framework designed to support asymmetric encryption.
- Connection and message listening run on separate threads to prevent execution blocking.
- Detects client/server disconnection without forcefully closing the connection.
- Uses an event-driven system:
    - Events are emitted when a socket is connected or disconnected.
    - Events are also triggered when a message or configuration message is received.

## Dependencies

- **Java Version**: 21
- **Libraries** (located in the `lib` directory, install only if missing):
    - `jackson-core-2.9.9.jar`
    - `jackson-databind-2.9.9.3.jar`
    - `jackson-annotations-2.18.3.jar`

## Project Structure

```
.
├── lib/                        # External libraries (Jackson)
│   ├── jackson-annotations-2.18.3.jar
│   ├── jackson-core-2.9.9.jar
│   └── jackson-databind-2.9.9.3.jar
├── readMe.md                   # Project documentation
├── vpnClient/
│   ├── src/
│   │   ├── fr/ynov/vpnClient/
│   │   │   ├── gui/             # Client GUI components
│   │   │   ├── model/           # Client socket and event handling
│   │   │   └── utils/           # Utility functions
│   │   └── Main.java            # Client main entry point
├── vpnModel/
│   ├── src/
│   │   ├── fr/ynov/vpnModel/
│   │   │   ├── gui/             # Shared GUI components
│   │   │   └── model/           # Common models (messages, encryption, config)
│   │   └── Main.java            # Shared main entry point (if applicable)
└── vpnServer/
    └── src/
        ├── fr/ynov/vpnServer/
        │   ├── gui/             # Server GUI components
        │   └── model/           # Server socket and event handling
        └── Main.java            # Server main entry point
```


## Installation

1. Ensure Java 21 is installed.

2. Download or clone the repository.

3. Check if the required dependencies are in the `lib` directory. If missing, download them manually.

4. Open the project in **IntelliJ IDEA** (or compile it using command line).

5. Compile the project inside IntelliJ or using:

    - Compile the client:

   ```sh
   javac -cp "lib/*" -d out/client vpnModel/**/*.java vpnClient/**/*.java 
   ```

    - Compile the server:

   ```sh
   javac -cp "lib/*" -d out/server vpnModel/**/*.java vpnServer/**/*.java 
   ```

6. Create executable JAR files:
    - Jar for the client: 
   ```sh
   jar cfe out/jar/vpnClient.jar Main -C out/client .
   ```
    - Jar for the server: 
   ```sh
   jar cfe out/jar/vpnClient.jar Main -C out/server .
   ```
7. Clean up build files:
   ```sh
   rm -rf out/client out/server
   ```
## Usage

After building the jar, you should run the next command to:

### Run the Server

```sh
  java -jar out/jar/vpnServer.jar
```

### Run the Client

```sh
  java -jar out/jar/vpnClient.jar
```

## License

This project is licensed under the MIT License.

## Contributors

Feel free to contribute to the project by submitting issues and pull requests.

