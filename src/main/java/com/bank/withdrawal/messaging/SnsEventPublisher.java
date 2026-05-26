package com.bank.withdrawal.messaging;

import com.bank.withdrawal.model.WithdrawalEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

@Component
@RequiredArgsConstructor
@Slf4j
public class SnsEventPublisher implements EventPublisher {

    private final SnsClient snsClient;
    private final ObjectMapper objectMapper;

    @Value("${aws.sns.topic-arn}")
    private String topicArn;

    @Override
    @Retryable(
            value = Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 500, multiplier = 2)
    )
    public void publishWithRetry(WithdrawalEvent event, String correlationId) {

        try {
            String payload = objectMapper.writeValueAsString(event);

            snsClient.publish(
                    PublishRequest.builder()
                            .topicArn(topicArn)
                            .message(payload)
                            .build()
            );

            log.info("EVENT_PUBLISHED correlationId={} accountId={}",
                    correlationId, event.accountId());

        } catch (Exception ex) {

            log.warn("EVENT_RETRY_TRIGGER correlationId={} accountId={}",
                    correlationId, event.accountId());

            throw new RuntimeException(ex);
        }
    }
}