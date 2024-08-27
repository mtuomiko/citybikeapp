package com.mtuomiko.citybikeapp.api

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(ApiConfig::class)
class ApiConfigProvider
