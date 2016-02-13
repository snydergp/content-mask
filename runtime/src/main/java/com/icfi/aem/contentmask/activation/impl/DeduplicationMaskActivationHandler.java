package com.icfi.aem.contentmask.activation.impl;

import com.icfi.aem.contentmask.activation.MaskActivationHandler;
import com.icfi.aem.contentmask.constants.JcrNames;
import com.icfi.aem.contentmask.sling.MaskedResource;
import com.icfi.aem.contentmask.sling.MaskingValueMap;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;

import java.util.HashSet;
import java.util.Objects;

/**
 * An example MaskActivationHandler implementation that removes duplicate properties on activation
 */
@Service
@Component
public class DeduplicationMaskActivationHandler implements MaskActivationHandler {

    @Override
    public boolean handleActivation(MaskedResource resource) {
        if (resource.getStorage().getValueMap().containsKey(JcrNames.REMOVE_DUPLICATION)) {
            return doHandleActivation(resource);
        }
        return false;
    }

    private boolean doHandleActivation(MaskedResource resource) {
        MaskingValueMap map = resource.getValueMap();
        boolean modified = false;
        for (String key: new HashSet<>(map.keySet())) {
            if (Objects.equals(map.getData(key), map.getStorage(key))) {
                modified = (map.remove(key) != null) || modified;
                map.setHidden(key, false);
            }
        }
        for (Resource child: resource.getChildren()) {
            if (child instanceof MaskedResource) {
                modified = doHandleActivation((MaskedResource) child) || modified;
            }
        }
        return modified;
    }

}
