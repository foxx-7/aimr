package com.aimr.notify.pubsub.channel.email.service.impl;

import com.aimr.notify.config.ApplicationProperties;
import com.aimr.notify.model.dto.ChannelDispatchDTO;
import com.aimr.notify.pubsub.channel.email.service.interfaces.EmailDeliveryService;
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

    @Qualifier("emailWorkerExecutor")
    private final Executor emailWorkerExecutor;
    private final ApplicationProperties properties;

    @KafkaListener(
            topics = "${kafka.email.topic}",
            groupId = "email-worker-group",
            concurrency = "3",
            containerFactory = "emailListenerFactory"
    )
    public void consume(List<ChannelDispatchDTO> dispatches, Acknowledgment ack) {
        log.info("[EmailWorker] Batch received — {} messages", dispatches.size());

        List<CompletableFuture<Void>> futures = dispatches.stream()
                .map(dispatch -> CompletableFuture.runAsync(
                        () -> emailDeliveryService.process(dispatch, 0, true),
                        emailWorkerExecutor)
                )
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .exceptionally(ex -> {
                    log.error("[EmailDeliveryWorker] Unexpected batch-level failure", ex);
                    return null; // absorb inorder for join() doesn't rethrow
                })
                .join();
        ack.acknowledge();
    }
}