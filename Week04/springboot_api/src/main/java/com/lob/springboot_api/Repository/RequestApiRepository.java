package com.lob.springboot_api.Repository;


import com.lob.springboot_api.dto.DaysTotalDto;
import com.lob.springboot_api.dto.RequestInfoDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.util.List;


public class RequestApiRepository {

    private final JdbcTemplate jdbcTemplate;

    public RequestApiRepository(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private RowMapper<RequestInfoDto> rowMapper() {
        return (rs, rowNum) -> {
            RequestInfoDto requestInfoDto = new RequestInfoDto();
            requestInfoDto.setRequestID(rs.getLong("requestID"));
            requestInfoDto.setUserID(rs.getString("userID"));
            requestInfoDto.setRequestCode(rs.getString("requestCode"));
            requestInfoDto.setCreateDate(rs.getString("createDate"));
            return requestInfoDto;
        };
    }
    private RowMapper<DaysTotalDto> dateRowMapper() {
        return (rs, rowNum) -> {
            DaysTotalDto daysTotalDto = new DaysTotalDto();
            daysTotalDto.setDay(rs.getString("day"));
            daysTotalDto.setCount(rs.getLong("count"));
            return daysTotalDto;
        };
    }

    public RequestInfoDto save(String requestCode, String userID, String createDate) {
        RequestInfoDto requestInfoDto = new RequestInfoDto();
        String sql = "Insert into requestinfo( requestCode, userID, createDate) values ( ?, ?, ?)";
        jdbcTemplate.update(sql , userID, requestCode, createDate);
        return requestInfoDto;
    }

    public List<RequestInfoDto> findByYear(String year){
        String sql = "select * from requestinfo where SUBSTR(createDate, 1, 2) = ?";
        List<RequestInfoDto> res = jdbcTemplate.query(sql, rowMapper() , year);
        return res;
    }

    public List<RequestInfoDto> findByMonth(String findDate) {
        String sql = "select * from requestinfo where SUBSTR(createDate, 1, 4) = ?";
        List<RequestInfoDto> res = jdbcTemplate.query(sql, rowMapper() , findDate);
        return res;
    }

    public List<RequestInfoDto> findByDays(String findDate) {
        String sql = "select * from requestinfo where SUBSTR(createDate, 1, 6) = ?";
        List<RequestInfoDto> res = jdbcTemplate.query(sql, rowMapper(), findDate);
        return res;
    }

    public List<RequestInfoDto> findByMonthAndOrgan(String findDate, String organ) {
        String sql = "SELECT * FROM requestinfo INNER JOIN user where SUBSTR(CreateDate, 1, 4) = ? " +
                "AND requestinfo.userID = user.userID AND user.HR_ORGAN = ? ";
        List<RequestInfoDto> res = jdbcTemplate.query(sql, rowMapper(), findDate, organ);
        return res;
    }

    public List<RequestInfoDto> findByOrgan(String organ) {
        String sql = "SELECT * FROM requestinfo INNER JOIN user WHERE requestinfo.userID = user.userID AND user.HR_ORGAN = ? ";
        List<RequestInfoDto> res = jdbcTemplate.query(sql, rowMapper(), organ);
        return res;
    }

    public List<DaysTotalDto> findByDaysAverage() {
        String sql = "SELECT MID(createdate, 1,6) day, count(*) as count from requestinfo group by day";
        List<DaysTotalDto> res = jdbcTemplate.query(sql, dateRowMapper());
        return res;
    }

    public List<DaysTotalDto> findByNotholidayAverage() {
        String sql = "SELECT MID(createdate, 1,6) day, count(*) as count from requestinfo " +
                     "where dayofweek(createdate) BETWEEN 2 and 6 group by day";
        List<DaysTotalDto> res = jdbcTemplate.query(sql, dateRowMapper());
        return res;
    }


}
