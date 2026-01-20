package com.example.demo.controller;

import com.example.demo.dto.Result;
import com.example.demo.dto.rooms.AddRoomRequest;
import com.example.demo.dto.rooms.DeleteRoomsRequest;
import com.example.demo.dto.rooms.UpdateRoomsRequest;
import com.example.demo.entity.Rooms;
import com.example.demo.service.impl.RoomsServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Tag(name = "房间管理", description = "房间相关接口，包括房间查询、添加、更新、删除")
@RestController
@RequestMapping("/rooms")
public class RoomsController {
    @Autowired
    private RoomsServiceImpl roomsService;

    @Operation(summary = "获取房间总数", description = "返回系统中所有房间的总数")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @GetMapping("/getRoomTotal")
    public Result<Integer> getRoomTotal() {
        int roomTotal = roomsService.getRoomTotal();
        if (roomTotal >= 0) {
            return Result.success(roomTotal);
        } else {
            return Result.error("数据获取失败");
        }
    }

    @Operation(summary = "获取空闲房间数", description = "返回当前可用状态的房间数量")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @GetMapping("/getAvalibleRoomTotal")
    public Result<Integer> getAvalibleRoomTotal() {
        int avalibleRoomTotal = roomsService.getAvalibleRoomTotal();
        if (avalibleRoomTotal >= 0) {
            return Result.success(avalibleRoomTotal);
        } else {
            return Result.error("数据获取失败");
        }
    }

    @Operation(summary = "获取已占用房间数", description = "返回当前被占用的房间数量")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @GetMapping("/getOccupiedRoomToal")
    public Result<Integer> getOccupiedRoomToal() {
        int occupiedRoomTotal = roomsService.getOccupiedRoomToal();
        if (occupiedRoomTotal >= 0) {
            return Result.success(occupiedRoomTotal);
        } else {
            return Result.error("数据获取失败");
        }
    }

