# 📚 Complete Codebase & Code Flow Explanation

## 🏗️ System Architecture Overview

This is a **distributed multiplayer game event engine** that processes real-time player actions and maintains authoritative game state. The system uses an **event-driven architecture** with Kafka for messaging, Redis for fast state storage, and WebSockets for real-time client communication.

```
┌─────────────┐
│   Client    │  (Browser/Game Client)
└──────┬──────┘
       │ WebSocket (STOMP)
       ▼
┌─────────────────────────────────────┐
│      GATEWAY-SERVICE                │  Port: 8080
│  • WebSocket Handler                │
│  • Kafka Producer (player-events)   │
│  • Kafka Consumer (game-state-updates)│
└──────────────┬──────────────────────┘
               │
               │ Kafka Topic: player-events
               │ (Partitioned by roomId)
               ▼
┌─────────────────────────────────────┐
│      KAFKA BROKER                   │
│  Topics:                            │
│  • player-events (20 partitions)    │
│  • game-state-updates (20 partitions)│
│  • player-events-dlq                │
└──────┬──────────────────┬───────────┘
       │                  │
       │ Consumes         │ Publishes
       ▼                  ▼
┌─────────────────────────────────────┐
│      ENGINE-SERVICE                 │  Port: 8081
│  • GameLogic.apply()                │
│  • Kafka Consumer                   │
│  • Redis State Storage              │
│  • State Update Publisher           │
└──────────────┬──────────────────────┘
               │
               │ Redis Key: room:<roomId>
               ▼
┌─────────────────────────────────────┐
│      REDIS                          │
│  Stores:                            │
│  • GameState (authoritative)        │
│  • TTL for empty rooms              │
└─────────────────────────────────────┘
```

---

## 📦 Project Structure Explained

### 1. **engine-service/** (Core Game Logic)
This is the **heart of the system** - the authoritative game server that processes all player actions and maintains game state.

**Key Components:**
- `GameLogic.java` - Deterministic game engine
- `PlayerEvent`, `GameState`, `Player`, `Bullet`, `StateUpdate` - Data models
- Kafka consumer (to be implemented) - Listens to player events
- Redis integration (to be implemented) - Stores game state

### 2. **gateway-service/** (Client Interface)
This is the **client-facing service** that handles WebSocket connections and routes events between clients and the backend.

**Key Components:**
- WebSocket/STOMP configuration (to be implemented)
- Kafka producer - Publishes player events
- Kafka consumer - Receives state updates to broadcast
- DTOs (`PlayerActionDTO`, `GameStateUpdateDTO`) - Client-facing data structures

### 3. **simulator-service/** (Load Testing)
This is a **testing tool** that generates fake player events to test the system under load.

**Key Components:**
- Event generator (to be implemented)
- Kafka producer - Sends test events

---

## 🔄 Complete Event Flow (Step-by-Step)

### Phase 1: Client Sends Action

```
1. Client (Browser) → WebSocket → Gateway Service
   • Client opens WebSocket connection to gateway-service:8080/ws
   • Subscribes to topic: /topic/room/{roomId}
   • Sends PlayerActionDTO:
     {
       "playerId": "player-123",
       "roomId": "room-abc",
       "actionType": "MOVE",
       "position": {"x": 100, "y": 200},
       "velocity": {"vx": 2.5, "vy": 0}
     }
```

### Phase 2: Gateway Processes & Publishes

```
2. Gateway Service → Kafka Producer
   • Gateway receives PlayerActionDTO via WebSocket
   • Converts to PlayerEvent model
   • Publishes to Kafka topic: "player-events"
   • Partition Key: roomId ("room-abc")
   • Message: PlayerEvent (serialized as JSON)
   
   Why roomId as partition key?
   → Ensures all events for the same room go to the same partition
   → Maintains event ordering per room
   → Allows parallel processing of different rooms
```

### Phase 3: Engine Consumes Event

```
3. Kafka Consumer (Engine Service) → GameLogic
   • Engine service consumer group: "engine-service-group"
   • Consumes batch of PlayerEvent messages
   • For each event:
     a. Load current GameState from Redis (key: "room:room-abc")
     b. Call GameLogic.apply(currentState, playerEvent)
     c. Get updated GameState back
```

### Phase 4: Game Logic Processing

