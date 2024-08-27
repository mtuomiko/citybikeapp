package com.mtuomiko.citybikeapp

import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ActiveProfiles

@Configuration
@ActiveProfiles(profiles = ["test"])
class TestConfig
