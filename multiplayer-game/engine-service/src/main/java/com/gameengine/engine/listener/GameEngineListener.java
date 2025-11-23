package com.gameengine.engine.listener;

import com.gameengine.engine.logic.GameLogic;
import com.gameengine.engine.model.GameState;
import com.gameengine.engine.model.PlayerEvent;
import com.gameengine.engine.repository.GameStateRepository;
import com.gameengine.engine.service.StateUpdatePublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Kafka listener that consumes player events and processes them through the game engine.
 */
@Component
public class GameEngineListener {

    private static final Logger logger = LoggerFactory.getLogger(GameEngineListener.class);

    private final GameLogic gameLogic;
    private final GameStateRepository gameStateRepository;
    private final StateUpdatePublisher stateUpdatePublisher;

    public GameEngineListener(GameLogic gameLogic,
                              GameStateRepository gameStateRepository,
                              StateUpdatePublisher stateUpdatePublisher) {
        this.gameLogic = gameLogic;
        this.gameStateRepository = gameStateRepository;
        this.stateUpdatePublisher = stateUpdatePublisher;
    }

    /**
     * Listens to player-events topic and processes events in batches.
     * Processes events for each room sequentially to maintain state consistency.
     */
    @KafkaListener(
            topics = "${spring.kafka.topics.player-events}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "playerEventKafkaListenerContainerFactory"
    )
    public void processPlayerEvents(
            @Payload List<PlayerEvent> events,
            @Header(KafkaHeaders.RECEIVED_KEY) List<String> keys,
            Acknowledgment acknowledgment) {
        
        logger.debug("Received batch of {} player events", events.size());

        try {
            // Process events grouped by roomId to maintain consistency
            for (int i = 0; i < events.size(); i++) {
                PlayerEvent event = events.get(i);
                String roomId = event.getRoomId();

                try {
                    // Load current state from Redis
                    GameState currentState = gameStateRepository.getGameState(roomId);
                    
                    // Apply game logic
                    GameState updatedState = gameLogic.apply(currentState, event);
                    
                    // Save updated state to Redis
                    gameStateRepository.saveGameState(roomId, updatedState);
                    
                    // Publish state update to Kafka
                    stateUpdatePublisher.publishStateUpdate(roomId, updatedState);
                    
                    logger.debug("Processed event: {} for room: {}", 
                            event.getActionType(), roomId);
                    
                } catch (Exception e) {
                    logger.error("Error processing event for room: {} - Event: {}", 
                            roomId, event, e);
                    // Continue processing other events even if one fails
                    // Failed event will be retried or sent to DLQ by error handler
                }
            }

            // Acknowledge all messages in the batch
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
                logger.debug("Acknowledged batch of {} events", events.size());
            }

        } catch (Exception e) {
            logger.error("Fatal error processing event batch", e);
            // Don't acknowledge - messages will be retried or sent to DLQ
            throw e;
        }
    }
}


