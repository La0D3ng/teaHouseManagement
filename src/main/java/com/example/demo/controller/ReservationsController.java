package com.example.demo.controller;

import com.example.demo.dto.Result;
import com.example.demo.dto.reservations.*;
import com.example.demo.dto.reservations.AvailableRoomInfo;
import com.example.demo.entity.Reservations;
import com.example.demo.service.IReservationsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "预订管理", description = "预订相关接口，包括预订查询、创建、修改、时间段检查")
@RestController
@RequestMapping("/reservations")
public class ReservationsController {
    @Autowired
    private IReservationsService reservationsService;
    
    /**
     * 验证特殊需求内容是否安全
     * 防止XSS攻击和恶意代码注入
     */
    private boolean isContentSafe(String content) {
        if (content == null || content.trim().isEmpty()) {
            return true;
        }
        
        // 检查危险字符和标签
        String[] dangerousPatterns = {
            "<script",
            "</script",
            "javascript:",
            "onerror=",
            "onload=",
            "onclick=",
            "onmouseover=",
            "<iframe",
            "</iframe",
            "<object",
            "<embed",
            "eval(",
            "expression(",
            "vbscript:",
            "<img",
            "src=",
            "<link",
            "<meta",
            "<style",
            "</style"
        };
        
        String lowerContent = content.toLowerCase();
        for (String pattern : dangerousPatterns) {
            if (lowerContent.contains(pattern.toLowerCase())) {
                return false;
            }
        }
        
        return true;
    }

    // 获取本月预定总量
    @GetMapping("/getCurrentMonthTotalReservations")
    public Result<Long> getMonthTotal() {
        Long monthTotal = reservationsService.getMonthTotal();
        if (monthTotal >= 0) {
            return Result.success(monthTotal);
        } else {
            return Result.error("数据获取失败");
        }
    }

    // 获取今日预定总量
    @GetMapping("/getTodayTotalReservations")
    public Result<Long> getTodayTotal() {
        Long todayTotal = reservationsService.getTodayTotal();
        if (todayTotal >= 0) {
            return Result.success(todayTotal);
        } else {
            return Result.error("数据获取失败");
        }
    }

    /**
     * 检查指定时间段是否可预定
     * POST /reservations/checkTimeSlot
     * @param request 包含roomId, reservationDate, startTime, endTime
     */
    @PostMapping("/checkTimeSlot")
    public Result<Boolean> checkTimeSlot(@RequestBody TimeSlotStatusRequest request) {
        try {
            if (request.getRoomId() == null) {
                return Result.error("包间ID不能为空");
            }
            if (request.getReservationDate() == null) {
                return Result.error("预定日期不能为空");
            }
            if (request.getStartTime() == null || request.getEndTime() == null) {
                return Result.error("预定时间不能为空");
            }
            
            boolean isAvailable = reservationsService.isTimeSlotAvailable(
                request.getRoomId(),
                request.getReservationDate(),
                request.getStartTime(),
                request.getEndTime()
            );
            
            return Result.success(isAvailable ? "aviliable" : "occupied", isAvailable);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("检查失败，请稍后重试");
        }
    }

    /**
     * 获取指定包间在指定日期的所有时间段状态
     * GET /reservations/getTimeSlotStatus/{roomId}
     * @param roomId 包间ID
     * @param date 日期（可选，默认今日）
     */
    @GetMapping("/getTimeSlotStatus/{roomId}")
    public Result<List<TimeSlotStatus>> getTimeSlotStatus(
            @PathVariable Long roomId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        try {
            if (roomId == null) {
                return Result.error("包间ID不能为空");
            }
            
            if (date == null) {
                date = LocalDate.now();
            }
            
            List<TimeSlotStatus> timeSlotStatus = reservationsService.getRoomTimeSlotStatus(roomId, date);
            return Result.success(timeSlotStatus);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("获取失败，请稍后重试");
        }
    }

