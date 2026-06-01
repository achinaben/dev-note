package com.scm.wms.outbound;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "scm.wms.outbound")
public class OutboundWmsProperties {

    /** true 时 CREATED 可直接交接发运（跳过拣货/复核步骤） */
    private boolean relaxedHandover = true;

    public boolean isRelaxedHandover() {
        return relaxedHandover;
    }

    public void setRelaxedHandover(boolean relaxedHandover) {
        this.relaxedHandover = relaxedHandover;
    }
}
