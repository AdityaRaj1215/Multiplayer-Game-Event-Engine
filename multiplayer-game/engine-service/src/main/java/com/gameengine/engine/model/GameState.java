package com.gameengine.engine.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Represents the complete game state for a room.
 * This is the authoritative state stored in Redis under key "room:<roomId>"
 */
public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;

    private String roomId;
    private Map<String, Player> players;
    private List<Bullet> bullets;
    private long timestamp;
    private long version;
    private double worldWidth;
    private double worldHeight;

    // Game world constants
    private static final double DEFAULT_WORLD_WIDTH = 1000.0;
    private static final double DEFAULT_WORLD_HEIGHT = 1000.0;

    public GameState() {
        this.players = new HashMap<>();
        this.bullets = new ArrayList<>();
        this.timestamp = System.currentTimeMillis();
        this.version = 0;
        this.worldWidth = DEFAULT_WORLD_WIDTH;
        this.worldHeight = DEFAULT_WORLD_HEIGHT;
    }

    public GameState(String roomId) {
        this();
        this.roomId = roomId;
    }

    public Player getPlayer(String playerId) {
        return players.get(playerId);
    }

    public void addPlayer(Player player) {
        players.put(player.getPlayerId(), player);
        incrementVersion();
    }

    public void removePlayer(String playerId) {
        players.remove(playerId);
        incrementVersion();
    }

    public void addBullet(Bullet bullet) {
        bullets.add(bullet);
        incrementVersion();
    }

    public void removeBullet(String bulletId) {
        bullets.removeIf(b -> b.getBulletId().equals(bulletId));
        incrementVersion();
    }

    public void clearExpiredBullets(long currentTime) {
        bullets.removeIf(bullet -> bullet.isExpired(currentTime));
    }

    private void incrementVersion() {
        this.version++;
        this.timestamp = System.currentTimeMillis();
    }

    public boolean isEmpty() {
        return players.isEmpty() && bullets.isEmpty();
    }

    public int getPlayerCount() {
        return players.size();
    }

    public int getBulletCount() {
        return bullets.size();
    }

    // Getters and Setters
    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public Map<String, Player> getPlayers() {
        return players;
    }

    public void setPlayers(Map<String, Player> players) {
        this.players = players;
    }

    public List<Bullet> getBullets() {
        return bullets;
    }

    public void setBullets(List<Bullet> bullets) {
        this.bullets = bullets;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public double getWorldWidth() {
        return worldWidth;
    }

    public void setWorldWidth(double worldWidth) {
        this.worldWidth = worldWidth;
    }

    public double getWorldHeight() {
        return worldHeight;
    }

    public void setWorldHeight(double worldHeight) {
        this.worldHeight = worldHeight;
    }

    public static double getDefaultWorldWidth() {
        return DEFAULT_WORLD_WIDTH;
    }

    public static double getDefaultWorldHeight() {
        return DEFAULT_WORLD_HEIGHT;
    }

    @Override
    public String toString() {
        return "GameState{" +
                "roomId='" + roomId + '\'' +
                ", playerCount=" + players.size() +
                ", bulletCount=" + bullets.size() +
                ", timestamp=" + timestamp +
                ", version=" + version +
                ", worldSize=" + worldWidth + "x" + worldHeight +
                '}';
    }
}

