package com.example.demo.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NonNull;

import java.time.LocalDateTime;

@Data
@TableName(value = "rooms")
@Schema(description = "包间信息")
public class Rooms {
    @Schema(description = "包间ID", example = "1")
    @TableId(value = "room_id", type = IdType.AUTO)
    @TableField("room_id")
    private Long roomId;

    @Schema(description = "包间名", example = "雅韵阁")
    @TableField("room_name")
    private String roomName;

    @Schema(description = "包间容量（人数）", example = "8")
    @TableField("capacity")
    private int capacity;

    @Schema(description = "包间特色", example = "古典,雅致")
    @TableField("features")
    private String features;

    @Schema(description = "包间每小时单价", example = "150.0")
    @TableField("hourly_rate")
    private double hourlyRate;

    @Schema(description = "包间类型", example = "premium", allowableValues = {"standard", "premium", "vip"})
    @TableField("room_type")
    private RoomType roomType;

    @Schema(description = "包间描述", example = "宽敞明亮的包间，适合商务洽谈")
    @TableField("description")
    private String description;

    @Schema(description = "包间图片地址（多个URL用逗号分隔）", example = "https://example.com/room1.jpg,https://example.com/room2.jpg")
    @TableField("image_urls")
    private String imageUrls;

    @Schema(description = "包间创建时间", example = "2024-01-01T10:00:00")
    @TableField("created_at")
    private LocalDateTime createdAt;

    @Schema(description = "包间更新时间", example = "2024-01-01T10:00:00")
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("status")
    private RoomStatus status;

    // 包间类型枚举（标准，优质，vip）
    public enum RoomType {
        standard,
        vip
    }

    // 包间状态枚举（维修，正常）
    public enum RoomStatus {
        maintenance,
        normal
    }
}
