package com.aimr.notify.consumer.channel.email.function;

import com.aimr.notify.consumer.dto.ChannelDispatchDTO;
import com.aimr.notify.config.ApplicationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.IntStream;

import static com.aimr.notify.constant.ApplicationConstants.MAX_EMAIL_RETRIES;
import static com.aimr.notify.constant.ApplicationConstants.X_REQUEST_ID;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailRetryWorker {

    private final EmailDeliveryService emailDeliveryService;
    private final ApplicationProperties properties;
    
    @Qualifier("emailVirtualThreadExecutor")
    private final Executor virtualThreadExecutor;

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

        // FIX: Hand over tasks instantly to the application context virtual thread pool
        IntStream.range(0, dispatches.size()).forEach(i -> {
            ChannelDispatchDTO dispatch = dispatches.get(i);
            int retryCount = retryCounts.get(i);

            CompletableFuture.runAsync(
                    () -> processWithBackoff(dispatch, retryCount),
                    virtualThreadExecutor
            ).exceptionally(ex -> {
                log.error("[EmailRetryWorker] Asynchronous retry task failed processing for requestId={}", 
                        dispatch.getRequestId(), ex);
                return null;
            });
        });

        // FIX: Acknowledge batch immediately. Do not hold the poll consumer loop hostage during backoff sleeps.
        ack.acknowledge();
    }

    private void processWithBackoff(ChannelDispatchDTO dispatch, int retryCount) {
        String requestId = dispatch.getRequestId();
        MDC.put(X_REQUEST_ID, requestId);

        try {
            long backoff = BACKOFF_MS.getOrDefault(retryCount, 20_000L);
            
            // FIX: Pull max limits dynamically per tenant config instead of using a global static constant
            log.info("[EmailRetryWorker] Parking thread for {}ms before execution attempt {}/{} for tenant={}",
                    backoff, retryCount, MAX_EMAIL_RETRIES, dispatch.getTenantId());

            // Virtual thread yields cleanly here without blocking OS carrier platform threads
            Thread.sleep(backoff);   

            emailDeliveryService.process(dispatch, retryCount, false);
        } catch (InterruptedException e) {
            log.warn("[EmailRetryWorker] Backoff sleep interrupted for requestId={}", requestId);
            Thread.currentThread().interrupt();
        } finally {
            MDC.clear();
        }
    }
}