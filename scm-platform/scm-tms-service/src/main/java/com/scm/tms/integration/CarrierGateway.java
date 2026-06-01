package com.scm.tms.integration;

import java.util.List;
import java.util.Map;

public interface CarrierGateway {
    List<Map<String, Object>> quote(Map<String, Object> request);
}
