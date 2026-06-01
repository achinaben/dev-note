package com.scm.common.tenant;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrgIdContextTest {

    @AfterEach
    void cleanup() {
        OrgIdContext.clear();
    }

    @Test
    void defaultsToOrg001() {
        assertEquals("ORG001", OrgIdContext.get());
    }

    @Test
    void usesThreadLocalValue() {
        OrgIdContext.set("ORG002");
        assertEquals("ORG002", OrgIdContext.get());
    }
}
