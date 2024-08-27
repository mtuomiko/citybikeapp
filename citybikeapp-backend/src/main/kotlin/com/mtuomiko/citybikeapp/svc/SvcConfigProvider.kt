package com.mtuomiko.citybikeapp.svc

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(SvcConfig::class)
class SvcConfigProvider
