package com.icfi.aem.contentmask.domain.impl;

import com.icfi.aem.contentmask.domain.MaskConfig;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.adapter.AdapterFactory;
import org.apache.sling.api.resource.Resource;

@Service
@Component(immediate = true)
public class JcrMaskConfigAdapterFactory implements AdapterFactory {

    @Property(name = AdapterFactory.ADAPTER_CLASSES)
    public static final String[] ADAPTER_CLASSES = {
        MaskConfig.class.getName()
    };

    @Property(name = AdapterFactory.ADAPTABLE_CLASSES)
    public static final String[] ADAPTABLE_CLASSES = {
        Resource.class.getName()
    };

    @Override
    public <AdapterType> AdapterType getAdapter(Object adaptable, Class<AdapterType> type) {
        if (adaptable instanceof Resource && MaskConfig.class.isAssignableFrom(type)) {
            return (AdapterType) new JcrMaskConfig((Resource) adaptable);
        }
        return null;
    }

}