    @Operation(summary = "更新房间信息", description = "管理员和员工可用，更新房间的详细信息")
    @ApiResponse(responseCode = "200", description = "更新成功")
    @ApiResponse(responseCode = "403", description = "权限不足")
    @PutMapping("/updateRooms")
    public Result<String> updateRooms(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "房间信息", required = true)
            @RequestBody UpdateRoomsRequest updateRoomsRequest) {
        if (updateRoomsRequest.getRoomId() == null) {
            return Result.error("包间ID不能为空");
        }
        if (updateRoomsRequest.getRoomStatus() == null) {
            return Result.error("包间状态不能为空");
        }

        boolean success = roomsService.updateRooms(
                updateRoomsRequest.getRoomId(),
                updateRoomsRequest.getRoomName(),
                updateRoomsRequest.getCapacity(),
                updateRoomsRequest.getFeatures(),
                updateRoomsRequest.getHourlyRate(),
                updateRoomsRequest.getRoomType(),
                updateRoomsRequest.getDescription(),
                updateRoomsRequest.getImageUrls(),
                updateRoomsRequest.getRoomStatus()
        );

        if (success) {
            return Result.success("包间信息更新成功");
        } else {
            return Result.error("包间信息更新失败");
        }
    }

    @Operation(summary = "更新房间状态", description = "管理员和员工可用，修改房间状态(available/occupied/maintenance)")
    @ApiResponse(responseCode = "200", description = "状态更新成功")
    @ApiResponse(responseCode = "403", description = "权限不足")
    @PostMapping("/updateRoomStatus")
    public Result<String> updateRoomStatus(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "房间状态信息", required = true)
            @RequestBody UpdateRoomsRequest updateRoomsRequest) {
        if (updateRoomsRequest.getRoomId() == null) {
            return Result.error("包间ID不能为空");
        }
        if (updateRoomsRequest.getRoomStatus() == null) {
            return Result.error("包间状态不能为空");
        }
        boolean success = roomsService.updateRoomStatus(
                updateRoomsRequest.getRoomId(),
                updateRoomsRequest.getRoomStatus()
        );

        if (success) {
            return Result.success("包间状态更新成功");
        } else {
            return Result.error("包间状态更新失败");
        }
    }

    @Operation(summary = "添加房间", description = "仅管理员可用，添加新的房间")
    @ApiResponse(responseCode = "200", description = "添加成功")
    @ApiResponse(responseCode = "403", description = "权限不足，仅管理员可操作")
    @PutMapping("/addRooms")
    public Result<String> addRooms(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "房间信息", required = true)
            @RequestBody AddRoomRequest addRoomRequest) {
        if (addRoomRequest.getRoomName() == null || addRoomRequest.getRoomName().trim().isEmpty()) {
            return Result.error("包间名不能为空");
        }
        if (addRoomRequest.getCapacity() == null || addRoomRequest.getCapacity().trim().isEmpty()) {
            return Result.error("包间容量不能为空");
        }
        if (addRoomRequest.getHourlyRate() == null || addRoomRequest.getHourlyRate().trim().isEmpty()) {
            return Result.error("包间费率不能为空");
        }
        if (addRoomRequest.getStatus() == null || addRoomRequest.getStatus().trim().isEmpty()) {
            return Result.error("包间状态不能为空");
        }
        try {
            Rooms room = new Rooms();
            room.setRoomName(addRoomRequest.getRoomName());
            room.setCapacity(Integer.parseInt(addRoomRequest.getCapacity()));
            room.setFeatures(addRoomRequest.getFeatures());
            room.setHourlyRate(Double.parseDouble(addRoomRequest.getHourlyRate()));
            room.setRoomType(Rooms.RoomType.valueOf(addRoomRequest.getRoomType()));
            room.setDescription(addRoomRequest.getDescription());
            room.setImageUrls(addRoomRequest.getImageUrls());
            room.setStatus(Rooms.RoomStatus.valueOf(addRoomRequest.getStatus()));

            boolean success = roomsService.addRooms(room);
            if (success) {
                return Result.success("包间添加成功");
            } else {
                return Result.error("包间添加失败");
            }
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("添加失败,请稍后重试");
        }
    }

    @Operation(summary = "删除房间", description = "仅管理员可用，删除指定房间")
    @ApiResponse(responseCode = "200", description = "删除成功")
    @ApiResponse(responseCode = "403", description = "权限不足，仅管理员可操作")
    @PostMapping("/deleteRooms")
    public Result<String> deleteRooms(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "删除请求", required = true)
            @RequestBody DeleteRoomsRequest deleteRoomsRequest) {
        if (deleteRoomsRequest.getRoomId() == null || deleteRoomsRequest.getRoomId().trim().isEmpty()) {
            return Result.error("待删除包间不能为空");
        }
        try {
            boolean success = roomsService.deleteRooms(Long.parseLong(deleteRoomsRequest.getRoomId()));
            if (success) {
                return Result.success("包间删除成功");
            } else {
                return Result.error("包间删除失败");
            }
        }  catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("删除失败,请稍后重试");
        }
    }

    @Operation(summary = "获取所有房间信息", description = "返回系统中所有房间的详细信息")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @GetMapping("/getAllRooms")
    public Result<List<Rooms>> getAllRooms() {
        try {
            List<Rooms> rooms = roomsService.getAllRooms();
            return Result.success(rooms);
        } catch (Exception e) {
            return Result.error("获取房间信息失败");
        }
    }

    @Operation(summary = "上传包间图片", description = "管理员和员工可用，上传包间图片文件，支持jpg/png/gif等图片格式，文件大小不超过5MB")
    @ApiResponse(responseCode = "200", description = "上传成功，返回图片 URL")
    @ApiResponse(responseCode = "400", description = "文件类型不支持或文件过大")
    @ApiResponse(responseCode = "403", description = "权限不足")
    @PostMapping("/uploadRoomImage")
    public Result<String> uploadRoomImage(
            @RequestParam("roomId") Long roomId,
            @RequestParam("file") MultipartFile file) {
        try {
            if (roomId == null) {
                return Result.error("包间ID不能为空");
            }
            
            // 上传文件并获取URL
            String imageUrl = roomsService.uploadRoomImage(roomId, file);
            return Result.success("包间图片上传成功", imageUrl);
        } catch (RuntimeException e) {
            // 返回具体的业务错误信息
            return Result.error(e.getMessage());
        } catch (IOException e) {
            // 返回IO异常的详细信息
            return Result.error("文件保存失败: " + e.getMessage());
        } catch (Exception e) {
            // 返回其他异常的详细信息
            e.printStackTrace();
            return Result.error("包间图片上传失败: " + e.getMessage());
        }
    }
}
