package com.icfi.aem.contentmask.jcr;

import com.icfi.aem.contentmask.runtime.constants.JcrProperties;
import com.icfi.aem.contentmask.runtime.constants.NodeTypes;
import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.jcr.api.SlingRepository;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;

@Component(immediate = true)
@Service
public class InitializeLiveCopyListener implements EventListener {

    private static final int EVENTS = Event.NODE_ADDED| Event.PROPERTY_ADDED | Event.PROPERTY_CHANGED;

    @Reference
    private SlingRepository repository;

    @Override
    public void onEvent(EventIterator events) {
        try {
            Session session = repository.loginAdministrative(null);
            while (events.hasNext()) {
                Event event = events.nextEvent();

                Node node;
                if (event.getType() == Event.NODE_ADDED) {
                    node = session.getNode(event.getPath());
                } else if (event.getPath().endsWith("/" + JcrProperties.INITIALIZE_LIVE_COPY)) {
                    node = session.getNode(StringUtils.removeEnd(event.getPath(), "/" + JcrProperties.INITIALIZE_LIVE_COPY));
                } else {
                    continue;
                }

                if (node.hasProperty(JcrProperties.INITIALIZE_LIVE_COPY)
                    && node.getProperty(JcrProperties.INITIALIZE_LIVE_COPY).getBoolean()
                    && !node.hasNode("cq:LiveSyncConfig")) {

                    node.addMixin("cq:LiveSync");
                    Node liveCopy = node.addNode("cq:LiveSyncConfig", "cq:LiveCopy");
                    liveCopy.setProperty("cq:master", "..");
                    session.save();
                }
            }
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }

    @Activate
    protected void activate() {
        try {
            Session session = repository.loginAdministrative(null);
            session.getWorkspace().getObservationManager().addEventListener(
                this,
                EVENTS,
                "/",
                true,
                null,
                new String[]{ NodeTypes.STORAGE_ROOT },
                false
            );
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }

    @Deactivate
    protected void deactivate() {
        try {
            Session session = repository.loginAdministrative(null);
            session.getWorkspace().getObservationManager().removeEventListener(this);
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }
}