    /**
     * 新增预定单
     * POST /reservations/addReservation
     * @param request 预定请求信息
     */
    @PostMapping("/addReservation")
    public Result<String> addReservation(@RequestBody AddReservationRequest request) {
        try {
            // 参数验证
            if (request.getRoomId() == null) {
                return Result.error("包间ID不能为空");
            }
            if (request.getUserId() == null) {
                return Result.error("用户ID不能为空");
            }
            if (request.getReservationDate() == null) {
                return Result.error("预定日期不能为空");
            }
            if (request.getStartTime() == null || request.getEndTime() == null) {
                return Result.error("预定时间不能为空");
            }
            if (request.getGuestCount() == null || request.getGuestCount() <= 0) {
                return Result.error("宾客人数必须大于0");
            }
            if (request.getContactPhone() == null || request.getContactPhone().trim().isEmpty()) {
                return Result.error("联系电话不能为空");
            }
            if (request.getContactName() == null || request.getContactName().trim().isEmpty()) {
                return Result.error("联系人姓名不能为空");
            }
            
            // 验证特殊需求内容安全性
            if (!isContentSafe(request.getSpecialRequirements())) {
                return Result.error("特殊需求包含非法字符或标签,请修改后重试");
            }
            
            // 限制特殊需求内容长度
            if (request.getSpecialRequirements() != null && request.getSpecialRequirements().length() > 200) {
                return Result.error("特殊需求不能超过200字");
            }
            
            // 创建预定对象
            Reservations reservation = new Reservations();
            reservation.setRoomId(request.getRoomId());
            reservation.setUserId(request.getUserId());
            reservation.setReservationDate(request.getReservationDate());
            reservation.setStartTime(request.getStartTime());
            reservation.setEndTime(request.getEndTime());
            reservation.setGuestCount(request.getGuestCount());
            reservation.setSpecialRequirements(request.getSpecialRequirements());
            reservation.setContactPhone(request.getContactPhone());
            reservation.setContactName(request.getContactName());
            
            boolean success = reservationsService.addReservation(reservation);
            
            if (success) {
                return Result.success("预定成功");
            } else {
                return Result.error("预定失败");
            }
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("预定失败，请稍后重试");
        }
    }

    /**
     * 获取可预定包间的详细信息（包含时间段状态）
     * GET /reservations/getAvailableRoomInfo/{roomId}
     * @param roomId 包间ID
     * @param date 日期（可选，默认今日）
     */
    @GetMapping("/getAvailableRoomInfo/{roomId}")
    public Result<AvailableRoomInfo> getAvailableRoomInfo(
            @PathVariable Long roomId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        try {
            if (roomId == null) {
                return Result.error("包间ID不能为空");
            }
            
            AvailableRoomInfo roomInfo = reservationsService.getAvailableRoomInfo(roomId, date);
            return Result.success(roomInfo);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("获取失败，请稍后重试");
        }
    }

    /**
     * 修改预订单信息
     * PUT /reservations/updateReservation
     * @param request 修改预订请求信息（不可修改房间，可修改时间段、人数、特殊需求）
     */
    @PutMapping("/updateReservation")
    public Result<String> updateReservation(@RequestBody UpdateReservationRequest request) {
        try {
            // 参数验证
            if (request.getReservationId() == null) {
                return Result.error("预定ID不能为空");
            }
            
            // 至少需要修改一项
            if (request.getReservationDate() == null && 
                request.getStartTime() == null && 
                request.getEndTime() == null && 
                request.getGuestCount() == null && 
                request.getSpecialRequirements() == null) {
                return Result.error("请至少修改一项信息");
            }
            
            // 如果修改时间，开始和结束时间必须同时提供
            if ((request.getStartTime() != null && request.getEndTime() == null) ||
                (request.getStartTime() == null && request.getEndTime() != null)) {
                return Result.error("修改时间段时，开始时间和结束时间必须同时提供");
            }
            
            // 验证特殊需求内容安全性
            if (request.getSpecialRequirements() != null && !isContentSafe(request.getSpecialRequirements())) {
                return Result.error("特殊需求包含非法字符或标签,请修改后重试");
            }
            
            // 限制特殊需求内容长度
            if (request.getSpecialRequirements() != null && request.getSpecialRequirements().length() > 200) {
                return Result.error("特殊需求不能超过200字");
            }
            
            boolean success = reservationsService.updateReservation(
                request.getReservationId(),
                request.getReservationDate(),
                request.getStartTime(),
                request.getEndTime(),
                request.getGuestCount(),
                request.getSpecialRequirements()
            );
            
            if (success) {
                return Result.success("预订单修改成功");
            } else {
                return Result.error("预订单修改失败");
            }
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("修改失败，请稍后重试");
        }
    }

