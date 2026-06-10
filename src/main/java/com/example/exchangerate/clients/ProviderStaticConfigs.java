package com.example.exchangerate.clients;

import com.example.exchangerate.models.ProviderCodes;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@ConfigurationProperties("providers")
public class ProviderStaticConfigs {

    Map<ProviderCodes, Map<String, String>> configs;
}