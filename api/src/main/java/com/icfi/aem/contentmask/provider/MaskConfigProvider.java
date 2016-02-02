package com.icfi.aem.contentmask.provider;

import com.icfi.aem.contentmask.domain.MaskConfig;
import org.apache.sling.api.resource.ResourceResolver;

/**
 * A provider for MaskConfig data.  Each config must be defined by a unique type/name pairing.  This pairing will be
 * used MaskedResourceProvider to generate a synthetic resource path for the configuration, at "/mask:root/$type/$name".
 * As such, both type and name should be valid JCR names.  When implementing a custom provider, use a distinct type to
 * 'namespace' your mask paths. Avoid using the types "uuid" and "named", which are used by the JcrMaskConfigProvider,
 * as this could lead to collisions.
 */
public interface MaskConfigProvider {

    /**
     * Returns the config matching the given type and name, if it exists
     * @param resolver The resolver to use
     * @param type The type of the config
     * @param name The name of the config
     * @return a MaskConfig matching the provided type and name, or null
     */
    MaskConfig getConfig(ResourceResolver resolver, String type, String name);

}
