package com.gameengine.engine.model;

import java.io.Serializable;

/**
 * Represents a state update message that is published to Kafka topic 'game-state-updates'.
 * This contains either a full state or a diff update.
 */
public class StateUpdate implements Serializable {
    private static final long serialVersionUID = 1L;

    private String roomId;
    private GameState gameState;
    private StateDiff diff;
    private long timestamp;
    private boolean isFullUpdate;

    public StateUpdate() {
        this.timestamp = System.currentTimeMillis();
    }

    public StateUpdate(String roomId, GameState gameState) {
        this();
        this.roomId = roomId;
        this.gameState = gameState;
        this.isFullUpdate = true;
    }

    public StateUpdate(String roomId, StateDiff diff) {
        this();
        this.roomId = roomId;
        this.diff = diff;
        this.isFullUpdate = false;
    }

    // Getters and Setters
    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public StateDiff getDiff() {
        return diff;
    }

    public void setDiff(StateDiff diff) {
        this.diff = diff;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isFullUpdate() {
        return isFullUpdate;
    }

    public void setFullUpdate(boolean fullUpdate) {
        isFullUpdate = fullUpdate;
    }

    @Override
    public String toString() {
        return "StateUpdate{" +
                "roomId='" + roomId + '\'' +
                ", isFullUpdate=" + isFullUpdate +
                ", timestamp=" + timestamp +
                '}';
    }

    /**
     * Represents a differential update to the game state.
     */
    public static class StateDiff implements Serializable {
        private static final long serialVersionUID = 1L;

        private Map<String, Player> updatedPlayers;
        private List<String> removedPlayers;
        private List<Bullet> newBullets;
        private List<String> removedBullets;
        private long version;

        public StateDiff() {
            this.updatedPlayers = new java.util.HashMap<>();
            this.removedPlayers = new java.util.ArrayList<>();
            this.newBullets = new java.util.ArrayList<>();
            this.removedBullets = new java.util.ArrayList<>();
        }

        public Map<String, Player> getUpdatedPlayers() {
            return updatedPlayers;
        }

        public void setUpdatedPlayers(Map<String, Player> updatedPlayers) {
            this.updatedPlayers = updatedPlayers;
        }

        public List<String> getRemovedPlayers() {
            return removedPlayers;
        }

        public void setRemovedPlayers(List<String> removedPlayers) {
            this.removedPlayers = removedPlayers;
        }

        public List<Bullet> getNewBullets() {
            return newBullets;
        }

        public void setNewBullets(List<Bullet> newBullets) {
            this.newBullets = newBullets;
        }

        public List<String> getRemovedBullets() {
            return removedBullets;
        }

        public void setRemovedBullets(List<String> removedBullets) {
            this.removedBullets = removedBullets;
        }

        public long getVersion() {
            return version;
        }

        public void setVersion(long version) {
            this.version = version;
        }

        public boolean isEmpty() {
            return updatedPlayers.isEmpty() && 
                   removedPlayers.isEmpty() && 
                   newBullets.isEmpty() && 
                   removedBullets.isEmpty();
        }

        @Override
        public String toString() {
            return "StateDiff{" +
                    "updatedPlayers=" + updatedPlayers.size() +
                    ", removedPlayers=" + removedPlayers.size() +
                    ", newBullets=" + newBullets.size() +
                    ", removedBullets=" + removedBullets.size() +
                    ", version=" + version +
                    '}';
        }
    }
}

