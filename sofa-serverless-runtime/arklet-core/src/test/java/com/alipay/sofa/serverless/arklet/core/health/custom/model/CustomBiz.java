package com.alipay.sofa.serverless.arklet.core.health.custom.model;

import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.BizState;

import java.net.URL;
import java.util.Map;
import java.util.Set;

public class CustomBiz implements Biz {

    private final String bizName;
    private final String bizVersion;

    public CustomBiz(String bizName, String bizVersion) {
        this.bizName = bizName;
        this.bizVersion = bizVersion;
    }

    @Override
    public void start(String[] strings) throws Throwable {

    }

    @Override
    public void stop() throws Throwable {

    }

    @Override
    public boolean isDeclared(URL url, String s) {
        return true;
    }

    @Override
    public boolean isDeclaredMode() {
        return true;
    }

    @Override
    public void setCustomBizName(String s) {

    }

    @Override
    public String getBizName() {
        return this.bizName;
    }

    @Override
    public String getBizVersion() {
        return bizVersion;
    }

    @Override
    public String getIdentity() {
        return null;
    }

    @Override
    public String getMainClass() {
        return null;
    }

    @Override
    public URL[] getClassPath() {
        return new URL[0];
    }

    @Override
    public Set<String> getDenyImportPackages() {
        return null;
    }

    @Override
    public Set<String> getDenyImportPackageNodes() {
        return null;
    }

    @Override
    public Set<String> getDenyImportPackageStems() {
        return null;
    }

    @Override
    public Set<String> getDenyImportClasses() {
        return null;
    }

    @Override
    public Set<String> getDenyImportResources() {
        return null;
    }

    @Override
    public Set<String> getDenyPrefixImportResourceStems() {
        return null;
    }

    @Override
    public Set<String> getDenySuffixImportResourceStems() {
        return null;
    }

    @Override
    public ClassLoader getBizClassLoader() {
        return null;
    }

    @Override
    public BizState getBizState() {
        return null;
    }

    @Override
    public String getWebContextPath() {
        return null;
    }

    @Override
    public Map<String, String> getAttributes() {
        return null;
    }

    @Override
    public int getPriority() {
        return 0;
    }
}
