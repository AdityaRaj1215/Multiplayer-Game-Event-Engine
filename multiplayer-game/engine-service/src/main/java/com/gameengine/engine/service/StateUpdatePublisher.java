package com.gameengine.engine.service;

import com.gameengine.engine.model.GameState;
import com.gameengine.engine.model.StateUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Service for publishing game state updates to Kafka.
 * Publishes to topic: game-state-updates (partitioned by roomId)
 */
@Service
public class StateUpdatePublisher {

    private static final Logger logger = LoggerFactory.getLogger(StateUpdatePublisher.class);

    private final KafkaTemplate<String, StateUpdate> kafkaTemplate;
    private final String stateUpdatesTopic;
    private final boolean enableDiffUpdates;

    public StateUpdatePublisher(
            @org.springframework.beans.factory.annotation.Qualifier("stateUpdateKafkaTemplate")
            KafkaTemplate<String, StateUpdate> stateUpdateKafkaTemplate,
            @Value("${spring.kafka.topics.game-state-updates}") String stateUpdatesTopic,
            @Value("${game.engine.enable-diff-updates:true}") boolean enableDiffUpdates) {
        this.kafkaTemplate = stateUpdateKafkaTemplate;
        this.stateUpdatesTopic = stateUpdatesTopic;
        this.enableDiffUpdates = enableDiffUpdates;
    }

    /**
     * Publishes a state update to Kafka.
     * Uses roomId as partition key to ensure ordering per room.
     *
     * @param roomId Room identifier (partition key)
     * @param gameState Updated game state
     */
    public void publishStateUpdate(String roomId, GameState gameState) {
        try {
            // Create state update message
            StateUpdate stateUpdate = new StateUpdate(roomId, gameState);
            stateUpdate.setTimestamp(System.currentTimeMillis());
            stateUpdate.setFullUpdate(!enableDiffUpdates); // Use full update if diffs disabled

            // Publish to Kafka with roomId as key (partition key)
            CompletableFuture<SendResult<String, StateUpdate>> future = 
                    kafkaTemplate.send(stateUpdatesTopic, roomId, stateUpdate);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.debug("Published state update for room: {} (version: {}) to topic: {}",
                            roomId, gameState.getVersion(), stateUpdatesTopic);
                } else {
                    logger.error("Failed to publish state update for room: {}", roomId, ex);
                }
            });

        } catch (Exception e) {
            logger.error("Error publishing state update for room: {}", roomId, e);
            // Don't throw - we don't want to fail event processing if publishing fails
            // Kafka retries will handle transient failures
        }
    }

    /**
     * Publishes a differential state update.
     * More efficient than full state updates.
     *
     * @param roomId Room identifier
     * @param stateDiff Differential update
     */
    public void publishStateDiff(String roomId, StateUpdate.StateDiff stateDiff) {
        if (!enableDiffUpdates) {
            // If diffs disabled, don't publish diff
            return;
        }

        try {
            StateUpdate stateUpdate = new StateUpdate(roomId, stateDiff);
            stateUpdate.setTimestamp(System.currentTimeMillis());
            stateUpdate.setFullUpdate(false);

            CompletableFuture<SendResult<String, StateUpdate>> future = 
                    kafkaTemplate.send(stateUpdatesTopic, roomId, stateUpdate);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.debug("Published state diff for room: {} to topic: {}", roomId, stateUpdatesTopic);
                } else {
                    logger.error("Failed to publish state diff for room: {}", roomId, ex);
                }
            });

        } catch (Exception e) {
            logger.error("Error publishing state diff for room: {}", roomId, e);
        }
    }
}

