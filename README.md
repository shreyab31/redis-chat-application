# Redis Chat Application

A Spring Boot REST API for managing real-time chat rooms using Redis.

## Architecture
- **Spring Boot 4.1.1**: Backend framework.
- **Redis**: Used for state management and real-time broadcasting.
    - **Hashes**: Store room metadata (`{room:<name>}:meta`).
    - **Sets**: Manage participant membership (`{room:<name>}:members`).
    - **Lists**: Store chronological message history (`{room:<name>}:messages`).
    - **Pub/Sub**: Broadcasts new messages to participants (`room:<name>:events`).
- **Cluster Compatibility**: Uses Redis Hash Tags (`{}`) to ensure all keys for a specific room reside on the same shard.

## API Endpoints

### 1. Create Chat Room
- **Endpoint**: `POST /api/chatapp/chatrooms`
- **Request**: `{"roomName": "string"}` (3-50 characters)
- **Responses**:
    - `201 Created`: `{"message": "...", "roomId": "...", "status": "success"}`
    - `409 Conflict`: `{"message": "Room already exists", "roomId": "...", "status": "error"}`
    - `400 Bad Request`: Validation error response.

### 2. Join Chat Room
- **Endpoint**: `POST /api/chatapp/chatrooms/{roomId}/join`
- **Request**: `{"participant": "string"}`
- **Responses**:
    - `200 OK`: `{"message": "...", "roomId": "...", "status": "success"}`
    - `404 Not Found`: `{"message": "Room not found", "roomId": "...", "status": "error"}`
    - `400 Bad Request`: Validation error response.

### 3. Send Message
- **Endpoint**: `POST /api/chatapp/chatrooms/{roomId}/messages`
- **Request**: `{"participant": "string", "message": "string"}`
- **Responses**:
    - `200 OK`: `{"message": "Message sent successfully.", "status": "success"}`
    - `404 Not Found`: `{"message": "Room not found", "status": "error"}`
    - `400 Bad Request`: Validation error response.

### 4. Retrieve Message History
- **Endpoint**: `GET /api/chatapp/chatrooms/{roomId}/messages`
- **Request Param**: `limit` (1-100, default 10)
- **Responses**:
    - `200 OK`: `{"messages": [ { "participant": "...", "message": "...", "timestamp": "..." }, ... ]}`
    - `404 Not Found`: Room does not exist.
    - `400 Bad Request`: Invalid limit.

## Setup & Configuration

### Environment Variables
The application requires a Redis instance. Configure the following environment variables:
- `REDIS_HOST`: Hostname of the Redis server.
- `REDIS_PORT`: Port of the Redis server.
- `REDIS_PASSWORD`: Password for Redis authentication.
- `REDIS_SSL_ENABLED`: `true` or `false` (defaults to `false`).

### Running the Application
```bash
./mvnw spring-boot:run
```

### Running Tests
```bash
./mvnw test
```
*Note: `PubSubIntegrationTest` requires a live Redis instance with the environment variables configured above to pass.*

## Testing with Postman
1. Import the provided Postman collection.
2. Set the environment variables in Postman to match your API host.
3. Execute requests in order: Create Room $\rightarrow$ Join Room $\rightarrow$ Send Message $\rightarrow$ Retrieve History.
