package com.icfi.aem.contentmask.domain;

import com.icfi.aem.contentmask.util.MaskUtil;

public abstract class AbstractMaskConfig implements MaskConfig {

    @Override
    public final String getContentPath() {
        return MaskUtil.getMaskContentPath(getMaskType(), getMaskName());
    }
}
