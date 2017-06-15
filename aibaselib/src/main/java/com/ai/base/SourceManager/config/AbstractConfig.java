package com.ai.base.SourceManager.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by baggio on 2017/6/12.
 */

public abstract class AbstractConfig {
    protected Map<String, ?> configsMap = new HashMap();

    protected AbstractConfig() {
    }

    protected Map<String, ?> getConfigMap() throws Exception {
        if(this.configsMap.isEmpty()) {
            this.configsMap = this.loadConfig();
        }

        return this.configsMap;
    }

    protected abstract Map<String, ?> loadConfig() throws Exception;
}
