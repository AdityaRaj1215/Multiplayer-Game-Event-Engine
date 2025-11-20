package com.gameengine.engine.model;

import java.io.Serializable;
import java.time.Instant;

/**
 * Represents a player action event in the game.
 * This event is published to Kafka topic 'player-events' with roomId as the partition key.
 */
public class PlayerEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private String playerId;
    private String roomId;
    private ActionType actionType;
    private long timestamp;
    private Position position;
    private Velocity velocity;

    public PlayerEvent() {
        this.timestamp = Instant.now().toEpochMilli();
    }

    public PlayerEvent(String playerId, String roomId, ActionType actionType) {
        this();
        this.playerId = playerId;
        this.roomId = roomId;
        this.actionType = actionType;
    }

    public PlayerEvent(String playerId, String roomId, ActionType actionType, Position position, Velocity velocity) {
        this(playerId, roomId, actionType);
        this.position = position;
        this.velocity = velocity;
    }

    // Getters and Setters
    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Velocity getVelocity() {
        return velocity;
    }

    public void setVelocity(Velocity velocity) {
        this.velocity = velocity;
    }

    @Override
    public String toString() {
        return "PlayerEvent{" +
                "playerId='" + playerId + '\'' +
                ", roomId='" + roomId + '\'' +
                ", actionType=" + actionType +
                ", timestamp=" + timestamp +
                ", position=" + position +
                ", velocity=" + velocity +
                '}';
    }

    /**
     * Available action types for players
     */
    public enum ActionType {
        MOVE,
        SHOOT,
        JUMP
    }

    /**
     * 2D Position representation
     */
    public static class Position implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private double x;
        private double y;

        public Position() {}

        public Position(double x, double y) {
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

        public double distance(Position other) {
            double dx = this.x - other.x;
            double dy = this.y - other.y;
            return Math.sqrt(dx * dx + dy * dy);
        }

        @Override
        public String toString() {
            return "Position{x=" + x + ", y=" + y + '}';
        }
    }

    /**
     * 2D Velocity representation
     */
    public static class Velocity implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private double vx;
        private double vy;

        public Velocity() {}

        public Velocity(double vx, double vy) {
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

        public double getMagnitude() {
            return Math.sqrt(vx * vx + vy * vy);
        }

        @Override
        public String toString() {
            return "Velocity{vx=" + vx + ", vy=" + vy + '}';
        }
    }
}

