package com.scm.tms.track;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Repository
@ConditionalOnProperty(name = "scm.storage", havingValue = "jdbc")
public class JdbcTrackEventRepository implements TrackEventRepository {

    private final JdbcTemplate jdbc;

    public JdbcTrackEventRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void append(String waybillNo, String eventCode, String source) {
        jdbc.update(
                "INSERT INTO tms_track_event(waybill_no,event_code,source) VALUES(?,?,?)",
                waybillNo, eventCode, source);
    }

    @Override
    public List<TrackEvent> listByWaybill(String waybillNo) {
        return jdbc.query(
                "SELECT waybill_no,event_code,source,event_at FROM tms_track_event "
                        + "WHERE waybill_no=? ORDER BY event_at,id",
                (rs, i) -> {
                    TrackEvent e = new TrackEvent();
                    e.setWaybillNo(rs.getString("waybill_no"));
                    e.setEventCode(rs.getString("event_code"));
                    e.setSource(rs.getString("source"));
                    var ts = rs.getTimestamp("event_at");
                    e.setEventAt(ts == null
                            ? OffsetDateTime.now()
                            : ts.toInstant().atOffset(ZoneOffset.UTC));
                    return e;
                },
                waybillNo);
    }
}
