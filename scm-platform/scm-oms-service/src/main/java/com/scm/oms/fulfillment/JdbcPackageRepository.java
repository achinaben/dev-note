package com.scm.oms.fulfillment;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@ConditionalOnProperty(name = "scm.storage", havingValue = "jdbc")
public class JdbcPackageRepository implements PackageRepository {
    private final JdbcTemplate jdbc;

    private static final RowMapper<PackageRecord> MAPPER = (rs, i) -> {
        PackageRecord p = new PackageRecord();
        p.setPackageNo(rs.getString("package_no"));
        p.setOrderNo(rs.getString("order_no"));
        p.setOutboundNo(rs.getString("outbound_no"));
        p.setStatus(rs.getString("status"));
        return p;
    };

    public JdbcPackageRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void save(PackageRecord pkg) {
        jdbc.update("""
                INSERT INTO oms_package(package_no,order_no,outbound_no,status)
                VALUES(?,?,?,?)
                ON DUPLICATE KEY UPDATE outbound_no=VALUES(outbound_no), status=VALUES(status)
                """,
                pkg.getPackageNo(), pkg.getOrderNo(), pkg.getOutboundNo(), pkg.getStatus());
    }

    @Override
    public Optional<PackageRecord> findByOrderNo(String orderNo) {
        var list = findAllByOrderNo(orderNo);
        return list.stream().findFirst();
    }

    @Override
    public List<PackageRecord> findAllByOrderNo(String orderNo) {
        return jdbc.query("SELECT * FROM oms_package WHERE order_no=?", MAPPER, orderNo);
    }

    @Override
    public Optional<PackageRecord> findByPackageNo(String packageNo) {
        var list = jdbc.query("SELECT * FROM oms_package WHERE package_no=?", MAPPER, packageNo);
        return list.stream().findFirst();
    }
}
