package com.gameengine.engine.model;

import java.io.Serializable;
import java.time.Instant;

/**
 * Represents a player in the game state.
 */
public class Player implements Serializable {
    private static final long serialVersionUID = 1L;

    private String playerId;
    private PlayerEvent.Position position;
    private PlayerEvent.Velocity velocity;
    private int health;
    private long lastActionTimestamp;
    private String lastAction;

    // Game constants
    private static final int MAX_HEALTH = 100;
    private static final double MAX_SPEED = 5.0; // pixels per frame/update
    private static final double PLAYER_RADIUS = 10.0; // collision radius

    public Player() {
        this.health = MAX_HEALTH;
        this.lastActionTimestamp = Instant.now().toEpochMilli();
        this.position = new PlayerEvent.Position(0, 0);
        this.velocity = new PlayerEvent.Velocity(0, 0);
    }

    public Player(String playerId) {
        this();
        this.playerId = playerId;
    }

    public Player(String playerId, PlayerEvent.Position position) {
        this(playerId);
        this.position = position;
    }

    // Getters and Setters
    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public PlayerEvent.Position getPosition() {
        return position;
    }

    public void setPosition(PlayerEvent.Position position) {
        this.position = position;
    }

    public PlayerEvent.Velocity getVelocity() {
        return velocity;
    }

    public void setVelocity(PlayerEvent.Velocity velocity) {
        this.velocity = velocity;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = Math.max(0, Math.min(MAX_HEALTH, health));
    }

    public long getLastActionTimestamp() {
        return lastActionTimestamp;
    }

    public void setLastActionTimestamp(long lastActionTimestamp) {
        this.lastActionTimestamp = lastActionTimestamp;
    }

    public String getLastAction() {
        return lastAction;
    }

    public void setLastAction(String lastAction) {
        this.lastAction = lastAction;
    }

    public boolean isAlive() {
        return health > 0;
    }

    public void takeDamage(int damage) {
        this.health = Math.max(0, this.health - damage);
    }

    public void heal(int amount) {
        this.health = Math.min(MAX_HEALTH, this.health + amount);
    }

    public static int getMaxHealth() {
        return MAX_HEALTH;
    }

    public static double getMaxSpeed() {
        return MAX_SPEED;
    }

    public static double getPlayerRadius() {
        return PLAYER_RADIUS;
    }

    @Override
    public String toString() {
        return "Player{" +
                "playerId='" + playerId + '\'' +
                ", position=" + position +
                ", velocity=" + velocity +
                ", health=" + health +
                ", lastAction='" + lastAction + '\'' +
                '}';
    }
}

