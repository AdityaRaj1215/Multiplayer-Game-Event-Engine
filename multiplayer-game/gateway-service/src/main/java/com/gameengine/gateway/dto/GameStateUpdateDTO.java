package com.gameengine.gateway.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DTO for sending game state updates to WebSocket clients.
 */
public class GameStateUpdateDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String roomId;
    private Map<String, PlayerDTO> players;
    private List<BulletDTO> bullets;
    private long timestamp;
    private long version;

    public GameStateUpdateDTO() {
        this.players = new HashMap<>();
        this.bullets = new ArrayList<>();
    }

    // Getters and Setters
    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public Map<String, PlayerDTO> getPlayers() {
        return players;
    }

    public void setPlayers(Map<String, PlayerDTO> players) {
        this.players = players;
    }

    public List<BulletDTO> getBullets() {
        return bullets;
    }

    public void setBullets(List<BulletDTO> bullets) {
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

    @Override
    public String toString() {
        return "GameStateUpdateDTO{" +
                "roomId='" + roomId + '\'' +
                ", playerCount=" + players.size() +
                ", bulletCount=" + bullets.size() +
                ", version=" + version +
                ", timestamp=" + timestamp +
                '}';
    }

    public static class PlayerDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        private String playerId;
        private PositionDTO position;
        private VelocityDTO velocity;
        private int health;

        public PlayerDTO() {}

        // Getters and Setters
        public String getPlayerId() {
            return playerId;
        }

        public void setPlayerId(String playerId) {
            this.playerId = playerId;
        }

        public PositionDTO getPosition() {
            return position;
        }

        public void setPosition(PositionDTO position) {
            this.position = position;
        }

        public VelocityDTO getVelocity() {
            return velocity;
        }

        public void setVelocity(VelocityDTO velocity) {
            this.velocity = velocity;
        }

        public int getHealth() {
            return health;
        }

        public void setHealth(int health) {
            this.health = health;
        }
    }

    public static class BulletDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        private String bulletId;
        private String shooterId;
        private PositionDTO position;
        private VelocityDTO velocity;

        public BulletDTO() {}

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

        public PositionDTO getPosition() {
            return position;
        }

        public void setPosition(PositionDTO position) {
            this.position = position;
        }

        public VelocityDTO getVelocity() {
            return velocity;
        }

        public void setVelocity(VelocityDTO velocity) {
            this.velocity = velocity;
        }
    }

    public static class PositionDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private double x;
        private double y;

        public PositionDTO() {}

        public PositionDTO(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getY() {
            return y;
        }

        public void setY(double y) {
            this.y = y;
        }
    }

    public static class VelocityDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private double vx;
        private double vy;

        public VelocityDTO() {}

        public VelocityDTO(double vx, double vy) {
            this.vx = vx;
            this.vy = vy;
        }

        public double getVx() {
            return vx;
        }

        public void setVx(double vx) {
            this.vx = vx;
        }

        public double getVy() {
            return vy;
        }

        public void setVy(double vy) {
            this.vy = vy;
        }
    }
}

