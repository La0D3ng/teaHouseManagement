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
@TableName("room_reviews")
@Schema(description = "包间评价信息")
public class RoomReviews {
    @Schema(description = "评价记录ID", example = "1")
    @TableId(value = "review_id", type = IdType.AUTO)
    private Long reviewId;

    @Schema(description = "关联的预定ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField("reservation_id")
    private Long reservationId;

    @Schema(description = "关联的包间ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField("room_id")
    private Long roomId;

    @Schema(description = "关联的用户ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField("user_id")
    private Long userId;

    @Schema(description = "评分（1-5分）", example = "5", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "1", maximum = "5")
    @TableField("rating")
    private Integer rating;

    @Schema(description = "评价内容", example = "环境很好,服务很周到")
    @TableField("review_content")
    private String reviewContent;

    @Schema(description = "创建时间", example = "2024-01-01T10:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField("created_at")
    private LocalDateTime createdAt;
}
