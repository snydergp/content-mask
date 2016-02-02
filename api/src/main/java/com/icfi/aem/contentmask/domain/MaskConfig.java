package com.icfi.aem.contentmask.domain;

import com.icfi.aem.contentmask.util.MaskUtil;

/**
 * Defines configuration for a mask.  Configurations are uniquely defined by their type and their name.  Names must be
 * unique for a given type.  The type field allows for extension via custom MaskConfigProvider implementations, without
 * the risk of name collisions.  Type and name are combined to produce the content path.
 * (See {@link MaskUtil#getMaskContentPath(String, String)}
 */
public interface MaskConfig {

    /**
     * @return The mask type, used to group content masks. Should be a valid JCR name.
     */
    String getMaskType();

    /**
     * @return The mask name, a unique identifier for a mask within the context of a particular mask type.  Should
     * be a valid JCR name.
     */
    String getMaskName();

    /**
     * @return The root path for resources to be masked
     */
    String getDataPath();

    /**
     * @return The storage path, where any local overwrites to the data path will be stored
     */
    String getStoragePath();

    /**
     * @return The content path, which contain the read/write merge of the data and storage paths.  T
     */
    String getContentPath();

}
