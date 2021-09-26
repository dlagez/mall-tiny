package com.roc.malltiny.config;

import com.roc.malltiny.common.config.BaseRedisConfig;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;


@EnableCaching
@Configuration
public class RedisConfig extends BaseRedisConfig {

}
