package com.scm.wms.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "scm.integration")
public class IntegrationProperties {
    /** http | kafka | both */
    private String erpShipment = "both";

    /** on | off — WMS 交接后向 TMS 绑定运单号 */
    private String tmsHandover = "on";

    public String getErpShipment() {
        return erpShipment;
    }

    public void setErpShipment(String erpShipment) {
        this.erpShipment = erpShipment;
    }

    public boolean useHttp() {
        return "http".equalsIgnoreCase(erpShipment) || "both".equalsIgnoreCase(erpShipment);
    }

    public boolean useKafka() {
        return "kafka".equalsIgnoreCase(erpShipment) || "both".equalsIgnoreCase(erpShipment);
    }

    public boolean useOff() {
        return "off".equalsIgnoreCase(erpShipment) || "none".equalsIgnoreCase(erpShipment);
    }

    public String getTmsHandover() {
        return tmsHandover;
    }

    public void setTmsHandover(String tmsHandover) {
        this.tmsHandover = tmsHandover;
    }

    public boolean tmsHandoverOn() {
        return !"off".equalsIgnoreCase(tmsHandover) && !"none".equalsIgnoreCase(tmsHandover);
    }
}
