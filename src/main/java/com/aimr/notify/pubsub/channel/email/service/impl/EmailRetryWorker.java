package com.aimr.notify.pubsub.channel.email.service.impl;

import com.aimr.notify.model.dto.ChannelDispatchDTO;
import com.aimr.notify.pubsub.channel.email.service.interfaces.EmailDeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static com.aimr.notify.constant.ApplicationConstants.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailRetryWorker {

    private final EmailDeliveryService emailDeliveryService;

    // Backoff per attempt: 1→5s, 2→10s, 3→20s
    private static final Map<Integer, Long> BACKOFF_MS = Map.of(
            1, 5_000L,
            2, 10_000L,
            3, 20_000L
    );

    @KafkaListener(
            topics = "${kafka.email.retry.topic}",
            groupId = "${kafka.email.retry.group}",
            concurrency = "3",
            containerFactory = "emailListenerFactory"
    )
    public void consume(
            List<ChannelDispatchDTO> dispatches,
            @Header("RETRY_COUNT") List<Integer> retryCounts,
            Acknowledgment ack) {

        List<CompletableFuture<Void>> futures = IntStream.range(0, dispatches.size())
                .mapToObj(i -> CompletableFuture.runAsync(
                        () -> processWithBackoff(dispatches.get(i), retryCounts.get(i)),
                        Executors.newVirtualThreadPerTaskExecutor())
                )
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .exceptionally(ex -> {
                    log.error("[EmailRetryWorker] Unexpected batch-level failure", ex);
                    return null;
                })
                .join();
        ack.acknowledge();
    }

    private void processWithBackoff(ChannelDispatchDTO dispatch, int retryCount) {

        /*
            NOTE -> the  'getKeyIfPresent()' function actually creates a new
            idempotency key for the dispatch if not found
         */
            long backoff = BACKOFF_MS.getOrDefault(retryCount, 20_000L);
            log.info("[EmailRetryWorker] Waiting {}ms before retry {}/{} requestId={}",
                    backoff, retryCount, MAX_EMAIL_RETRIES, dispatch.getRequestId());

            try {
                Thread.sleep(backoff);   // virtual thread parks cheaply here
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

                emailDeliveryService.process(dispatch, retryCount, false);
    }
}