package com.example.exchangerate.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BatchConfigTest {

    @Test
    void batchConfig_hasDefaultValues() {
        BatchConfig config = new BatchConfig();
        assertEquals(10, config.getMaxSize());
    }

    @Test
    void batchConfig_setAndGetMaxSize() {
        BatchConfig config = new BatchConfig();
        config.setMaxSize(25);
        assertEquals(25, config.getMaxSize());
    }
}
