package com.gameengine.gateway.dto;

import java.io.Serializable;

/**
 * DTO for receiving player actions from WebSocket/REST clients.
 */
public class PlayerActionDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String playerId;
    private String roomId;
    private String actionType; // "MOVE", "SHOOT", "JUMP"
    private PositionDTO position;
    private VelocityDTO velocity;

    public PlayerActionDTO() {}

    public PlayerActionDTO(String playerId, String roomId, String actionType) {
        this.playerId = playerId;
        this.roomId = roomId;
        this.actionType = actionType;
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

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
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

    @Override
    public String toString() {
        return "PlayerActionDTO{" +
                "playerId='" + playerId + '\'' +
                ", roomId='" + roomId + '\'' +
                ", actionType='" + actionType + '\'' +
                ", position=" + position +
                ", velocity=" + velocity +
                '}';
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

