package com.icfi.aem.contentmask.sling.impl;

import com.icfi.aem.contentmask.sling.MaskedResource;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.servlets.post.AbstractPostOperation;
import org.apache.sling.servlets.post.Modification;
import org.apache.sling.servlets.post.PostResponse;

import javax.jcr.RepositoryException;
import java.util.List;

@Service
@Component(immediate = true)
@Properties(
    @Property(name = SlingPostRevertOperation.PROP_OPERATION_NAME, value = "revert-mask")
)
public class SlingPostRevertOperation extends AbstractPostOperation {

    @Override
    protected void doRun(SlingHttpServletRequest request, PostResponse response, List<Modification> changes)
        throws RepositoryException {

        String path = getItemPath(request);
        ResourceResolver resolver = request.getResourceResolver();
        Resource resource = resolver.resolve(path);
        if (resource instanceof MaskedResource) {
            MaskedResource maskedResource = (MaskedResource) resource;
            maskedResource.revert();
        }
    }
}
