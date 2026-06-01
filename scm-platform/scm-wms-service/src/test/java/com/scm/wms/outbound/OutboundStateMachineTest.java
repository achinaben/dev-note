package com.scm.wms.outbound;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OutboundStateMachineTest {

    private final OutboundStateMachine sm = new OutboundStateMachine();

    @ParameterizedTest
    @CsvSource({
            "CREATED,PICKED,true",
            "PICKED,CHECKED,true",
            "CHECKED,SHIPPED,true",
            "CREATED,SHIPPED,false",
            "SHIPPED,CREATED,false"
    })
    void transitions(String from, String to, boolean allowed) {
        assertEquals(allowed, sm.canTransition(OutboundStatus.valueOf(from), OutboundStatus.valueOf(to)));
    }

    @Test
    void fastForwardCreatedToChecked() {
        assertEquals(OutboundStatus.CHECKED, sm.fastForwardToChecked(OutboundStatus.CREATED));
    }

    @Test
    void applyRejectsShipFromCreated() {
        assertThrows(IllegalStateException.class,
                () -> sm.apply(OutboundStatus.CREATED, OutboundStatus.SHIPPED));
    }
}
