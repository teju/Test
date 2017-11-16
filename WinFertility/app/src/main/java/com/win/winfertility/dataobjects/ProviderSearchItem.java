package com.win.winfertility.dataobjects;

public class ProviderSearchItem {
    public String LinkName = "";
    public String ProviderSearchLink = "";

    public String getProviderLogo() {
        return ProviderLogo;
    }

    public void setProviderLogo(String providerLogo) {
        ProviderLogo = providerLogo;
    }

    public String ProviderLogo = "";

    @Override
    public String toString() {
        return this.LinkName;
    }
}
