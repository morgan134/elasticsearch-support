package org.xbib.elasticsearch.plugin.support;

import org.elasticsearch.action.ActionModule;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.rest.RestModule;
import org.xbib.elasticsearch.action.ingest.IngestAction;
import org.xbib.elasticsearch.action.ingest.TransportIngestAction;
import org.xbib.elasticsearch.rest.action.ingest.RestIngestAction;

/**
 * Support plugin
 */
public class SupportPlugin extends Plugin {

    @Override
    public String name() {
        return "support-" + Build.getInstance().getVersion()
                + "-" + Build.getInstance().getShortHash()
                + " " + Build.getInstance().getPlatform();
    }

    @Override
    public String description() {
        return "Support plugin";
    }


    public void onModule(ActionModule module) {
        module.registerAction(IngestAction.INSTANCE, TransportIngestAction.class);
    }

    public void onModule(RestModule module) {
        module.addRestAction(RestIngestAction.class);
    }

}
