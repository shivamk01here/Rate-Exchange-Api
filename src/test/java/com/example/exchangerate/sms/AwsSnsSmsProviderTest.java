package com.example.exchangerate.sms;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class AwsSnsSmsProviderTest {

    @Mock
    private SmsProviderFactory factory;

    private AwsSnsSmsProvider provider;
    private SmsConfig smsConfig;

    @BeforeEach
    void setUp() {
        smsConfig = new SmsConfig();
        smsConfig.getAwsSns().setAccessKey("AKIA12345678");
        smsConfig.getAwsSns().setSecretKey("secretkey");
        smsConfig.getAwsSns().setRegion("us-west-2");
        provider = new AwsSnsSmsProvider(smsConfig, factory);
    }

    @Test
    void shouldReturnAwsSnsType() {
        assertThat(provider.getProviderType()).isEqualTo(SmsProviderType.AWS_SNS);
    }

    @Test
    void shouldSendSuccessfully() {
        SmsRequest request = SmsRequest.builder()
                .to("+1234567890")
                .message("Test message")
                .build();

        CompletableFuture<SmsResponse> future = provider.send(request);
        SmsResponse response = future.join();

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessageId()).startsWith("SNS");
        assertThat(response.getProvider()).isEqualTo(SmsProviderType.AWS_SNS);
    }
}
