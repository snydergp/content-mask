package com.icfi.aem.contentmask.components.content;

import com.citytechinc.cq.component.annotations.Component;
import com.citytechinc.cq.component.annotations.DialogField;
import com.citytechinc.cq.component.annotations.FieldProperty;
import com.citytechinc.cq.component.annotations.Listener;
import com.citytechinc.cq.component.annotations.editconfig.ActionConfig;
import com.citytechinc.cq.component.annotations.widgets.Hidden;
import com.citytechinc.cq.component.annotations.widgets.PathField;
import com.icfi.aem.contentmask.domain.MaskConfig;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;

@Component(value = "Inherited Component", actions = {"text:Inherited Component", "-", "copymove", "delete", "-"},
    disableTargeting = true, listeners = @Listener(name = "afteredit", value = "REFRESH_PAGE"),
    templatePath = "/apps/contentmask/components/content/inheritedcomponent/cq:template", actionConfigs = {
        @ActionConfig(text = "Set Target", handler = "contentmask.inheritedcomponent.setTarget"),
        @ActionConfig(text = "Edit", handler = "contentmask.inheritedcomponent.editInherited"),
})
@Model(adaptables = { Resource.class, SlingHttpServletRequest.class })
public class InheritedComponent {

    private final Resource resource;
    private final MaskConfig config;

    @DialogField(name = "./mask:dataPath", additionalProperties = @FieldProperty(name = "predicate", value = "nosystem"))
    @PathField(rootPath = "/content")
    private String path;

    @DialogField(name = "./mask:dataPath@TypeHint", defaultValue = "Path")
    @Hidden(value = "Path")
    private String typeHint;

    public InheritedComponent(Resource resource) {
        this.resource = resource;
        this.config = resource.adaptTo(MaskConfig.class);
    }

    public InheritedComponent(SlingHttpServletRequest request) {
        this(request.getResource());
    }

    public String getPath() {
        return config.getContentPath();
    }

    public String getResourceType() {
        return resource.getResourceResolver().resolve(getPath()).getResourceType();
    }

}
