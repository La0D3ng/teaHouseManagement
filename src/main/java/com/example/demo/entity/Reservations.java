package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NonNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName(value = "reservations")
@Schema(description = "预定记录信息")
public class Reservations {
    @Schema(description = "预定记录ID")
    @TableId(value = "reservation_id", type = IdType.AUTO)
    private Long reservationId;

    @Schema(description = "关联的包间ID")
    @TableField("room_id")
    private Long roomId;

    @Schema(description = "关联的用户ID")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "预定日期（年月日）")
    @TableField("reservation_date")
    private LocalDate reservationDate;

    @Schema(description = "预定开始时间（时分秒）")
    @TableField("start_time")
    private LocalTime startTime;

    @Schema(description = "预定结束时间（时分秒）")
    @TableField("end_time")
    private LocalTime endTime;

    @Schema(description = "宾客人数")
    @TableField("guest_count")
    private Integer guestCount;

    @Schema(description = "总金额（精确计算）")
    @TableField("total_amount")
    private BigDecimal totalAmount;

    @Schema(description = "预定状态")
    @TableField("reservation_status")
    private ReservationStatus reservationStatus;

    @Schema(description = "特殊需求备注（可选）")
    @TableField("special_requirements")
    private String specialRequirements;

    @Schema(description = "联系电话")
    @TableField("contact_phone")
    private String contactPhone;

    @Schema(description = "联系人姓名")
    @TableField("contact_name")
    private String contactName;

    @Schema(description = "记录创建时间")
    @TableField("created_at")
    private LocalDateTime createdAt;

    @Schema(description = "记录更新时间")
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    // 预定状态枚举
    public enum ReservationStatus {
        pending,    // 待确认
        confirmed,  // 已确认
        cancelled,  // 已取消
        completed,  // 已完成
    }
}
