package com.icfi.aem.contentmask.util;

import com.icfi.aem.contentmask.constants.JcrNames;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import java.util.Arrays;

public final class MaskUtil {

    private static final String ROOT_PREFIX = "/" + JcrNames.ROOT + "/";

    public static String getMaskContentPath(String maskType, String maskName) {
        return ROOT_PREFIX + maskType + "/" + maskName;
    }

    public static boolean isDeleted(Resource r, String rootPath) {
        String resourceName = r.getName();
        Resource resourceParent = r.getParent();
        while (resourceParent != null && !resourceParent.getPath().equals(rootPath)) {
            ValueMap valueMap = resourceParent.getValueMap();
            String[] hiddenChildren = valueMap.get(JcrNames.HIDE_CHILDREN, String[].class);
            if (hiddenChildren != null && Arrays.asList(hiddenChildren).contains(resourceName)) {
                return true;
            }
            resourceName = resourceParent.getName();
            resourceParent = resourceParent.getParent();
        }
        return false;
    }

    private MaskUtil() { }
}
