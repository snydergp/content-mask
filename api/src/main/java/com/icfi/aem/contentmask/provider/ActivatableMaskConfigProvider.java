package com.icfi.aem.contentmask.provider;

import com.icfi.aem.contentmask.domain.MaskConfig;
import org.apache.sling.api.resource.ResourceResolver;

import java.util.List;

/**
 * A service that provides a listing of mask configs that require special handling (via MaskActivationHandlers)
 */
public interface ActivatableMaskConfigProvider {

    /**
     * Returns all MaskConfigs that require handling for the given activation path
     * @param resolver the resolver
     * @param path the path being activated
     * @return all configs that should be called against active MaskActivationHandlers
     */
    List<MaskConfig> getConfigsForActivationPath(ResourceResolver resolver, String path);

}