    // 修改订单状态
    @PostMapping("/updateReservationStatus")
    public Result<String> updateReservationStatus(@RequestBody UpdateReservationStatusRequest request) {
        try {
            if (request.getReservationId() == null) {
                return Result.error("预订单ID不能为空");
            }
            if (request.getReservationStatus() == null || request.getReservationStatus().trim().isEmpty()) {
                return Result.error("预订单更新状态不能为空");
            }

            boolean success = reservationsService.updateReservationStatus(request.getReservationId(), request.getReservationStatus());
            if (success) {
                return Result.success("预订单状态修改成功");
            } else {
                return Result.error("预订单状态修改失败");
            }
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("修改失败，请稍后重试");
        }
    }

    // 获取订单信息
    @PostMapping("/getReservationsInfo")
    public Result<List<ReservationsInfo>> getReservationsInfo(@RequestBody ReservationsInfoRequest request) {
        try {
            if (request.getUserId() == null) {
                return Result.error("用户ID不能为空");
            }
            if (request.getReservationStatus() == null || request.getReservationStatus().isEmpty()) {
                return Result.error("订单状态不能为空");
            }

            List<ReservationsInfo> reservationsInfos = reservationsService.getReservationsInfo(request.getUserId(), request.getReservationStatus());
            return Result.success(reservationsInfos);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("获取失败，请稍后重试");
        }
    }

    /**
     * 复合搜索筛选可用包间信息
     * POST /reservations/searchAvailableRooms
     * @param request 查询条件（日期、时间段、容量、特色）
     */
    @PostMapping("/searchAvailableRooms")
    public Result<List<AvailableRoomInfo>> searchAvailableRooms(@RequestBody ComplexAvailableRoomInfoRequest request) {
        try {
            // 参数验证（所有参数都是可选的，有默认值）,故不用验证是否为空值
            // 如果提供了时间，开始和结束时间必须同时提供
            if ((request.getStartTime() != null && request.getEndTime() == null) ||
                (request.getStartTime() == null && request.getEndTime() != null)) {
                return Result.error("开始时间和结束时间必须同时提供");
            }
            
            List<AvailableRoomInfo> availableRooms = reservationsService.getAviliableRoomInfoByCompoundQuery(
                request.getReservationDate(),
                request.getStartTime(),
                request.getEndTime(),
                request.getCapacity(),
                request.getFeatures(),
                request.getRoomType()
            );
            
            return Result.success("搜索成功，找到" + availableRooms.size() + "个可用包间", availableRooms);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("搜索失败，请稍后重试");
        }
    }

    @PostMapping("/cancelReservation")
    public Result<Double> cancelReservation(@RequestBody CancelReservationRequest request) {
        try {
            if (request.getReservationId() == null || request.getUserId() == null) {
                return Result.error("参数不能为空");
            }
            // 先检查订单状态
            Reservations reservations = reservationsService.getById(request.getReservationId());
            if (reservations.getReservationStatus() == Reservations.ReservationStatus.cancelled || reservations.getReservationStatus() == Reservations.ReservationStatus.completed) {
                // 已完成的订单和已取消的订单不能取消
                throw new RuntimeException("该订单不可取消");
            }
            // 返回需要退回的总金额
            Double back = reservationsService.cancelReservation(request.getReservationId(), request.getUserId());
            return Result.success(back);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("取消订单失败，请稍后重试");
        }
    }
}
