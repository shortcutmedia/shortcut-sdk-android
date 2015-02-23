/*
 * Copyright (c) 2015 Shortcut Media AG - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.scm.reader.livescanner.config;

public class SDKConfig {
    public static final String SDK_UA_NAME = "ShortcutSDK";
    public static final int SDK_VERSION_CODE = 1;

    public static final String sdkUASignature() {
        return String.format("%s/%s", SDKConfig.SDK_UA_NAME, SDKConfig.SDK_VERSION_CODE);
    }
}
