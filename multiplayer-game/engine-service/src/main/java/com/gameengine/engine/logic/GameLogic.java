package com.gameengine.engine.logic;

import com.gameengine.engine.model.Bullet;
import com.gameengine.engine.model.GameState;
import com.gameengine.engine.model.Player;
import com.gameengine.engine.model.PlayerEvent;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Deterministic game logic engine.
 * 
 * This class applies game rules and validates player actions.
 * All logic must be deterministic to ensure consistent state across instances.
 */
@Component
public class GameLogic {

    // Game constants (must match Player and Bullet constants)
    private static final double MAX_PLAYER_SPEED = 5.0;
    private static final double PLAYER_RADIUS = 10.0;
    private static final double WORLD_BOUNDARY_PADDING = 20.0;
    private static final double BULLET_SPEED = 10.0;
    private static final double BULLET_RADIUS = 2.0;

    /**
     * Applies a player event to the game state and returns the updated state.
     * This is the core deterministic game logic method.
     * 
     * @param state Current game state
     * @param event Player event to apply
     * @return Updated game state
     */
    public GameState apply(GameState state, PlayerEvent event) {
        if (state == null) {
            state = new GameState(event.getRoomId());
            state.setWorldWidth(GameState.getDefaultWorldWidth());
            state.setWorldHeight(GameState.getDefaultWorldHeight());
        }

        // Update timestamp
        long currentTime = System.currentTimeMillis();

        // Process based on action type
        switch (event.getActionType()) {
            case MOVE:
                applyMove(state, event, currentTime);
                break;
            case SHOOT:
                applyShoot(state, event, currentTime);
                break;
            case JUMP:
                applyJump(state, event, currentTime);
                break;
            default:
                // Unknown action type, log but don't crash
                System.err.println("Unknown action type: " + event.getActionType());
        }

        // Update physics (move bullets, check collisions)
        updatePhysics(state, currentTime);

        // Clean up expired bullets
        state.clearExpiredBullets(currentTime);

        return state;
    }

    /**
     * Applies a MOVE action to update player position and velocity.
     */
    private void applyMove(GameState state, PlayerEvent event, long currentTime) {
        Player player = getOrCreatePlayer(state, event.getPlayerId());

        if (event.getPosition() != null) {
            PlayerEvent.Position newPosition = event.getPosition();
            // Validate position is within world boundaries
            newPosition = validatePosition(newPosition, state.getWorldWidth(), state.getWorldHeight());
            player.setPosition(newPosition);
        }

        if (event.getVelocity() != null) {
            PlayerEvent.Velocity newVelocity = event.getVelocity();
            // Validate and clamp velocity to max speed
            newVelocity = validateAndClampVelocity(newVelocity);
            player.setVelocity(newVelocity);
        }

        // Update position based on velocity (simple integration)
        updatePlayerPosition(player, state.getWorldWidth(), state.getWorldHeight());

        player.setLastActionTimestamp(currentTime);
        player.setLastAction("MOVE");
    }

    /**
     * Applies a SHOOT action to spawn a bullet.
     */
    private void applyShoot(GameState state, PlayerEvent event, long currentTime) {
        Player player = getOrCreatePlayer(state, event.getPlayerId());

        // Player must be alive to shoot
        if (!player.isAlive()) {
            return;
        }

        // Calculate bullet direction (normalized velocity or forward direction)
        PlayerEvent.Velocity direction = calculateShootDirection(player, event);

        // Create bullet starting at player position
        String bulletId = UUID.randomUUID().toString();
        Bullet bullet = new Bullet(bulletId, event.getPlayerId(), 
                                   new PlayerEvent.Position(player.getPosition().getX(), player.getPosition().getY()),
                                   direction);
        
        state.addBullet(bullet);

        player.setLastActionTimestamp(currentTime);
        player.setLastAction("SHOOT");
    }

    /**
     * Applies a JUMP action (could affect velocity or position).
     */
    private void applyJump(GameState state, PlayerEvent event, long currentTime) {
        Player player = getOrCreatePlayer(state, event.getPlayerId());

        if (!player.isAlive()) {
            return;
        }

        // Simple jump: add upward velocity component
        PlayerEvent.Velocity currentVelocity = player.getVelocity();
        PlayerEvent.Velocity jumpVelocity = new PlayerEvent.Velocity(
                currentVelocity.getVx(),
                currentVelocity.getVy() - 3.0  // Upward impulse
        );
        
        jumpVelocity = validateAndClampVelocity(jumpVelocity);
        player.setVelocity(jumpVelocity);

        player.setLastActionTimestamp(currentTime);
        player.setLastAction("JUMP");
    }

