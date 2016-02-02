package com.icfi.aem.contentmask.msm;

import com.icfi.aem.contentmask.constants.JcrNames;
import com.icfi.aem.contentmask.sling.MaskedResource;
import com.icfi.aem.contentmask.sling.MaskingValueMap;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.servlets.post.HtmlResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A servlet that intercepts calls for the AEM's LiveRelationshipServlet, which handles property inheritance changes in
 * MSM-controlled components (i.e., the lock/unlock icons and functionality in dialogs).  This servlet allows reuse of
 * the MSM dialog functionality for "overriding" content for components defined as a part of a content mask.
 */
@SlingServlet(resourceTypes = "sling/servlet/default", selectors = "msm", extensions = "conf", methods = "POST")
public class MsmInterceptorServlet extends SlingAllMethodsServlet {

    private static final Logger LOG = LoggerFactory.getLogger(MsmInterceptorServlet.class);

    private static final String CMD = "cmd";
    private static final String PROPERTY = "msm:propertyName";
    private static final String CMD_CANCEL = "cancelPropertyInheritance";
    private static final String CMD_REENABLE = "reenablePropertyInheritance";
    private static final String PROP_INHERITANCE_CANCELLED = "cq:propertyInheritanceCancelled";
    private static final String PROP_SYNC_CANCELLED = "cq:PropertyLiveSyncCancelled";


    @Reference(target = "(service.pid=com.day.cq.wcm.msm.impl.servlets.LiveRelationshipServlet)")
    private Servlet intercepted;

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
        throws ServletException, IOException {
        String path = request.getResource().getPath();
        if (path.startsWith("/" + JcrNames.ROOT + "/")) {
            path = fixPath(path);
            Resource resource = request.getResourceResolver().resolve(path);
            if (resource instanceof MaskedResource) {
                String cmd = request.getParameter(CMD);
                String property = request.getParameter(PROPERTY);
                if (CMD_REENABLE.equals(cmd) || CMD_CANCEL.equals(cmd)) {
                    LOG.info("Intercepting MSM command on a Masked Resource");
                    MaskingValueMap valueMap = (MaskingValueMap) resource.getValueMap();

                    String[] mixins = valueMap.get("jcr:mixinTypes", String[].class);
                    List<String> mixinList = new ArrayList<>(Arrays.asList(mixins == null ? new String[0] : mixins));
                    if (!mixinList.contains(PROP_SYNC_CANCELLED)) {
                        mixinList.add(PROP_SYNC_CANCELLED);
                        valueMap.put("jcr:mixinTypes", mixinList.toArray(new String[mixinList.size()]));
                    }
                    String[] props = valueMap.get(PROP_INHERITANCE_CANCELLED, String[].class);
                    List<String> cancelled = new ArrayList<>(Arrays.asList(props == null ? new String[0] : props));
                    if (CMD_REENABLE.equals(cmd)) {
                        cancelled.remove(property);
                        valueMap.remove(property);
                        valueMap.setHidden(property, false);
                    } else if (CMD_CANCEL.equals(cmd)) {
                        if (!cancelled.contains(property)) {
                            cancelled.add(property);
                        }
                    }
                    valueMap.put(PROP_INHERITANCE_CANCELLED, cancelled.toArray(new String[cancelled.size()]));
                    request.getResourceResolver().commit();
                    HtmlResponse htmlResponse = new HtmlResponse();
                    htmlResponse.setStatus(HttpServletResponse.SC_OK, "Live Relationship Updated");
                    htmlResponse.setTitle("OK");
                    htmlResponse.send(response, true);
                    return;
                }
            }
        }
        intercepted.service(request, response);
    }

    private static String fixPath(String path) {
        return path.replace("/.msm.conf", "");
    }
}
