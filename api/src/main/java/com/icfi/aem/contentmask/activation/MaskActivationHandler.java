package com.icfi.aem.contentmask.activation;

import com.icfi.aem.contentmask.sling.MaskedResource;

public interface MaskActivationHandler {

    /**
     * Called when the provided resource has been activated.  The handler can use this opportunity to move data from the
     * storage resource to the data resource, as needed.  This can be used to simulate the effect of page publishing
     * in external systems that don't support an author/publish mechanism by keeping data in the JCR before activation.
     * If any changes are made, the handler should return true to indicate that a save is needed.
     * @param resource the resource being activated
     * @return true, if changes were made
     */
    boolean handleActivation(MaskedResource resource);

}