```
4. GameLogic.apply() - Core Deterministic Logic
   
   Input: GameState (current) + PlayerEvent (action)
   Output: GameState (updated)
   
   Process Flow:
   
   A. Validate/Create Player
      • getOrCreatePlayer() - Gets player or creates new one at world center
   
   B. Process Action Type:
      
      MOVE:
      ├─ Validate position (within world boundaries)
      ├─ Validate & clamp velocity (max speed: 5.0)
      ├─ Update player position = position + velocity
      └─ Apply boundary constraints
      
      SHOOT:
      ├─ Check player is alive
      ├─ Calculate bullet direction (from player velocity or event)
      ├─ Create new Bullet at player position
      └─ Add bullet to GameState.bullets list
      
      JUMP:
      ├─ Check player is alive
      ├─ Add upward velocity impulse (-3.0 on Y-axis)
      └─ Clamp velocity to max speed
   
   C. Update Physics:
      ├─ Move all bullets: position += velocity
      ├─ Check bullet-boundary collisions (remove if out of bounds)
      └─ Check bullet-player collisions:
         • Calculate distance between bullet and each player
         • If distance < (PLAYER_RADIUS + BULLET_RADIUS):
           → Apply damage to player
           → Remove bullet
           → Skip shooter (can't hit yourself)
   
   D. Cleanup:
      └─ Remove expired bullets (lifetime > 5 seconds)
   
   E. Update State Metadata:
      └─ Increment version, update timestamp
```

### Phase 5: Store State in Redis

```
5. Engine Service → Redis
   • Store updated GameState in Redis
   • Key: "room:room-abc"
   • Value: Serialized GameState (JSON)
   • Fields stored:
     - state: Full GameState object
     - lastUpdated: Timestamp
     - players: Player count
   • TTL: 300 seconds (if room becomes empty)
```

### Phase 6: Publish State Update

```
6. Engine Service → Kafka Producer
   • Create StateUpdate object:
     {
       "roomId": "room-abc",
       "gameState": {...},  // Full or diff
       "isFullUpdate": false,
       "timestamp": 1234567890
     }
   • Publish to topic: "game-state-updates"
   • Partition Key: roomId (same as input)
```

### Phase 7: Gateway Receives & Broadcasts

```
7. Gateway Service (Kafka Consumer) → WebSocket Clients
   • Gateway consumer subscribes to "game-state-updates"
   • Receives StateUpdate message
   • Converts to GameStateUpdateDTO
   • Broadcasts to all WebSocket clients subscribed to:
     /topic/room/{roomId}
   • All clients in that room receive real-time update
```

### Phase 8: Client Receives Update

```
8. WebSocket → Client
   • Client receives GameStateUpdateDTO via WebSocket
   • Updates local game rendering
   • Shows updated player positions, bullets, health, etc.
```

---

## 📊 Data Models Deep Dive

### 1. **PlayerEvent** (`engine-service/model/PlayerEvent.java`)

**Purpose:** Represents a player action that flows through the system.

**Fields:**
```java
String playerId          // Unique player identifier
String roomId           // Game room (Kafka partition key!)
ActionType actionType   // MOVE, SHOOT, or JUMP
long timestamp          // When event occurred
Position position       // Where player wants to be (optional)
Velocity velocity       // How fast player is moving (optional)
```

**Nested Classes:**
- `Position`: `{double x, double y}` - 2D coordinates
- `Velocity`: `{double vx, double vy}` - 2D velocity vector

**Key Characteristics:**
- ✅ Implements `Serializable` - Can be sent over Kafka
- ✅ `roomId` is used as Kafka partition key
- ✅ Timestamp auto-set on creation

**Example:**
```json
{
  "playerId": "player-123",
  "roomId": "room-abc",
  "actionType": "MOVE",
  "timestamp": 1699123456789,
  "position": {"x": 150.5, "y": 200.3},
  "velocity": {"vx": 2.0, "vy": -1.5}
}
```

---

### 2. **GameState** (`engine-service/model/GameState.java`)

**Purpose:** Authoritative game state for a room - the single source of truth.

**Fields:**
```java
String roomId                    // Room identifier
Map<String, Player> players      // All players in room (keyed by playerId)
List<Bullet> bullets             // All active bullets
long timestamp                   // Last update time
long version                     // Version number (for optimistic locking)
double worldWidth                // World boundary (default: 1000.0)
double worldHeight               // World boundary (default: 1000.0)
```

