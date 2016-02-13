package com.icfi.aem.contentmask.sling.impl;

import com.day.cq.replication.ReplicationAction;
import com.icfi.aem.contentmask.activation.MaskActivationHandler;
import com.icfi.aem.contentmask.domain.MaskConfig;
import com.icfi.aem.contentmask.provider.ActivatableMaskConfigProvider;
import com.icfi.aem.contentmask.sling.MaskedResource;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * A replication handler that delegates to MaskActivationHandlers whenever masked content is replicated
 */
@Service
@Component(immediate = true)
@Properties({
    @Property(name = EventConstants.EVENT_TOPIC, value = ReplicationAction.EVENT_TOPIC)
})
public class ActivationHandler implements EventHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ActivationHandler.class);

    @Reference
    private ResourceResolverFactory resolverFactory;

    @Reference(policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE,
        referenceInterface = MaskActivationHandler.class, bind = "bindHandler", unbind = "unbindHandler")
    private final List<MaskActivationHandler> handlers = new ArrayList<>();

    @Reference(policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE,
        referenceInterface = ActivatableMaskConfigProvider.class, bind = "bindProvider", unbind = "unbindProvider")
    private final List<ActivatableMaskConfigProvider> providers = new ArrayList<>();

    @Override
    public void handleEvent(Event event) {
        ResourceResolver resolver;
        try {
            resolver = resolverFactory.getAdministrativeResourceResolver(null);
        } catch (LoginException e) {
            throw new IllegalStateException("Error obtaining admin resolver", e);
        }
        ReplicationAction action = ReplicationAction.fromEvent(event);
        List<MaskConfig> configs = new ArrayList<>();
        for (ActivatableMaskConfigProvider provider: this.providers) {
            configs.addAll(provider.getConfigsForActivationPath(resolver, action.getPath()));
        }
        boolean modified = false;
        for (MaskConfig config: configs) {
            modified = modified || handlePath(resolver, config.getContentPath());
        }
        if (modified) {
            // TODO reactivate if changes?
        }
    }

    protected void bindHandler(MaskActivationHandler handler) {
        this.handlers.add(handler);
    }

    protected void unbindHandler(MaskActivationHandler handler) {
        this.handlers.remove(handler);
    }

    protected void bindProvider(ActivatableMaskConfigProvider provider) {
        this.providers.add(provider);
    }

    protected void unbindProvider(ActivatableMaskConfigProvider provider) {
        this.providers.remove(provider);
    }

    private boolean handlePath(ResourceResolver resolver, String path) {
        LOG.info("Handling activation for masked path {}", path);
        Resource resource = resolver.resolve(path);
        boolean modified = false;
        if (resource instanceof MaskedResource) {
            for (MaskActivationHandler handler : handlers) {
                modified = handler.handleActivation((MaskedResource) resource) || modified;
            }
            if (modified) {
                try {
                    resolver.commit();
                } catch (PersistenceException e) {
                    LOG.error("Error persisting changes", e);
                }
            }
        }
        return modified;
    }
}
