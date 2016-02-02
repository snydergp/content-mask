package com.icfi.aem.contentmask.sling;

import org.apache.sling.api.resource.Resource;

/**
 * A resource subclass that exposes additional functionality pertaining to resource masking
 */
public interface MaskedResource extends Resource {

    /**
     * Returns the data resource. The data resource is overshadowed by any properties contained in the storage resource.
     * The data resource is read-only from the MaskedResource interface.  All writes are pushed into the storage
     * resource.
     * @return The data resource
     */
    Resource getData();

    /**
     * Returns the storage resource.  Properties in the storage resource overshadow any corresponding property in the
     * data resource.  Properties not overshadowed can be explicitly hidden (See
     * {@link com.icfi.aem.contentmask.sling.MaskingValueMap}).
     * @return
     */
    Resource getStorage();

    MaskingValueMap getValueMap();
}
