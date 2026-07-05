package com.example.exchangerate.sms;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SmsProviderFactoryTest {

    private SmsProviderFactory factory;

    @BeforeEach
    void setUp() {
        factory = new SmsProviderFactory();
    }

    @Test
    void shouldRegisterAndRetrieveProvider() {
        ConsoleSmsProvider provider = new ConsoleSmsProvider(factory);
        provider.register();

        SmsProvider retrieved = factory.getProvider(SmsProviderType.CONSOLE);
        assertThat(retrieved).isSameAs(provider);
    }

    @Test
    void shouldThrowWhenRegisteringDuplicateType() {
        ConsoleSmsProvider first = new ConsoleSmsProvider(factory);
        first.register();

        ConsoleSmsProvider second = new ConsoleSmsProvider(factory);
        assertThatThrownBy(second::register)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already registered");
    }

    @Test
    void shouldThrowWhenRetrievingUnregisteredType() {
        assertThatThrownBy(() -> factory.getProvider(SmsProviderType.TWILIO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No SMS provider registered");
    }
}
