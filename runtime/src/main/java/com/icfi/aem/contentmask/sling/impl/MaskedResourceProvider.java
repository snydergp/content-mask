package com.icfi.aem.contentmask.sling.impl;

import com.icfi.aem.contentmask.domain.MaskConfig;
import com.icfi.aem.contentmask.provider.MaskConfigProvider;
import com.icfi.aem.contentmask.sling.MaskedResource;
import com.icfi.aem.contentmask.runtime.util.PathUtil;
import com.icfi.aem.contentmask.util.MaskUtil;
import org.apache.sling.api.resource.ModifyingResourceProvider;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceProvider;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MaskedResourceProvider implements ResourceProvider, ModifyingResourceProvider {

    private final List<MaskConfigProvider> repositories;

    public MaskedResourceProvider(List<MaskConfigProvider> repositories) {
        this.repositories = repositories;
    }

    @Override
    public MaskedResource getResource(ResourceResolver resolver, HttpServletRequest request, String path) {
        return getResource(resolver, path);
    }

    @Override
    public MaskedResource getResource(ResourceResolver resolver, String path) {
        final MaskConfig config = findConfig(resolver, path);
        if (config == null) {
            return null;
        }
        String dataPath = PathUtil.getDataPath(path, config);
        Resource data = resolver.resolve(dataPath);
        String resolvedPath = data.getResourceMetadata().getResolutionPath();
        if (!ResourceUtil.isNonExistingResource(data) && !dataPath.equals(resolvedPath)) {
            // The delegated resource provider has found a match at a different path (e.g., "/a/b/c.selector.extension"
            // was resolved to "a/b/c"). Force retry of resolution.
            return null;
        }
        String storagePath = PathUtil.getContentPath(path, config);
        Resource content = resolver.resolve(storagePath);
        resolvedPath = content.getResourceMetadata().getResolutionPath();
        if (!ResourceUtil.isNonExistingResource(content) && !storagePath.equals(resolvedPath)) {
            // See comment above
            return null;
        }
        if (MaskUtil.isDeleted(content, config.getStoragePath())) {
            // The storage path was resolved successfully, but it marks the merged resource for deletion
            return null;
        }
        return new MaskedResourceImpl(config, PathUtil.getRelativePath(path, config), data, content);
    }

    @Override
    public Iterator<Resource> listChildren(Resource parent) {
        return parent.listChildren();
    }

    private MaskConfig findConfig(ResourceResolver resolver, String path) {
        String type = PathUtil.getMaskType(path);
        String name = PathUtil.getMaskName(path);
        if (type != null && name != null) {
            for (MaskConfigProvider repository: repositories) {
                MaskConfig config = repository.getConfig(resolver, type, name);
                if (config != null) {
                    return config;
                }
            }
        }
        return null;
    }

    @Override
    public Resource create(ResourceResolver resolver, String path, Map<String, Object> properties)
        throws PersistenceException {
        return null;
        // TODO not used by Sling POST, but might be needed by other resource creation methods
    }

    @Override
    public void delete(ResourceResolver resolver, String path) throws PersistenceException {
        MaskedResource resource = getResource(resolver, path);
        if (resource != null) {
            Resource storage = resource.getStorage();
            if (!ResourceUtil.isNonExistingResource(storage)) {
                resolver.delete(storage);
                MaskedResource parent = getResource(resolver, ResourceUtil.getParent(path));
                if (parent != null) {
                    parent.getValueMap().setHiddenChild(resource.getName(), true);
                }
            }
        }
    }

    @Override
    public void revert(ResourceResolver resolver) {
        // Any changes are held by delegated resource providers
    }

    @Override
    public void commit(ResourceResolver resolver) throws PersistenceException {
        // Any changes are held by delegated resource providers
    }

    @Override
    public boolean hasChanges(ResourceResolver resolver) {
        // Any changes are held by delegated resource providers
        return false;
    }
}
