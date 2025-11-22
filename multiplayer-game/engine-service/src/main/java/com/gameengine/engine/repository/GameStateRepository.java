package com.gameengine.engine.repository;

import com.gameengine.engine.model.GameState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

/**
 * Repository for managing game state in Redis.
 * Stores authoritative game state under key: "room:<roomId>"
 */
@Repository
public class GameStateRepository {

    private static final Logger logger = LoggerFactory.getLogger(GameStateRepository.class);
    private static final String ROOM_KEY_PREFIX = "room:";
    private static final String STATE_FIELD = "state";
    private static final String LAST_UPDATED_FIELD = "lastUpdated";
    private static final String PLAYERS_FIELD = "players";

    private final RedisTemplate<String, GameState> redisTemplate;
    private final long roomTtlSeconds;

    public GameStateRepository(RedisTemplate<String, GameState> redisTemplate,
                               @Value("${game.engine.room-ttl-seconds:300}") long roomTtlSeconds) {
        this.redisTemplate = redisTemplate;
        this.roomTtlSeconds = roomTtlSeconds;
    }

    /**
     * Gets the game state for a room.
     *
     * @param roomId Room identifier
     * @return GameState or null if not found
     */
    public GameState getGameState(String roomId) {
        String key = ROOM_KEY_PREFIX + roomId;
        try {
            GameState state = redisTemplate.opsForValue().get(key);
            if (state == null) {
                logger.debug("No game state found for room: {}", roomId);
            }
            return state;
        } catch (Exception e) {
            logger.error("Error getting game state for room: {}", roomId, e);
            throw new RuntimeException("Failed to retrieve game state", e);
        }
    }

    /**
     * Saves the game state for a room.
     * Sets TTL if room is empty, otherwise extends TTL.
     *
     * @param roomId Room identifier
     * @param gameState Game state to save
     */
    public void saveGameState(String roomId, GameState gameState) {
        String key = ROOM_KEY_PREFIX + roomId;
        try {
            // Save the state
            redisTemplate.opsForValue().set(key, gameState);
            
            // Set or extend TTL
            // If room is empty, set TTL for cleanup
            // If room has players, extend TTL
            if (gameState.isEmpty()) {
                redisTemplate.expire(key, roomTtlSeconds, TimeUnit.SECONDS);
                logger.debug("Set TTL {} seconds for empty room: {}", roomTtlSeconds, roomId);
            } else {
                // Extend TTL for active rooms
                redisTemplate.expire(key, roomTtlSeconds, TimeUnit.SECONDS);
            }
            
            logger.debug("Saved game state for room: {} (players: {}, bullets: {}, version: {})",
                    roomId, gameState.getPlayerCount(), gameState.getBulletCount(), gameState.getVersion());
            
        } catch (Exception e) {
            logger.error("Error saving game state for room: {}", roomId, e);
            throw new RuntimeException("Failed to save game state", e);
        }
    }

    /**
     * Deletes the game state for a room.
     *
     * @param roomId Room identifier
     */
    public void deleteGameState(String roomId) {
        String key = ROOM_KEY_PREFIX + roomId;
        try {
            redisTemplate.delete(key);
            logger.info("Deleted game state for room: {}", roomId);
        } catch (Exception e) {
            logger.error("Error deleting game state for room: {}", roomId, e);
            throw new RuntimeException("Failed to delete game state", e);
        }
    }

    /**
     * Checks if a room exists.
     *
     * @param roomId Room identifier
     * @return true if room exists
     */
    public boolean roomExists(String roomId) {
        String key = ROOM_KEY_PREFIX + roomId;
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            logger.error("Error checking if room exists: {}", roomId, e);
            return false;
        }
    }
}