    /**
     * Updates physics: moves bullets, checks collisions.
     */
    private void updatePhysics(GameState state, long currentTime) {
        // Move bullets
        for (Bullet bullet : state.getBullets()) {
            PlayerEvent.Position pos = bullet.getPosition();
            PlayerEvent.Velocity vel = bullet.getVelocity();
            
            // Update bullet position
            double newX = pos.getX() + vel.getVx();
            double newY = pos.getY() + vel.getVy();
            
            bullet.setPosition(new PlayerEvent.Position(newX, newY));

            // Check if bullet hit world boundaries
            if (isOutOfBounds(bullet.getPosition(), state.getWorldWidth(), state.getWorldHeight())) {
                state.removeBullet(bullet.getBulletId());
                continue;
            }

            // Check bullet-player collisions (excluding shooter)
            checkBulletCollisions(state, bullet);
        }
    }

    /**
     * Checks if a bullet collides with any players (excluding the shooter).
     */
    private void checkBulletCollisions(GameState state, Bullet bullet) {
        for (Player player : state.getPlayers().values()) {
            // Don't hit the shooter
            if (player.getPlayerId().equals(bullet.getShooterId())) {
                continue;
            }

            // Don't hit dead players
            if (!player.isAlive()) {
                continue;
            }

            // Check collision (simple circle-circle collision)
            double distance = bullet.getPosition().distance(player.getPosition());
            double collisionDistance = PLAYER_RADIUS + BULLET_RADIUS;

            if (distance < collisionDistance) {
                // Hit! Apply damage and remove bullet
                player.takeDamage((int) Bullet.getBulletDamage());
                state.removeBullet(bullet.getBulletId());
                break; // Bullet can only hit one player
            }
        }
    }

    /**
     * Updates player position based on velocity with boundary checks.
     */
    private void updatePlayerPosition(Player player, double worldWidth, double worldHeight) {
        PlayerEvent.Position pos = player.getPosition();
        PlayerEvent.Velocity vel = player.getVelocity();

        double newX = pos.getX() + vel.getVx();
        double newY = pos.getY() + vel.getVy();

        // Apply boundary constraints
        double minX = WORLD_BOUNDARY_PADDING;
        double maxX = worldWidth - WORLD_BOUNDARY_PADDING;
        double minY = WORLD_BOUNDARY_PADDING;
        double maxY = worldHeight - WORLD_BOUNDARY_PADDING;

        newX = Math.max(minX, Math.min(maxX, newX));
        newY = Math.max(minY, Math.min(maxY, newY));

        // If hitting boundary, zero out velocity component
        if (newX == minX || newX == maxX) {
            vel.setVx(0);
        }
        if (newY == minY || newY == maxY) {
            vel.setVy(0);
        }

        player.setPosition(new PlayerEvent.Position(newX, newY));
    }

    /**
     * Validates and clamps velocity to maximum speed.
     */
    private PlayerEvent.Velocity validateAndClampVelocity(PlayerEvent.Velocity velocity) {
        double magnitude = Math.sqrt(velocity.getVx() * velocity.getVx() + velocity.getVy() * velocity.getVy());
        
        if (magnitude > MAX_PLAYER_SPEED) {
            double scale = MAX_PLAYER_SPEED / magnitude;
            return new PlayerEvent.Velocity(
                    velocity.getVx() * scale,
                    velocity.getVy() * scale
            );
        }
        
        return velocity;
    }

    /**
     * Validates position is within world boundaries.
     */
    private PlayerEvent.Position validatePosition(PlayerEvent.Position position, double worldWidth, double worldHeight) {
        double padding = WORLD_BOUNDARY_PADDING;
        double x = Math.max(padding, Math.min(worldWidth - padding, position.getX()));
        double y = Math.max(padding, Math.min(worldHeight - padding, position.getY()));
        return new PlayerEvent.Position(x, y);
    }

    /**
     * Checks if a position is out of world bounds.
     */
    private boolean isOutOfBounds(PlayerEvent.Position position, double worldWidth, double worldHeight) {
        return position.getX() < 0 || position.getX() > worldWidth ||
               position.getY() < 0 || position.getY() > worldHeight;
    }

    /**
     * Calculates shoot direction based on player velocity or default forward direction.
     */
    private PlayerEvent.Velocity calculateShootDirection(Player player, PlayerEvent event) {
        if (event.getVelocity() != null && 
            (event.getVelocity().getVx() != 0 || event.getVelocity().getVy() != 0)) {
            // Use event velocity direction
            return event.getVelocity();
        } else if (player.getVelocity() != null &&
                   (player.getVelocity().getVx() != 0 || player.getVelocity().getVy() != 0)) {
            // Use player velocity direction
            return player.getVelocity();
        } else {
            // Default: shoot right
            return new PlayerEvent.Velocity(BULLET_SPEED, 0);
        }
    }

    /**
     * Gets existing player or creates a new one at center of world.
     */
    private Player getOrCreatePlayer(GameState state, String playerId) {
        Player player = state.getPlayer(playerId);
        if (player == null) {
            player = new Player(playerId);
            // Spawn at center of world
            player.setPosition(new PlayerEvent.Position(
                    state.getWorldWidth() / 2,
                    state.getWorldHeight() / 2
            ));
            state.addPlayer(player);
        }
        return player;
    }
}


