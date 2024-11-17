package com.mtuomiko.citybikeapp

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@EnableConfigurationProperties(DataLoaderConfig::class)
@Profile("dataloader")
class DataLoaderConfigProvider
