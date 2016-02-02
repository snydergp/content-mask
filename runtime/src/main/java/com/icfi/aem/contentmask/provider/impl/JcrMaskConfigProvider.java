package com.icfi.aem.contentmask.provider.impl;

import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.SearchResult;
import com.icfi.aem.contentmask.runtime.constants.JcrProperties;
import com.icfi.aem.contentmask.runtime.constants.MaskTypes;
import com.icfi.aem.contentmask.runtime.constants.NodeTypes;
import com.icfi.aem.contentmask.domain.MaskConfig;
import com.icfi.aem.contentmask.provider.MaskConfigProvider;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;
import java.util.HashMap;
import java.util.Map;

@Service
@Component(immediate = true)
public class JcrMaskConfigProvider implements MaskConfigProvider {

    private static final int JCR_UUID_LENGTH = 36;

    private static final Logger LOG = LoggerFactory.getLogger(JcrMaskConfigProvider.class);

    @Reference
    private QueryBuilder queryBuilder;

    @Override
    public MaskConfig getConfig(ResourceResolver resolver, String type, String name) {
        if (MaskTypes.NAMED.equals(type) || MaskTypes.UUID.equals(type)) {
            if (MaskTypes.UUID.equals(type) && name.length() != JCR_UUID_LENGTH) {
                return null;
            }
            Map<String, String> props = new HashMap<>();
            props.put("p.limit", "1");
            props.put("type", MaskTypes.NAMED.equals(type) ? NodeTypes.NAMED_CONFIG : NodeTypes.CONFIG);
            props.put("property", MaskTypes.NAMED.equals(type) ? JcrProperties.NAME : JcrProperties.JCR_UUID);
            props.put("property.value", name);
            Query query = queryBuilder.createQuery(PredicateGroup.create(props), resolver.adaptTo(Session.class));
            SearchResult result = query.getResult();
            Resource resource = result.getResources().hasNext() ? result.getResources().next() : null;
            if (resource != null) {
                return resource.adaptTo(MaskConfig.class);
            }
        }
        LOG.info("Requested MaskConfig {}/{} not found", type, name);
        return null;
    }
}
