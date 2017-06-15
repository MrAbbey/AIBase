package com.ai.base.SourceManager.Multiple;

import com.ai.base.SourceManager.config.ServerConfig;
import com.ai.base.SourceManager.config.ServerDataConfig;
import com.ai.base.SourceManager.config.ServerPageConfig;

import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by baggio on 2017/6/12.
 */

public class MultipleAppConfig {
    private String requestHost;
    private String requestPath;
    private String requestServlet;
    private String appPath;
    private ServerPageConfig serverPageConfig;
    private ServerDataConfig serverDataConfig;
    private ServerConfig serverConfig;
    private RSAPublicKey publicKey;
    private Map<String, String> defineConfig = new HashMap();

    public MultipleAppConfig(String requestHost, String requestPath, String requestServlet, String appPath) {
        this.requestHost = requestHost;
        this.requestPath = requestPath;
        this.requestServlet = requestServlet;
        this.appPath = appPath;
    }

    public String getRequestHost() {
        return this.requestHost;
    }

    public void setRequestHost(String requestHost) {
        this.requestHost = requestHost;
    }

    public String getRequestPath() {
        return this.requestPath;
    }

    public void setRequestPath(String requestPath) {
        this.requestPath = requestPath;
    }

    public String getRequestServlet() {
        return this.requestServlet;
    }

    public void setRequestServlet(String requestServlet) {
        this.requestServlet = requestServlet;
    }

    public String getAppPath() {
        return this.appPath;
    }

    public void setAppPath(String appPath) {
        this.appPath = appPath;
    }

    public ServerPageConfig getServerPageConfig() {
        return this.serverPageConfig;
    }

    public void setServerPageConfig(ServerPageConfig serverPageConfig) {
        this.serverPageConfig = serverPageConfig;
    }

    public ServerDataConfig getServerDataConfig() {
        return this.serverDataConfig;
    }

    public void setServerDataConfig(ServerDataConfig serverDataConfig) {
        this.serverDataConfig = serverDataConfig;
    }

    public ServerConfig getServerConfig() {
        return this.serverConfig;
    }

    public void setServerConfig(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    public RSAPublicKey getPublicKey() {
        return this.publicKey;
    }

    public void setPublicKey(RSAPublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public void putDefine(String key, String value) {
        this.defineConfig.put(key, value);
    }

    public String getDefine(String key) {
        return (String)this.defineConfig.get(key);
    }
}
