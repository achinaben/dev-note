package com.scm.wms.outbound;

import org.springframework.stereotype.Component;

@Component
public class OutboundStateMachine {

    public boolean canTransition(OutboundStatus current, OutboundStatus next) {
        if (next.rank() < current.rank()) {
            return false;
        }
        if (next == OutboundStatus.SHIPPED && current != OutboundStatus.CHECKED) {
            return false;
        }
        return next.rank() - current.rank() <= 2;
    }

    public OutboundStatus apply(OutboundStatus current, OutboundStatus next) {
        if (!canTransition(current, next)) {
            throw new IllegalStateException("Cannot transition from " + current + " to " + next);
        }
        return next;
    }

    /** 联调宽松模式：CREATED 可直接 CHECKED */
    public OutboundStatus fastForwardToChecked(OutboundStatus current) {
        if (current == OutboundStatus.CHECKED || current == OutboundStatus.SHIPPED) {
            return current;
        }
        if (current == OutboundStatus.CREATED) {
            return OutboundStatus.CHECKED;
        }
        return apply(current, OutboundStatus.CHECKED);
    }
}
