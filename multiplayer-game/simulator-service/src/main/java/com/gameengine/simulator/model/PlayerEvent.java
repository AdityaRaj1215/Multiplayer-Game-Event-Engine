package com.gameengine.simulator.model;

import java.io.Serializable;
import java.time.Instant;

/**
 * Represents a player action event for the simulator.
 * This matches the event structure used by the engine service.
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

    public enum ActionType {
        MOVE,
        SHOOT,
        JUMP
    }

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

        @Override
        public String toString() {
            return "Position{x=" + x + ", y=" + y + '}';
        }
    }

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

        @Override
        public String toString() {
            return "Velocity{vx=" + vx + ", vy=" + vy + '}';
        }
    }
}