**Key Methods:**
- `addPlayer(Player)` - Adds player, increments version
- `removePlayer(playerId)` - Removes player, increments version
- `addBullet(Bullet)` - Adds bullet, increments version
- `removeBullet(bulletId)` - Removes bullet, increments version
- `clearExpiredBullets(time)` - Removes bullets older than 5 seconds
- `isEmpty()` - Returns true if no players or bullets

**Storage:**
- Stored in Redis under key: `"room:" + roomId`
- TTL: 300 seconds if room becomes empty

**Example in Redis:**
```
Key: "room:room-abc"
Value: {
  "state": { ... GameState object ... },
  "lastUpdated": 1699123456789,
  "players": 3
}
```

---

### 3. **Player** (`engine-service/model/Player.java`)

**Purpose:** Represents a player entity in the game.

**Fields:**
```java
String playerId                  // Unique identifier
Position position                // Current 2D position
Velocity velocity                // Current velocity vector
int health                       // Health points (0-100)
long lastActionTimestamp         // When last action occurred
String lastAction                // Last action performed
```

**Game Constants:**
- `MAX_HEALTH = 100`
- `MAX_SPEED = 5.0` (pixels per update)
- `PLAYER_RADIUS = 10.0` (collision radius)

**Key Methods:**
- `isAlive()` - Returns true if health > 0
- `takeDamage(amount)` - Reduces health, clamps to 0
- `heal(amount)` - Increases health, clamps to MAX_HEALTH

---

### 4. **Bullet** (`engine-service/model/Bullet.java`)

**Purpose:** Represents a projectile fired by a player.

**Fields:**
```java
String bulletId                  // Unique bullet identifier
String shooterId                 // Who fired it
Position position                // Current position
Velocity velocity                // Movement vector
long createdAt                   // When bullet was created
double damage                    // Damage amount (default: 25.0)
```

**Game Constants:**
- `BULLET_SPEED = 10.0`
- `BULLET_RADIUS = 2.0`
- `BULLET_DAMAGE = 25.0`
- `BULLET_LIFETIME_MS = 5000` (5 seconds)

**Key Methods:**
- `isExpired(currentTime)` - Returns true if bullet is older than 5 seconds

**Lifecycle:**
1. Created when player shoots
2. Moves each game update (position += velocity)
3. Removed when:
   - Hits a player (collision detected)
   - Hits world boundary
   - Expires (5 seconds old)

---

### 5. **StateUpdate** (`engine-service/model/StateUpdate.java`)

**Purpose:** Message published to Kafka containing game state updates.

**Fields:**
```java
String roomId                    // Target room
GameState gameState              // Full game state (if isFullUpdate=true)
StateDiff diff                   // Differential update (if isFullUpdate=false)
long timestamp                   // Update timestamp
boolean isFullUpdate             // Full vs diff flag
```

**Nested Class - StateDiff:**
```java
Map<String, Player> updatedPlayers    // Players that changed
List<String> removedPlayers           // Players that left
List<Bullet> newBullets               // Bullets added
List<String> removedBullets           // Bullets removed
long version                          // State version
```

**Usage:**
- Full updates: Entire GameState (useful for new clients)
- Diff updates: Only changes (more efficient for frequent updates)

---

## 🎮 GameLogic Class - Detailed Breakdown

### Main Method: `apply(GameState state, PlayerEvent event)`

This is the **core deterministic function** that transforms game state based on player actions.

```java
public GameState apply(GameState state, PlayerEvent event) {
    // 1. Initialize state if null (first event in room)
    if (state == null) {
        state = new GameState(event.getRoomId());
        state.setWorldWidth(1000.0);
        state.setWorldHeight(1000.0);
    }
    
    // 2. Get current timestamp
    long currentTime = System.currentTimeMillis();
    
    // 3. Route to action handler
    switch (event.getActionType()) {
        case MOVE: applyMove(state, event, currentTime); break;
        case SHOOT: applyShoot(state, event, currentTime); break;
        case JUMP: applyJump(state, event, currentTime); break;
    }
    
    // 4. Update physics (move bullets, check collisions)
    updatePhysics(state, currentTime);
    
    // 5. Cleanup expired bullets
    state.clearExpiredBullets(currentTime);
    
    return state;  // Return updated state
}
```

### Action Handlers

#### `applyMove()` - Movement Processing

