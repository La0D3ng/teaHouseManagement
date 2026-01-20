package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.dto.reservations.AvailableRoomInfo;
import com.example.demo.entity.Rooms;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface IRoomsService extends IService<Rooms> {
    int getRoomTotal();

    int getAvalibleRoomTotal();

    int getOccupiedRoomToal();

    // 修改包间信息
    boolean updateRooms(Long roomId, String roomName, int capacity, String features, String hourlyRate, String roomType, String description, String imageUrls, Rooms.RoomStatus status);

    // 包间使用状态更新
    boolean updateRoomStatus(Long roomId, Rooms.RoomStatus newRoomStatus);

    // 删除包间
    boolean deleteRooms(Long roomId);

    // 新增包间
    boolean addRooms(Rooms room);

    // 获取所有房间信息
    List<Rooms> getAllRooms();

    // 上传包间图片文件
    String uploadRoomImage(Long roomId, MultipartFile file) throws IOException;
}
