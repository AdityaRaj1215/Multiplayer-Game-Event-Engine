# Multiplayer Game Event Engine – Real-Time State Sync

A production-grade distributed backend engine for multiplayer games where players send actions (move, shoot, jump) and the server broadcasts authoritative real-time game state updates.

## 🎮 Overview

This system processes player events through Kafka, stores game state in Redis, and pushes updates to clients via WebSockets. The architecture is designed to be scalable, partitioned by game rooms, fault-tolerant, and supports replay/snapshot mechanics.

## 🏗️ Architecture

The system consists of three main services:

1. **gateway-service**: WebSocket gateway accepting player connections and forwarding state updates
2. **engine-service**: Core game logic engine processing events and maintaining authoritative state
3. **simulator-service**: CLI tool for load testing with fake player events

### Technology Stack

- **Java 17**: Core language
- **Spring Boot 3**: Application framework
- **Kafka**: Event streaming platform
- **Redis**: Fast game state storage
- **Docker**: Containerization
- **Kubernetes**: Orchestration
- **WebSocket (STOMP)**: Real-time client communication

## 📁 Project Structure

```
multiplayer-game/
├── gateway-service/      # WebSocket gateway service
│   ├── src/main/java/
│   ├── src/main/resources/
│   ├── Dockerfile
│   └── k8s/
├── engine-service/       # Game logic engine service
│   ├── src/main/java/
│   ├── src/main/resources/
│   ├── Dockerfile
│   └── k8s/
├── simulator-service/    # Load testing simulator
│   ├── src/main/java/
│   ├── src/main/resources/
│   └── Dockerfile
└── infra/                # Infrastructure configuration
    ├── docker-compose.yml
    └── k8s/             # Kubernetes manifests
        ├── redis.yaml
        ├── zookeeper.yaml
        ├── kafka.yaml
        ├── gateway.yaml
        ├── engine.yaml
        └── hpa.yaml
```

## 🔄 Event Flow

```
Client → Gateway → Kafka (player-events) → Engine → Redis → Kafka (game-state-updates) → Gateway → Client
```

1. **Player Action**: Client sends action via WebSocket to gateway-service
2. **Event Publishing**: Gateway publishes event to Kafka topic `player-events` (partitioned by roomId)
3. **Event Processing**: engine-service consumes events and applies deterministic game logic
4. **State Storage**: Updated game state stored in Redis under `room:<roomId>`
5. **State Update**: Engine publishes state update to Kafka topic `game-state-updates`
6. **Broadcast**: Gateway consumes state updates and broadcasts to connected clients in the room

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Maven 3.6+
- Docker & Docker Compose
- Kubernetes cluster (optional, for K8s deployment)

### Running with Docker Compose

See detailed instructions in the [infra/README.md](infra/README.md) once created.

### Building Services

```bash
# Build all services
cd gateway-service && mvn clean install
cd ../engine-service && mvn clean install
cd ../simulator-service && mvn clean install
```

## 📊 Core Data Models

### PlayerEvent
- `playerId`: Unique player identifier
- `roomId`: Game room identifier (Kafka partition key)
- `actionType`: MOVE, SHOOT, or JUMP
- `position`: Current player position (x, y)
- `velocity`: Player velocity vector (vx, vy)
- `timestamp`: Event timestamp

### GameState
- `roomId`: Game room identifier
- `players`: Map of players in the room
- `bullets`: List of active bullets/projectiles
- `timestamp`: State timestamp
- `version`: State version for optimistic locking
- `worldWidth` / `worldHeight`: Game world boundaries

### Player
- `playerId`: Unique identifier
- `position`: Current position
- `velocity`: Current velocity
- `health`: Health points (0-100)
- `lastActionTimestamp`: Last action time
- `lastAction`: Last performed action

### StateUpdate
- `roomId`: Target room
- `gameState`: Full or partial game state
- `diff`: Differential update (optional)
- `timestamp`: Update timestamp
- `isFullUpdate`: Whether this is a full state or diff

## 🎯 Features

- ✅ Real-time WebSocket communication (STOMP)
- ✅ Kafka-based event streaming with partitioning
- ✅ Redis-based fast state storage
- ✅ Deterministic game logic engine
- ✅ Fault tolerance with DLQ (Dead Letter Queue)
- ✅ Horizontal scaling support
- ✅ Health checks and metrics (Spring Actuator + Prometheus)
- ✅ Kubernetes-ready with HPA

## 📝 TODO

This is Phase 1 - Project Foundation. Upcoming phases:

- Phase 2: Engine Service Implementation
- Phase 3: Gateway Service Implementation
- Phase 4: Simulator Service Implementation
- Phase 5: Infrastructure Configuration
- Phase 6: Testing
- Phase 7: Complete Documentation

## 📄 License

This project is part of a production-grade system implementation.

