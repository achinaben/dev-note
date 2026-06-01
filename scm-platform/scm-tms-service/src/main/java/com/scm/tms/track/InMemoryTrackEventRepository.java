package com.scm.tms.track;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@ConditionalOnProperty(name = "scm.storage", havingValue = "memory", matchIfMissing = true)
public class InMemoryTrackEventRepository implements TrackEventRepository {

    private final Map<String, List<TrackEvent>> byWaybill = new ConcurrentHashMap<>();

    @Override
    public void append(String waybillNo, String eventCode, String source) {
        TrackEvent e = new TrackEvent();
        e.setWaybillNo(waybillNo);
        e.setEventCode(eventCode);
        e.setSource(source);
        e.setEventAt(OffsetDateTime.now());
        byWaybill.compute(waybillNo, (k, list) -> {
            List<TrackEvent> events = list == null ? new ArrayList<>() : new ArrayList<>(list);
            events.add(e);
            return events;
        });
    }

    @Override
    public List<TrackEvent> listByWaybill(String waybillNo) {
        return Collections.unmodifiableList(byWaybill.getOrDefault(waybillNo, List.of()));
    }
}
