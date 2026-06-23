package com.aimr.notify.consumer.channel.email.function;

import com.aimr.notify.config.ApplicationProperties;
import com.aimr.notify.domain.context.NotificationContext;
import com.aimr.notify.domain.context.NotificationContextHolder;
import com.aimr.notify.consumer.dto.ChannelDispatchDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailDeliveryWorker {

    private final EmailDeliveryService emailDeliveryService;
    
    @Qualifier("emailVirtualThreadExecutor")
    private final Executor emailVirtualThreadExecutor;
    private final ApplicationProperties properties;

    @KafkaListener(
            topics = "${kafka.email.topic}",
            groupId = "email-worker-group",
            concurrency = "3",
            containerFactory = "emailListenerFactory"
    )
    public void consume(List<ChannelDispatchDTO> dispatches, Acknowledgment ack) {
        log.info("[EmailWorker] Batch received — {} messages", dispatches.size());

        // FIX: Extract parent thread context variables before dropping across thread pools
        NotificationContext parentContext = NotificationContextHolder.getContext();

        List<CompletableFuture<Void>> futures = dispatches.stream()
                .map(dispatch -> CompletableFuture.runAsync(() -> {
                    try {
                        // FIX: Explicitly bind the thread variables down to the active async thread
                        NotificationContextHolder.setContext(parentContext);
                        emailDeliveryService.process(dispatch, 0, true);
                    } finally {
                        // Ensure thread safety tracking cleanup
                        NotificationContextHolder.clear();
                    }
                }, emailVirtualThreadExecutor))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .exceptionally(ex -> {
                    log.error("[EmailDeliveryWorker] Unexpected batch-level failure", ex);
                    return null;
                })
                .join();
                
        ack.acknowledge();
    }
}