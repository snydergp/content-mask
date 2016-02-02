package com.icfi.aem.contentmask.runtime.util;

import com.icfi.aem.contentmask.constants.JcrNames;
import com.icfi.aem.contentmask.domain.MaskConfig;
import org.apache.commons.lang3.StringUtils;

public final class PathUtil {

    private static final String ROOT_PREFIX = "/" + JcrNames.ROOT + "/";

    public static String getDataPath(String requestPath, MaskConfig config) {
        if (!requestPath.startsWith(config.getContentPath())) {
            throw new IllegalStateException();
        }
        String relPath = StringUtils.removeStart(requestPath, config.getContentPath());
        return config.getDataPath() + relPath;
    }

    public static String getContentPath(String requestPath, MaskConfig config) {
        if (!requestPath.startsWith(config.getContentPath())) {
            throw new IllegalStateException();
        }
        String relPath = StringUtils.removeStart(requestPath, config.getContentPath());
        return config.getStoragePath() + relPath;
    }

    public static String getRelativePath(String requestPath, MaskConfig config) {
        if (!requestPath.startsWith(config.getContentPath())) {
            throw new IllegalStateException();
        }
        return StringUtils.removeStart(requestPath, config.getContentPath());
    }

    public static String getMaskType(String requestPath) {
        if (StringUtils.startsWith(requestPath, ROOT_PREFIX)) {
            String remainder = StringUtils.removeStart(requestPath, ROOT_PREFIX);
            int index = StringUtils.indexOf(remainder, '/');
            return StringUtils.substring(remainder, 0, index);
        }
        return null;
    }

    public static String getMaskName(String requestPath) {
        if (StringUtils.startsWith(requestPath, ROOT_PREFIX)) {
            String remainder = StringUtils.removeStart(requestPath, ROOT_PREFIX);
            int start = StringUtils.indexOf(remainder, '/');
            int end = StringUtils.ordinalIndexOf(remainder, "/", 2);
            if (end == -1) {
                return StringUtils.substring(remainder, start + 1);
            }
            return StringUtils.substring(remainder, start + 1, end);
        }
        return null;
    }

    private PathUtil() { }
}
