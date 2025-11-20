package com.gameengine.engine.model;

import java.io.Serializable;

/**
 * Represents a bullet/projectile in the game.
 */
public class Bullet implements Serializable {
    private static final long serialVersionUID = 1L;

    private String bulletId;
    private String shooterId;
    private PlayerEvent.Position position;
    private PlayerEvent.Velocity velocity;
    private long createdAt;
    private double damage;

    // Game constants
    private static final double BULLET_SPEED = 10.0;
    private static final double BULLET_RADIUS = 2.0;
    private static final double BULLET_DAMAGE = 25.0;
    private static final long BULLET_LIFETIME_MS = 5000; // 5 seconds

    public Bullet() {
        this.damage = BULLET_DAMAGE;
        this.createdAt = System.currentTimeMillis();
    }

    public Bullet(String bulletId, String shooterId, PlayerEvent.Position position, PlayerEvent.Velocity direction) {
        this();
        this.bulletId = bulletId;
        this.shooterId = shooterId;
        this.position = position;
        // Normalize direction and scale to bullet speed
        double magnitude = direction.getMagnitude();
        if (magnitude > 0) {
            double scale = BULLET_SPEED / magnitude;
            this.velocity = new PlayerEvent.Velocity(
                    direction.getVx() * scale,
                    direction.getVy() * scale
            );
        } else {
            this.velocity = new PlayerEvent.Velocity(BULLET_SPEED, 0);
        }
    }

    public boolean isExpired(long currentTime) {
        return (currentTime - createdAt) > BULLET_LIFETIME_MS;
    }

    // Getters and Setters
    public String getBulletId() {
        return bulletId;
    }

    public void setBulletId(String bulletId) {
        this.bulletId = bulletId;
    }

    public String getShooterId() {
        return shooterId;
    }

    public void setShooterId(String shooterId) {
        this.shooterId = shooterId;
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

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public double getDamage() {
        return damage;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public static double getBulletSpeed() {
        return BULLET_SPEED;
    }

    public static double getBulletRadius() {
        return BULLET_RADIUS;
    }

    public static double getBulletDamage() {
        return BULLET_DAMAGE;
    }

    public static long getBulletLifetimeMs() {
        return BULLET_LIFETIME_MS;
    }

    @Override
    public String toString() {
        return "Bullet{" +
                "bulletId='" + bulletId + '\'' +
                ", shooterId='" + shooterId + '\'' +
                ", position=" + position +
                ", velocity=" + velocity +
                ", damage=" + damage +
                '}';
    }
}

