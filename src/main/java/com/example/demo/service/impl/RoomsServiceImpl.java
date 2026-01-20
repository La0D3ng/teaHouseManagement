package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.entity.Rooms;
import com.example.demo.mapper.RoomsMapper;
import com.example.demo.service.IRoomsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class RoomsServiceImpl extends ServiceImpl<RoomsMapper, Rooms> implements IRoomsService {
    @Autowired
    private RoomsMapper roomsMapper;

    // 包间图片存储路径，在application.yml中配置
    @Value("${file.room-image-path:uploads/rooms/}")
    private String roomImagePath;

    // 返回总房间数
    @Override
    public int getRoomTotal() {
        return roomsMapper.getRoomTotal();
    }

    // 返回空闲状态的房间数(根据当前时间段是否有预订记录判断)
    @Override
    public int getAvalibleRoomTotal() {
        return roomsMapper.getAvailableRoomTotal();
    }

    // 返回使用中的包间数(根据当前时间段是否有预订记录判断)
    @Override
    public int getOccupiedRoomToal() {
        return roomsMapper.getOccupiedRoomTotal();
    }

    // 更新包间信息
    @Override
    public boolean updateRooms(Long roomId, String roomName, int capacity, String features, String hourlyRate, String roomType, String description, String imageUrls, Rooms.RoomStatus status) {
        // 检查包间是否已存在
        QueryWrapper<Rooms> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("room_id", roomId);
        Rooms room = this.getOne(queryWrapper);
        if (room == null) {
            throw new RuntimeException("该房间不存在");
        }

        // 检查更新的新状态是否为

        if (capacity == -10086 && roomName == null) {
            // 只更新包间状态
            room.setStatus(status);
        } else {
            // 检查 capacity 容量范围：
            if (capacity <= 0 && capacity != -10086) {
                throw new RuntimeException("包间容量错误");
            }
            if (capacity >= 30) {
                throw new RuntimeException("包间容量错误");
            }

            String finalRoomName = roomName != null ? roomName : room.getRoomName();
            String finalFeatures = features != null ? features : room.getFeatures();
            Double finalHourlyRate = hourlyRate != null ? Double.parseDouble(hourlyRate) : room.getHourlyRate();
            Rooms.RoomType finalRoomType = roomType != null ? Rooms.RoomType.valueOf(roomType) : room.getRoomType();
            String finalDescription = description != null ? description : room.getDescription();
            String finalImageUrls = imageUrls != null ? imageUrls : room.getImageUrls();

            room.setCapacity(capacity);
            // 不更新则保持原样
            room.setRoomName(finalRoomName);
            room.setFeatures(finalFeatures);
            room.setHourlyRate(finalHourlyRate);
            room.setRoomType(finalRoomType);
            room.setDescription(finalDescription);
            room.setImageUrls(finalImageUrls);
            room.setStatus(status);
        }

        return updateById(room);
    }

    // 直接复用上一个接口
    @Override
    public boolean updateRoomStatus(Long roomId, Rooms.RoomStatus newRoomStatus) {
        return updateRooms(roomId,null, -10086, null, null, null, null, null, newRoomStatus);
    }

    // 删除包间
    @Override
    public boolean deleteRooms(Long roomId) {
        // 先检查包间是否存在
        QueryWrapper<Rooms> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("room_id", roomId);
        Rooms room = this.getOne(queryWrapper);
        if (room == null) {
            throw new RuntimeException("该房间不存在");
        }

        return removeById(roomId);
    }

    // 新增包间
    @Override
    public boolean addRooms(Rooms room) {
        // 检查包间名是否已存在
        QueryWrapper<Rooms> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("room_name", room.getRoomName());
        if (this.getOne(queryWrapper) != null) {
            throw new RuntimeException("包间名已存在");
        }

        // 插入用户
        return this.save(room);
    }

    // 获取所有房间信息
    @Override
    public List<Rooms> getAllRooms() {
        return roomsMapper.getAllRooms();
    }

    // 上传包间图片文件
    @Override
    public String uploadRoomImage(Long roomId, MultipartFile file) throws IOException {
        // 验证包间是否存在
        QueryWrapper<Rooms> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("room_id", roomId);
        Rooms room = this.getOne(queryWrapper);
        
        if (room == null) {
            throw new RuntimeException("包间不存在");
        }
        
        // 验证文件是否为空
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("请选择要上传的文件");
        }
        
        // 验证文件类型
        String originalFilename = file.getOriginalFilename();
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("只能上传图片文件");
        }
        
        // 获取文件扩展名并验证
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }
        
        // 验证文件扩展名是否为允许的图片格式
        String[] allowedExtensions = {".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"};
        boolean isValidExtension = false;
        for (String ext : allowedExtensions) {
            if (ext.equals(fileExtension)) {
                isValidExtension = true;
                break;
            }
        }
        if (!isValidExtension) {
            throw new RuntimeException("不支持的文件格式，仅支持: jpg, jpeg, png, gif, bmp, webp");
        }
        
        // 验证文件大小 (限制为5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("文件大小不能超过5MB");
        }
        
        // 生成唯一文件名: room_roomId_timestamp_uuid.ext
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String newFilename = "room_" + roomId + "_" + timestamp + "_" + uuid + fileExtension;
        
        // 创建存储目录
        Path uploadPath = Paths.get(roomImagePath);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // 保存文件
        Path filePath = uploadPath.resolve(newFilename);
        file.transferTo(filePath.toFile());
        
        // 生成访问 URL (相对路径)
        String imageUrl = "/rooms/images/" + newFilename;
        
        // 更新包间图片 URL (追加到现有imageUrls)
        String currentImageUrls = room.getImageUrls();
        if (currentImageUrls == null || currentImageUrls.trim().isEmpty()) {
            room.setImageUrls(imageUrl);
        } else {
            // 用逗号分隔多个图片URL
            room.setImageUrls(currentImageUrls + "," + imageUrl);
        }
        this.updateById(room);
        
        return imageUrl;
    }
}
