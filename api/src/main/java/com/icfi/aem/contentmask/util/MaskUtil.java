package com.icfi.aem.contentmask.util;

import com.icfi.aem.contentmask.constants.JcrNames;

public final class MaskUtil {

    private static final String ROOT_PREFIX = "/" + JcrNames.ROOT + "/";

    public static String getMaskContentPath(String maskType, String maskName) {
        return ROOT_PREFIX + maskType + "/" + maskName;
    }

    private MaskUtil() { }
}