```java
private void applyMove(GameState state, PlayerEvent event, long currentTime) {
    // 1. Get or create player
    Player player = getOrCreatePlayer(state, event.getPlayerId());
    
    // 2. Update position if provided
    if (event.getPosition() != null) {
        Position newPos = validatePosition(event.getPosition(), ...);
        player.setPosition(newPos);
    }
    
    // 3. Update velocity if provided
    if (event.getVelocity() != null) {
        Velocity newVel = validateAndClampVelocity(event.getVelocity());
        player.setVelocity(newVel);
    }
    
    // 4. Apply velocity to position (physics integration)
    updatePlayerPosition(player, ...);
    
    // 5. Update metadata
    player.setLastActionTimestamp(currentTime);
    player.setLastAction("MOVE");
}
```

**Velocity Clamping:**
```java
private Velocity validateAndClampVelocity(Velocity velocity) {
    double magnitude = Math.sqrt(vx² + vy²);
    if (magnitude > MAX_PLAYER_SPEED (5.0)) {
        // Scale down to max speed
        double scale = 5.0 / magnitude;
        return new Velocity(vx * scale, vy * scale);
    }
    return velocity;
}
```

**Boundary Constraints:**
```java
private void updatePlayerPosition(Player player, double worldWidth, double worldHeight) {
    double newX = position.x + velocity.vx;
    double newY = position.y + velocity.vy;
    
    // Clamp to world boundaries (with padding)
    newX = Math.max(20, Math.min(980, newX));  // Padding: 20px
    newY = Math.max(20, Math.min(980, newY));
    
    // Zero velocity component if hitting boundary
    if (newX == 20 || newX == 980) velocity.vx = 0;
    if (newY == 20 || newY == 980) velocity.vy = 0;
    
    player.setPosition(new Position(newX, newY));
}
```

#### `applyShoot()` - Bullet Creation

```java
private void applyShoot(GameState state, PlayerEvent event, long currentTime) {
    Player player = getOrCreatePlayer(state, event.getPlayerId());
    
    // 1. Validate player is alive
    if (!player.isAlive()) return;
    
    // 2. Calculate shoot direction
    Velocity direction = calculateShootDirection(player, event);
    
    // 3. Create bullet at player position
    String bulletId = UUID.randomUUID().toString();
    Bullet bullet = new Bullet(
        bulletId,
        event.getPlayerId(),  // shooterId
        player.getPosition(), // start position
        direction             // normalized and scaled to BULLET_SPEED
    );
    
    // 4. Add to game state
    state.addBullet(bullet);
    
    player.setLastActionTimestamp(currentTime);
    player.setLastAction("SHOOT");
}
```

#### `applyJump()` - Jump Action

```java
private void applyJump(GameState state, PlayerEvent event, long currentTime) {
    Player player = getOrCreatePlayer(state, event.getPlayerId());
    
    if (!player.isAlive()) return;
    
    // Add upward velocity impulse
    Velocity currentVel = player.getVelocity();
    Velocity jumpVel = new Velocity(
        currentVel.getVx(),
        currentVel.getVy() - 3.0  // Upward impulse
    );
    
    // Clamp to max speed
    jumpVel = validateAndClampVelocity(jumpVel);
    player.setVelocity(jumpVel);
    
    player.setLastActionTimestamp(currentTime);
    player.setLastAction("JUMP");
}
```

### Physics Updates

#### `updatePhysics()` - Bullet Movement & Collisions

```java
private void updatePhysics(GameState state, long currentTime) {
    for (Bullet bullet : state.getBullets()) {
        // 1. Move bullet
        double newX = bullet.position.x + bullet.velocity.vx;
        double newY = bullet.position.y + bullet.velocity.vy;
        bullet.setPosition(new Position(newX, newY));
        
        // 2. Check boundary collision
        if (isOutOfBounds(bullet.position, ...)) {
            state.removeBullet(bullet.bulletId);
            continue;
        }
        
        // 3. Check player collisions
        checkBulletCollisions(state, bullet);
    }
}
```

#### `checkBulletCollisions()` - Collision Detection

```java
private void checkBulletCollisions(GameState state, Bullet bullet) {
    for (Player player : state.getPlayers().values()) {
        // Skip shooter
        if (player.getPlayerId().equals(bullet.getShooterId())) continue;
        
        // Skip dead players
        if (!player.isAlive()) continue;
        
        // Circle-circle collision detection
        double distance = bullet.position.distance(player.position);
        double collisionDistance = PLAYER_RADIUS (10.0) + BULLET_RADIUS (2.0);
        
        if (distance < collisionDistance) {
            // Hit! Apply damage
            player.takeDamage((int) BULLET_DAMAGE (25.0));
            state.removeBullet(bullet.bulletId);
            break;  // Bullet can only hit one player
        }
    }
}
```

