package com.example.demo.controller;

import com.example.demo.dto.Result;
import com.example.demo.dto.roomReviews.AddReviewRequest;
import com.example.demo.dto.roomReviews.RoomReviewsInfo;
import com.example.demo.dto.roomReviews.UpdateReviewRequest;
import com.example.demo.service.IRoomReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "评价管理", description = "房间评价相关接口，包括添加、删除、修改、查询评价")
@RestController
@RequestMapping("/roomReviews")
public class RoomReviewsController {
    @Autowired
    private IRoomReviewService roomReviewService;
    
    /**
     * 验证评价内容是否安全
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

    @Operation(summary = "新增评价", description = "仅客户可用，为已完成的预订添加评价")
    @ApiResponse(responseCode = "200", description = "评价成功")
    @ApiResponse(responseCode = "400", description = "参数错误或订单状态不符")
    @ApiResponse(responseCode = "403", description = "权限不足，仅客户可评价")
    @PostMapping("/addReview")
    public Result<String> addReview(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "评价信息，包含预订ID、用户ID、评分(1-5)、评价内容",
                required = true
            )
            @RequestBody AddReviewRequest request) {
        try {
            // 参数验证
            if (request.getReservationId() == null) {
                return Result.error("预定ID不能为空");
            }
            if (request.getUserId() == null) {
                return Result.error("用户ID不能为空");
            }
            if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
                return Result.error("评分必须在1-5之间");
            }
            
            // 验证评价内容安全性
            if (!isContentSafe(request.getReviewContent())) {
                return Result.error("评价内容包含非法字符或标签，请修改后重试");
            }
            
            // 限制评价内容长度
            if (request.getReviewContent() != null && request.getReviewContent().length() > 100) {
                return Result.error("评价内容不能超过100字");
            }
            
            // 创建评价对象
            AddReviewRequest reviewsInfo = new AddReviewRequest();
            reviewsInfo.setReservationId(request.getReservationId());
            reviewsInfo.setUserId(request.getUserId());
            reviewsInfo.setRating(request.getRating());
            reviewsInfo.setReviewContent(request.getReviewContent());
            
            boolean success = roomReviewService.addReviews(reviewsInfo);
            
            if (success) {
                return Result.success("评价成功");
            } else {
                return Result.error("评价失败");
            }
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("评价失败，请稍后重试");
        }
    }

    @Operation(summary = "删除评价", description = "仅客户可用，删除自己的评价")
    @ApiResponse(responseCode = "200", description = "删除成功")
    @ApiResponse(responseCode = "400", description = "评价不存在或非本人评价")
    @ApiResponse(responseCode = "403", description = "权限不足，仅客户可操作")
    @DeleteMapping("/deleteReview/{reviewId}")
    public Result<String> deleteReview(
            @Parameter(description = "评价ID", required = true, example = "1")
            @PathVariable Long reviewId) {
        try {
            if (reviewId == null) {
                return Result.error("评价ID不能为空");
            }
            
            boolean success = roomReviewService.deleteReviews(reviewId);
            
            if (success) {
                return Result.success("评价删除成功");
            } else {
                return Result.error("评价删除失败");
            }
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("删除失败，请稍后重试");
        }
    }

    @Operation(summary = "修改评价", description = "仅客户可用，修改自己的评价内容或评分")
    @ApiResponse(responseCode = "200", description = "修改成功")
    @ApiResponse(responseCode = "400", description = "参数错误或评价不存在")
    @ApiResponse(responseCode = "403", description = "权限不足，仅客户可操作")
    @PutMapping("/updateReview")
    public Result<String> updateReview(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "修改内容，包含评价ID、评分(1-5)、评价内容（至少修改一项）",
                required = true
            )
            @RequestBody UpdateReviewRequest request) {
        try {
            // 参数验证
            if (request.getReviewId() == null) {
                return Result.error("评价ID不能为空");
            }
            
            // 至少需要修改一项
            if (request.getRating() == null && request.getReviewContent() == null) {
                return Result.error("请至少修改一项信息");
            }
            
            // 验证评分范围
            if (request.getRating() != null && (request.getRating() < 1 || request.getRating() > 5)) {
                return Result.error("评分必须在1-5之间");
            }
            
            // 验证评价内容安全性
            if (request.getReviewContent() != null && !isContentSafe(request.getReviewContent())) {
                return Result.error("评价内容包含非法字符或标签，请修改后重试");
            }
            
            // 限制评价内容长度
            if (request.getReviewContent() != null && request.getReviewContent().length() > 100) {
                return Result.error("评价内容不能超过100字");
            }
            
            boolean success = roomReviewService.updateReviews(
                request.getReviewId(),
                request.getRating(),
                request.getReviewContent()
            );
            
            if (success) {
                return Result.success("评价修改成功");
            } else {
                return Result.error("评价修改失败");
            }
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("修改失败，请稍后重试");
        }
    }

    @Operation(summary = "查看用户评价", description = "获取指定用户的所有评价记录")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @ApiResponse(responseCode = "400", description = "用户不存在")
    @GetMapping("/showAllReviews/{userId}")
    public Result<List<RoomReviewsInfo>> showAllReviews(
            @Parameter(description = "用户ID", required = true, example = "1")
            @PathVariable Long userId) {
        try {
            if (userId == null) {
                return Result.error("用户ID不能为空");
            }
            
            List<RoomReviewsInfo> reviews = roomReviewService.showAllReviews(userId);
            return Result.success(reviews);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("获取失败，请稍后重试");
        }
    }
}
