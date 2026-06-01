package com.scm.tms.support;

import com.github.tomakehurst.wiremock.WireMockServer;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

/** WireMock 启动封装；Java 22+ 与 Jetty12 存在 ICCE，需在测试类上 @EnabledIf。 */
public final class WireMockTestServer {

    private WireMockTestServer() {
    }

    /** WireMock Jetty12 在 JDK 22+ 暂不可用，CI（JDK 17）正常跑。 */
    public static boolean isSupportedJvm() {
        return Runtime.version().feature() <= 21;
    }

    public static WireMockServer startDynamic() {
        WireMockServer server = new WireMockServer(wireMockConfig().dynamicPort());
        server.start();
        return server;
    }
}