**Collision Detection Math:**
- Player: Circle with radius 10.0
- Bullet: Circle with radius 2.0
- Collision: Distance between centers < 12.0
- Distance formula: `√((x₂-x₁)² + (y₂-y₁)²)`

---

## ⚙️ Configuration Explained

### `application.yml` - Key Settings

#### Kafka Producer (for state updates)
```yaml
producer:
  key-serializer: StringSerializer        # roomId as key
  value-serializer: JsonSerializer        # StateUpdate as JSON
  acks: all                               # Wait for all replicas
  enable-idempotence: true                # Prevent duplicates
  retries: 3                              # Retry failed sends
  compression-type: snappy                # Compress messages
```

#### Kafka Consumer (for player events)
```yaml
consumer:
  group-id: engine-service-group          # Consumer group
  auto-offset-reset: earliest             # Read from beginning if no offset
  enable-auto-commit: false               # Manual commit for reliability
  properties:
    spring.json.trusted.packages: "*"     # Allow deserialization
```

#### Kafka Listener
```yaml
listener:
  ack-mode: manual_immediate              # Manual acknowledgment
  concurrency: 5                          # 5 parallel threads
  poll-timeout: 3000                      # 3 second poll timeout
  type: batch                             # Process batches of events
```

#### Redis
```yaml
redis:
  host: localhost                         # Redis server
  port: 6379                              # Redis port
  timeout: 2000ms                         # Connection timeout
  jedis:
    pool:
      max-active: 10                      # Max connections
      max-idle: 5                         # Max idle connections
```

---

## 🔍 Key Design Patterns & Concepts

### 1. **Deterministic Game Logic**
- Same input always produces same output
- No random numbers, no time-dependent logic (except cleanup)
- Critical for multi-instance deployments

### 2. **Event Sourcing Pattern**
- All state changes come from events
- State is derived from events, not modified directly
- Enables replay and debugging

### 3. **Partitioning Strategy**
- Kafka partitioned by `roomId`
- All events for same room → same partition
- Maintains event ordering per room
- Enables parallel processing across rooms

### 4. **Authoritative Server**
- Server (engine-service) is source of truth
- Clients receive updates, don't send state
- Prevents cheating and desync

### 5. **Version-Based State**
- Each state change increments version
- Enables optimistic locking
- Detects concurrent modifications

### 6. **Boundary Validation**
- All positions validated against world boundaries
- Velocities clamped to max speed
- Prevents cheating and invalid states

---

## 🚀 How Scaling Works

### Horizontal Scaling with Kafka Partitions

```
Room A events → Partition 0 → Engine Instance 1
Room B events → Partition 1 → Engine Instance 2
Room C events → Partition 2 → Engine Instance 1
Room D events → Partition 3 → Engine Instance 3
...
```

- Each room's events go to a specific partition
- Multiple engine instances can consume different partitions
- Same room always processed by same partition (ordering preserved)
- Can scale by adding more engine instances (up to partition count)

### Redis State Storage

```
Engine Instance 1 → Redis → "room:room-a" (GameState)
Engine Instance 2 → Redis → "room:room-b" (GameState)
```

- Shared state store (Redis)
- Any engine instance can read/write any room
- Partitioning ensures no concurrent writes to same room
- TTL cleans up empty rooms automatically

---

## 🔄 Complete Example: Player Moves Right

Let's trace a complete example:

### Step 1: Client Action
```javascript
// Client sends via WebSocket
{
  "playerId": "player-123",
  "roomId": "room-abc",
  "actionType": "MOVE",
  "velocity": {"vx": 3.0, "vy": 0}
}
```

### Step 2: Gateway Processes
```java
// Gateway converts to PlayerEvent
PlayerEvent event = new PlayerEvent(
    "player-123",
    "room-abc",
    ActionType.MOVE,
    null,  // no position update
    new Velocity(3.0, 0)
);

// Publishes to Kafka
kafkaTemplate.send("player-events", "room-abc", event);
```

