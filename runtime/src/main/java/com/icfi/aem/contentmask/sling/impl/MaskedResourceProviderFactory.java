package com.icfi.aem.contentmask.sling.impl;

import com.icfi.aem.contentmask.provider.MaskConfigProvider;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceProvider;
import org.apache.sling.api.resource.ResourceProviderFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Component(immediate = true)
@Properties({
    @Property(name = ResourceProvider.ROOTS, value = "/mask:root"),
    @Property(name = ResourceProvider.OWNS_ROOTS, boolValue = true)
})
public class MaskedResourceProviderFactory implements ResourceProviderFactory {

    @Reference(cardinality = ReferenceCardinality.MANDATORY_MULTIPLE, policy = ReferencePolicy.DYNAMIC,
        bind = "bindRepo", unbind = "unbindRepo", referenceInterface = MaskConfigProvider.class)
    private List<MaskConfigProvider> repositories = new CopyOnWriteArrayList<>();

    @Override
    public ResourceProvider getResourceProvider(Map<String, Object> authenticationInfo) throws LoginException {
        // TODO how should auth be handled?
        return new MaskedResourceProvider(Collections.unmodifiableList(repositories));
    }

    @Override
    public ResourceProvider getAdministrativeResourceProvider(Map<String, Object> authenticationInfo)
        throws LoginException {
        // TODO how should auth be handled?
        return new MaskedResourceProvider(Collections.unmodifiableList(repositories));
    }

    public void bindRepo(MaskConfigProvider repository) {
        repositories.add(repository);
    }

    public void unbindRepo(MaskConfigProvider repository) {
        repositories.remove(repository);
    }

}
