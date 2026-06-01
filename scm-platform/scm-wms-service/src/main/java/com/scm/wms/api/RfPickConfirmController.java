package com.scm.wms.api;

import com.scm.wms.pick.PickConfirmService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/rf/v1/pick")
public class RfPickConfirmController {

    private final PickConfirmService pickConfirmService;

    public RfPickConfirmController(PickConfirmService pickConfirmService) {
        this.pickConfirmService = pickConfirmService;
    }

    @PostMapping(value = "/confirm", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> confirm(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(pickConfirmService.confirm(body));
    }
}
