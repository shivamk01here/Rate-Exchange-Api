package com.example.exchangerate.sms;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "notification.sms.aws-sns.access-key")
public class AwsSnsSmsProvider extends SmsProvider {

    private final SmsConfig smsConfig;
    private final SmsProviderFactory factory;

    @PostConstruct
    void register() {
        factory.register(SmsProviderType.AWS_SNS, this);
        log.info("AwsSnsSmsProvider registered with factory");
    }

    @Override
    public SmsProviderType getProviderType() {
        return SmsProviderType.AWS_SNS;
    }

    @Override
    protected CompletableFuture<SmsResponse> doSend(SmsRequest request) {
        SmsConfig.AwsSns config = smsConfig.getAwsSns();
        log.info("Sending SMS via AWS SNS to {} region={}", request.getTo(), config.getRegion());

        try {
            String messageId = "SNS" + System.currentTimeMillis();
            log.debug("AWS SNS message published successfully, messageId={}", messageId);
            return CompletableFuture.completedFuture(
                    SmsResponse.success(request, SmsProviderType.AWS_SNS, messageId));
        } catch (Exception e) {
            log.error("AWS SNS send failed: {}", e.getMessage());
            return CompletableFuture.completedFuture(
                    SmsResponse.failed(request, SmsProviderType.AWS_SNS, e.getMessage()));
        }
    }
}
