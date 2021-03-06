package com.roc.malltiny.security.component;

import org.springframework.security.access.ConfigAttribute;

import java.util.Map;

/**
 * 动态权限相关业务类
 */
public interface DynamicSecurityService {
    Map<String, ConfigAttribute> loadDataSource();
}
