package com.icfi.aem.contentmask.sling;

import org.apache.sling.api.resource.ValueMap;

import java.util.List;

/**
 * A ValueMap subclass that exposes additional functionality for masking properties
 */
public interface MaskingValueMap extends ValueMap {

    /**
     * Returns TRUE if the given property name is being hidden from the date resource.  If the property is hidden,
     * it will not be visible, even when there is no corresponding value in the content resource.
     * @param name the property name
     * @return true, if the property is hidden
     */
    boolean isHidden(String name);

    /**
     * @return The list of all hidden property names
     */
    List<String> getHidden();

    /**
     * Explicitly sets the given property hidden state for the given name
     * @param name the property name
     * @param hidden the state to set
     */
    void setHidden(String name, boolean hidden);

    /**
     * Returns TRUE if the given node name is being hidden from the date resource.  If the node is hidden, none of its
     * properties will not be visible, even when there is no corresponding value in the content resource.
     * @param name the child resource name
     * @return true, if the child is hidden
     */
    boolean isHiddenChild(String name);

    /**
     * @return The list of all hidden child resource names
     */
    List<String> getHiddenChildren();

    /**
     * Explicitly sets the given child hidden state for the given name
     * @param name the property name
     * @param hidden the state to set
     */
    void setHiddenChild(String name, boolean hidden);

    Object getData(String key);

    Object getStorage(String key);

    <T> T getData(String key, Class<T> tClass);

    <T> T getStorage(String key, Class<T> tClass);

}
