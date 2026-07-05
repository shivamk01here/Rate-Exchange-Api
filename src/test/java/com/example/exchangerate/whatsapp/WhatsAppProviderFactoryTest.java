package com.example.exchangerate.whatsapp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WhatsAppProviderFactoryTest {

    private WhatsAppProviderFactory factory;

    @BeforeEach
    void setUp() {
        factory = new WhatsAppProviderFactory();
    }

    @Test
    void shouldRegisterAndRetrieveProvider() {
        WhatsAppProvider provider = new ConsoleWhatsAppProvider(factory);
        provider.register();

        WhatsAppProvider retrieved = factory.getProvider(WhatsAppProviderType.CONSOLE);
        assertThat(retrieved).isSameAs(provider);
    }

    @Test
    void shouldThrowWhenRegisteringDuplicateType() {
        WhatsAppProvider first = new ConsoleWhatsAppProvider(factory);
        first.register();

        WhatsAppProvider second = new ConsoleWhatsAppProvider(factory);
        assertThatThrownBy(second::register)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already registered");
    }

    @Test
    void shouldThrowWhenRetrievingUnregisteredType() {
        assertThatThrownBy(() -> factory.getProvider(WhatsAppProviderType.TWILIO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No WhatsApp provider registered");
    }
}
