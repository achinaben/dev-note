package com.scm.tms.track;

import java.util.List;

public interface TrackEventRepository {

    void append(String waybillNo, String eventCode, String source);

    List<TrackEvent> listByWaybill(String waybillNo);
}
