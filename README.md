# Socket IO Server implementation in Java with Spring Boot

## Overview
This project implements a **Socket.IO server** using **Java Spring Boot**. It enables real-time, bi-directional communication between clients and the server, making it ideal for applications that require live updates, such as chat systems, IoT device communication, and real-time notifications.

## Features
- **Socket.IO integration** for real-time messaging
- **Namespace-based communication** for modular event handling
- **JWT-based authentication** for secure connections
- **Session management** with automatic token validation
- **Connection event handling** to monitor client connections
- **Spring Boot & Dependency Injection** for structured service management

## Technologies Used
- **Java 17+**
- **Spring Boot**
- **Socket.IO (corundumstudio)**
- **Netty**
- **JWT Authentication**
- **REST API Integration**
- **Log4j for logging**

## Project Structure
```
com.service.socketio
│── controller
│   ├── SocketIOController.java    # Manages Socket.IO connections and events
│── service
│   ├── SecurityService.java       # Handles token authentication
│   ├── SocketIOService.java       # Fetches namespace data and manages connections
│── Session
│   ├── SessionManager.java        # Manages active sessions and token expiration
│── SpringSocketProjectApplication.java               # Main Spring Boot application
```

## Setup & Installation
### Prerequisites
- Java 17+
- Maven
- Spring Boot

### Steps
1. Clone the repository:
   ```sh
   git clone https://github.com/LucasDiasJorge/Socket-IO
   cd Socket-IO
   ```
2. Build the project:
   ```sh
   mvn clean install
   ```
3. Configure environment variables (or `application.properties`):
   ```properties
   auth.token.secret=your_jwt_secret_key
   namespaces.url=http://your-api-endpoint/licenses
   ```
4. Run the application:
   ```sh
   mvn spring-boot:run
   ```

## Usage
### Connecting to the Socket.IO Server
Clients should connect to:
```
ws://your-server-ip:port/socket-io/{namespace}
```
### Events
- **selfcheckSend** - Used to send messages between clients
- **selfcheckConnection** - Used to notify about connection statuses

## Security
- Uses **JWT authentication** to validate client connections
- Disconnects clients with invalid or expired tokens

## License
This project is licensed under the MIT License.

## Contribution
Contributions are welcome! Feel free to fork this repo and submit a pull request.

## Contact
For any inquiries, please reach out at `lucas_jorg@hotmail.com`

