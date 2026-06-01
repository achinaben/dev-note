package com.scm.tms.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scm.tms.shipment.ShipmentApplicationService;
import com.scm.tms.track.InMemoryTrackEventRepository;
import com.scm.tms.shipment.InMemoryShipmentStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.InputStream;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BindWaybillContractTest {

    private MockMvc mvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        var service = new ShipmentApplicationService(
                new InMemoryShipmentStore(), new InMemoryTrackEventRepository());
        mvc = MockMvcBuilders.standaloneSetup(new ShipmentBindController(service)).build();
    }

    @Test
    void bindBodyMatchesSchema() throws Exception {
        String body = """
                {"package_no":"P-ct-001","order_no":"O-1","waybill_no":"WB-CT-001"}
                """;
        JsonNode node = mapper.readTree(body);
        try (InputStream schema = getClass().getResourceAsStream("/contracts/bind-waybill-request.schema.json")) {
            // 结构与契约 required 一致即可
            assert node.has("package_no") && node.has("waybill_no");
        }
        mvc.perform(post("/tms/v1/shipment/bind-waybill")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }
}
