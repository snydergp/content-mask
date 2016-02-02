package com.icfi.aem.contentmask.domain.impl;

import com.icfi.aem.contentmask.constants.JcrNames;
import com.icfi.aem.contentmask.runtime.constants.JcrProperties;
import com.icfi.aem.contentmask.runtime.constants.MaskTypes;
import com.icfi.aem.contentmask.runtime.constants.NodeTypes;
import com.icfi.aem.contentmask.domain.AbstractMaskConfig;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * The MaskConfig implementation that wraps either mask:Config or mask:NamedConfig nodes
 */
public class JcrMaskConfig extends AbstractMaskConfig {

    private final String type;
    private final String name;
    private final String dataPath;
    private final String storagePath;

    public JcrMaskConfig(Resource resource) {
        ValueMap valueMap = resource.getValueMap();
        Node node = resource.adaptTo(Node.class);
        boolean named;
        try {
            if (node == null || !node.isNodeType(NodeTypes.CONFIG)) {
                throw new IllegalArgumentException("Resource " + resource.getPath() + " is not a MaskConfig node");
            }
            named = node.isNodeType(NodeTypes.NAMED_CONFIG);
        } catch (RepositoryException e) {
            throw new IllegalArgumentException("Error verifying node type");
        }
        dataPath = valueMap.get(JcrProperties.DATA_PATH, String.class);
        storagePath = resource.getPath() + "/" + JcrNames.STORAGE_ROOT;
        if (named) {
            type = MaskTypes.NAMED;
            name = valueMap.get(JcrProperties.NAME, String.class);
        } else {
            type = MaskTypes.UUID;
            name = valueMap.get(JcrProperties.JCR_UUID, String.class);
        }
    }

    @Override
    public String getMaskType() {
        return type;
    }

    @Override
    public String getMaskName() {
        return name;
    }

    @Override
    public String getDataPath() {
        return dataPath;
    }

    @Override
    public String getStoragePath() {
        return storagePath;
    }
}
