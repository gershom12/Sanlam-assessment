package com.bank.withdrawal.messaging;

import com.bank.withdrawal.model.WithdrawalEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

@Component
@RequiredArgsConstructor
@Slf4j
public class SnsEventPublisher {

    private final SnsClient snsClient;
    private final ObjectMapper objectMapper;

    @Value("${aws.sns.topic-arn}")
    private String topicArn;

    public void publish(WithdrawalEvent event) {

        try {
            String payload = objectMapper.writeValueAsString(event);

            snsClient.publish(
                    PublishRequest.builder()
                            .topicArn(topicArn)
                            .message(payload)
                            .build()
            );

            log.info(
                    "EVENT_PUBLISHED accountId={} amount={}",
                    event.accountId(),
                    event.amount()
            );

        } catch (Exception ex) {

            log.error(
                    "EVENT_PUBLISH_FAILED accountId={}",
                    event.accountId(),
                    ex
            );
        }
    }
}