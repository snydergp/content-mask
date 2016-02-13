package com.icfi.aem.contentmask.sling.impl;

import com.icfi.aem.contentmask.constants.JcrNames;
import com.icfi.aem.contentmask.domain.MaskConfig;
import com.icfi.aem.contentmask.runtime.constants.JcrProperties;
import com.icfi.aem.contentmask.sling.MaskedResource;
import com.icfi.aem.contentmask.sling.MaskingValueMap;
import org.apache.sling.api.resource.AbstractResource;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.NonExistingResource;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ModifiableValueMapDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MaskedResourceImpl extends AbstractResource implements MaskedResource {

    private static final Logger LOG = LoggerFactory.getLogger(MaskedResourceImpl.class);

    private static final String PRIMARY_TYPE = "nt:unstructured";

    private final Resource data;
    private Resource storage;
    private boolean initialized;
    private MaskingValueMapImpl cache;
    private final String path;
    private final MergedResourceMetadata metadata;


    public MaskedResourceImpl(MaskConfig config, String relPath, Resource data, Resource storage) {
        this.data = data;
        this.storage = storage;
        path = config.getContentPath() + relPath;
        metadata = new MergedResourceMetadata();
        metadata.setResolutionPath(path);
    }

    @Override
    public Resource getData() {
        return data;
    }

    @Override
    public Resource getStorage() {
        return storage;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getResourceType() {
        return ResourceUtil.isNonExistingResource(data) ? storage.getResourceType() : data.getResourceType();
    }

    @Override
    public String getResourceSuperType() {
        return ResourceUtil.isNonExistingResource(data) ? storage.getResourceSuperType() : data.getResourceSuperType();
    }

    @Override
    public ResourceMetadata getResourceMetadata() {
        return metadata;
    }

    @Override
    public ResourceResolver getResourceResolver() {
        return storage.getResourceResolver();
    }

    @Override
    public Resource getParent() {
        return data.getResourceResolver().resolve(ResourceUtil.getParent(path));
    }

    @Override
    public Resource getChild(String relPath) {
        return data.getResourceResolver().resolve(ResourceUtil.getParent(path + "/" + relPath));
    }

    @Override
    public Iterator<Resource> listChildren() {
        return getChildren().iterator();
    }

    @Override
    public Iterable<Resource> getChildren() {
        Set<String> names = new LinkedHashSet<String>();
        for (Resource resource : data.getChildren()) {
            names.add(resource.getName());
        }
        names.removeAll(getValueMap().getHiddenChildren());
        for (Resource resource: storage.getChildren()) {
            names.add(resource.getName());
        }
        List<Resource> children = new ArrayList<>();
        for (String name: names) {
            children.add(storage.getResourceResolver().resolve(path + "/" + name));
        }
        return children;
    }

    @Override
    public boolean hasChildren() {
        return data.hasChildren() || storage.hasChildren();
    }

    @Override
    public MaskingValueMapImpl getValueMap() {
        if (cache == null) {
            cache = new MaskingValueMapImpl();
        }
        return cache;
    }

    @Override
    public void revert() {
        if (!ResourceUtil.isNonExistingResource(storage)) {
            try {
                for (Resource child: storage.getChildren()) {
                    storage.getResourceResolver().delete(child);
                }
                boolean initializeLiveCopy = getValueMap().get(JcrProperties.INITIALIZE_LIVE_COPY, Boolean.FALSE);
                getValueMap().clear();
                if (initializeLiveCopy) {
                    getValueMap().put(JcrProperties.INITIALIZE_LIVE_COPY, Boolean.TRUE);
                }
            } catch (PersistenceException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public <AdapterType> AdapterType adaptTo(Class<AdapterType> type) {
        if (ModifiableValueMap.class.isAssignableFrom(type)) {
            return (AdapterType) getValueMap().getModifiable();
        }
        if (ValueMap.class.isAssignableFrom(type)) {
            return (AdapterType) getValueMap();
        }
        return super.adaptTo(type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        MaskedResourceImpl that = (MaskedResourceImpl) o;

        return path.equals(that.path);

    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    /**
     * Initializes the storage resource.  This is done lazily on the first write to prevent empty storage nodes from
     * being created a read-only situation.  This method creates empty "nt:unstructured" nodes from the first existing
     * ancestor up to the current storage path.
     */
    private void initializeStorage() {
        if (storage instanceof NonExistingResource) {
            Map<String, Object> props = new HashMap<>();
            props.put(JcrProperties.JCR_PRIMARY_TYPE, PRIMARY_TYPE);
            try {
                storage = ResourceUtil.getOrCreateResource(
                    storage.getResourceResolver(),
                    storage.getPath(),
                    props,
                    null,
                    false);
            } catch (PersistenceException e) {
                LOG.error("Error creating resource for Content Mask storage", e);
            }
        }
        initialized = true;
    }

    private class MaskingValueMapImpl implements MaskingValueMap {

        private final ValueMap data;
        private ValueMap storageRead = storage.getValueMap();
        private ModifiableValueMap storageWrite;

        private MaskingValueMapImpl() {
            this.data = MaskedResourceImpl.this.data.getValueMap();
        }

        @Override
        public boolean isHidden(String name) {
            if (name.startsWith(JcrNames.NS)) {
                return true;
            }
            return getHidden().contains(name);
        }

        @Override
        public List<String> getHidden() {
            String[] hideProperties = storageRead.get(JcrNames.HIDE_PROPERTIES, String[].class);
            return hideProperties == null ? new ArrayList<String>() : new ArrayList<>(Arrays.asList(hideProperties));
        }

        @Override
        public void setHidden(String name, boolean hidden) {
            List<String> list = getHidden();
            if (hidden && !list.contains(name)) {
                initWrite();
                list.add(name);
                storageWrite.put(JcrNames.HIDE_PROPERTIES, list.toArray(new String[list.size()]));
            } else {
                initWrite();
                list.remove(name);
                storageWrite.put(JcrNames.HIDE_PROPERTIES, list.toArray(new String[list.size()]));
            }
        }

        @Override
        public boolean isHiddenChild(String name) {
            if (name.startsWith(JcrNames.NS)) {
                return true;
            }
            return getHiddenChildren().contains(name);
        }

        @Override
        public List<String> getHiddenChildren() {
            String[] hideProperties = storageRead.get(JcrNames.HIDE_CHILDREN, String[].class);
            return hideProperties == null ? new ArrayList<String>() : new ArrayList<>(Arrays.asList(hideProperties));
        }

        @Override
        public void setHiddenChild(String name, boolean hidden) {
            List<String> list = getHiddenChildren();
            if (hidden && !list.contains(name)) {
                initWrite();
                list.add(name);
                storageWrite.put(JcrNames.HIDE_CHILDREN, list.toArray(new String[list.size()]));
            } else {
                initWrite();
                list.remove(name);
                storageWrite.put(JcrNames.HIDE_CHILDREN, list.toArray(new String[list.size()]));
            }
        }

        @Override
        public Object getData(String key) {
            return data.get(key);
        }

        @Override
        public Object getStorage(String key) {
            return storageRead.get(key);
        }

        @Override
        public <T> T getData(String key, Class<T> tClass) {
            return data.get(key, tClass);
        }

        @Override
        public <T> T getStorage(String key, Class<T> tClass) {
            return storageRead.get(key, tClass);
        }

        @Override
        public <T> T get(String name, Class<T> type) {
            if (storageRead.containsKey(name)) {
                return storageRead.get(name, type);
            }
            return isHidden(name) ? null : data.get(name, type);
        }

        @Override
        public <T> T get(String name, T defaultValue) {
            if (storageRead.containsKey(name)) {
                return storageRead.get(name, defaultValue);
            }
            return isHidden(name) ? null : data.get(name, defaultValue);
        }

        @Override
        public int size() {
            return keySet().size();
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean containsKey(Object key) {
            return storageRead.containsKey(key) || data.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return storageRead.containsValue(value) || data.containsValue(value);
        }

        @Override
        public Object get(Object key) {
            if (storageRead.containsKey(key)) {
                return storageRead.get(key);
            }
            return isHidden(String.valueOf(key)) ? null : data.get(key);
        }

        @Override
        public Object put(String key, Object value) {
            initWrite();
            return storageWrite.put(key, value);
        }

        @Override
        public Object remove(Object key) {
            initWrite();
            String k = String.valueOf(key);
            Node node = storage.adaptTo(Node.class);
            try {
                if (node != null && node.hasProperty(String.valueOf(key))) {
                    if (!node.getProperty(k).getDefinition().isProtected()) {
                        setHidden(String.valueOf(key), true);
                        return storageWrite.remove(key);
                    }
                }
            } catch (RepositoryException e) {
                LOG.error("Error checking property protection state", e);
            }
            return null;
        }

        @Override
        public void putAll(Map<? extends String, ?> m) {
            initWrite();
            storageWrite.putAll(m);
        }

        @Override
        public void clear() {
            initWrite();
            for (String key: keySet()) {
                remove(key);
            }
        }

        @Override
        public Set<String> keySet() {
            Set<String> keys = new HashSet<>(data.keySet());
            keys.addAll(storageRead.keySet());
            keys.removeAll(getHidden());
            Iterator<String> iterator = keys.iterator();
            while (iterator.hasNext()) {
                if (iterator.next().startsWith(JcrNames.NS)) {
                    iterator.remove();
                }
            }
            return keys;
        }

        @Override
        public Collection<Object> values() {
            List<Object> out = new ArrayList<>();
            for (String key: keySet()) {
                out.add(get(key));
            }
            return out;
        }

        @Override
        public Set<Entry<String, Object>> entrySet() {

            Set<Entry<String, Object>> out = new HashSet<>();
            for (final String key: keySet()) {
                final Object value = get(key);
                out.add(new Entry<String, Object>() {
                    @Override
                    public String getKey() {
                        return key;
                    }

                    @Override
                    public Object getValue() {
                        return value;
                    }

                    @Override
                    public Object setValue(Object value) {
                        throw new UnsupportedOperationException();
                    }
                });
            }
            return out;
        }

        /**
         * Initializes the storage for this value map's resource (if it hasn't already been initialized)
         */
        private void initWrite() {
            if (storageWrite == null) {
                if (!initialized) {
                    initializeStorage();
                }
                storageWrite = MaskedResourceImpl.this.storage.adaptTo(ModifiableValueMap.class);
                storageRead = storageWrite;
            }
        }

        private ModifiableValueMap getModifiable() {
            return new ModifiableValueMapDecorator(this);
        }

    }

    private static class MergedResourceMetadata extends ResourceMetadata {
        @Override
        public void lock() {

        }
    }


}