### Step 3: Engine Consumes
```java
// Engine receives event
@KafkaListener(topics = "player-events")
public void processEvent(PlayerEvent event) {
    // Load state from Redis
    GameState state = redisRepository.get("room:" + event.getRoomId());
    
    // Apply game logic
    GameState updatedState = gameLogic.apply(state, event);
    
    // Save to Redis
    redisRepository.save("room:" + event.getRoomId(), updatedState);
    
    // Publish state update
    stateUpdatePublisher.publish(updatedState);
}
```

### Step 4: Game Logic Applies
```java
// GameLogic.apply() called
GameState apply(GameState state, PlayerEvent event) {
    Player player = getOrCreatePlayer(state, "player-123");
    
    // Current position: (500, 500)
    // Current velocity: (0, 0)
    
    // Set new velocity
    Velocity vel = validateAndClampVelocity(new Velocity(3.0, 0));
    // Result: (3.0, 0) - within max speed, no clamping needed
    player.setVelocity(vel);
    
    // Update position
    Position newPos = new Position(500 + 3.0, 500 + 0);
    // Result: (503.0, 500.0)
    player.setPosition(newPos);
    
    // Update physics (move bullets, check collisions)
    updatePhysics(state, ...);
    
    // Cleanup expired bullets
    state.clearExpiredBullets(...);
    
    // Increment version: 42 → 43
    state.setVersion(43);
    
    return state;
}
```

### Step 5: State Stored
```redis
SET "room:room-abc" {
  "state": {
    "roomId": "room-abc",
    "players": {
      "player-123": {
        "playerId": "player-123",
        "position": {"x": 503.0, "y": 500.0},
        "velocity": {"vx": 3.0, "vy": 0},
        "health": 100
      }
    },
    "bullets": [],
    "version": 43,
    "timestamp": 1699123456789
  },
  "lastUpdated": 1699123456789,
  "players": 1
}
EXPIRE "room:room-abc" 300  // TTL if room becomes empty
```

### Step 6: State Update Published
```java
// Publish to Kafka
StateUpdate update = new StateUpdate(
    "room-abc",
    updatedState,  // full or diff
    false  // isFullUpdate
);

kafkaTemplate.send("game-state-updates", "room-abc", update);
```

### Step 7: Gateway Broadcasts
```java
// Gateway consumer receives update
@KafkaListener(topics = "game-state-updates")
public void onStateUpdate(StateUpdate update) {
    // Convert to DTO
    GameStateUpdateDTO dto = convertToDTO(update);
    
    // Broadcast to all WebSocket clients in room
    messagingTemplate.convertAndSend(
        "/topic/room/" + update.getRoomId(),
        dto
    );
}
```

### Step 8: Client Receives
```javascript
// Client receives via WebSocket
stompClient.subscribe('/topic/room/room-abc', function(message) {
    const stateUpdate = JSON.parse(message.body);
    
    // Update local game view
    stateUpdate.players["player-123"].position;  // {x: 503.0, y: 500.0}
    
    // Render player at new position
    renderPlayer(stateUpdate.players["player-123"]);
});
```

---

## 🎯 Summary: What Does What

| Component | Responsibility | Technology |
|-----------|---------------|------------|
| **Gateway Service** | WebSocket handler, event routing | Spring WebSocket, Kafka Producer/Consumer |
| **Engine Service** | Game logic, state management | GameLogic class, Kafka Consumer, Redis |
| **Kafka** | Event streaming, message queue | Apache Kafka (topics: player-events, game-state-updates) |
| **Redis** | Fast state storage | Redis (keys: room:{roomId}) |
| **GameLogic** | Deterministic game rules | Pure Java class (no dependencies) |
| **Client** | User interface, rendering | Browser/Game Client (WebSocket/STOMP) |

---

## 📝 Key Takeaways

1. **Deterministic Logic**: GameLogic.apply() produces consistent results
2. **Partitioning**: Kafka partitioned by roomId ensures ordering
3. **State Authority**: Redis stores authoritative state, engine maintains it
4. **Real-time Updates**: WebSocket broadcasts keep clients synchronized
5. **Scalability**: Horizontal scaling via Kafka partitions
6. **Fault Tolerance**: DLQ, retries, idempotence prevent data loss

---

**This architecture ensures:**
- ✅ Consistent game state across instances
- ✅ Real-time player synchronization
- ✅ Scalable to thousands of concurrent rooms
- ✅ Fault-tolerant with retry mechanisms
- ✅ Cheat-proof (server authoritative)

