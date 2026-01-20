package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.Rooms;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RoomsMapper extends BaseMapper<Rooms> {
    // ------- 包间管理页面需求 -------
    // 获取总包间数
    @Select("SELECT COUNT(room_id) FROM rooms")
    int getRoomTotal();

    // 改！包间是否可用是根据预订单里面来判断的！
    // 复合查询:根据容量、特色和包间类型筛选包间(仅返回可用状态的包间) -- MyBatis 支持动态 SQL
    @Select("<script>" +
            "SELECT * FROM rooms WHERE 1=1 " +
            "<if test='capacity != null'> AND capacity &gt;= #{capacity} </if>" +
            "<if test='features != null and features != \"\"'> AND features LIKE CONCAT('%', #{features}, '%') </if>" +
            "<if test='roomType != null'> AND room_type = #{roomType} </if>" +
            "AND (status = 'normal')" +
            "</script>")
    List<Rooms> getRoomsByComplexQuery(@Param("capacity") Integer capacity, @Param("features") String features, @Param("roomType") Rooms.RoomType roomType);

    // 获取所有房间的所有信息
    @Select("SELECT * FROM rooms")
    List<Rooms> getAllRooms();

    // 获取当前空闲的房间数(当前时间段没有预订记录的房间)
    @Select("SELECT COUNT(DISTINCT r.room_id) FROM rooms r " +
            "WHERE r.status = 'normal' " +
            "AND r.room_id NOT IN ( " +
            "    SELECT DISTINCT room_id FROM reservations " +
            "    WHERE reservation_date = CURDATE() " +
            "    AND reservation_status != 'cancelled' " +
            "    AND reservation_status != 'completed' " +
            "    AND start_time <= CURTIME() " +
            "    AND end_time > CURTIME() " +
            ")")
    int getAvailableRoomTotal();

    // 获取当前使用中的房间数(当前时间段有预订记录的房间)
    @Select("SELECT COUNT(DISTINCT room_id) FROM reservations " +
            "WHERE reservation_date = CURDATE() " +
            "AND reservation_status != 'cancelled' " +
            "AND reservation_status != 'completed' " +
            "AND start_time <= CURTIME() " +
            "AND end_time > CURTIME()")
    int getOccupiedRoomTotal();
}
