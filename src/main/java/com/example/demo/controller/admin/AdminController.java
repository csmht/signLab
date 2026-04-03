package com.example.demo.controller.admin;

import com.example.demo.annotation.RequireRole;
import com.example.demo.enums.UserRole;
import com.example.demo.pojo.request.ClassExperimentQueryRequest;
import com.example.demo.pojo.response.ApiResponse;
import com.example.demo.pojo.response.ClassExperimentDetailResponse;
import com.example.demo.pojo.response.PageResponse;
import com.example.demo.service.ClassExperimentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/admin")
@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AdminController {

    private final ClassExperimentService classExperimentService;

    /**
     * 查询所有班级实验（课次）列表
     * 支持按班级编号、课程ID、时间段过滤
     *
     * @param request 查询请求
     * @return 分页结果
     */
    @GetMapping("/class-experiments")
    @RequireRole(value = UserRole.ADMIN)
    public ApiResponse<PageResponse<ClassExperimentDetailResponse>> queryAllClassExperiments(
            ClassExperimentQueryRequest request) {
        PageResponse<ClassExperimentDetailResponse> response =
            classExperimentService.queryAllClassExperiments(request);
        return ApiResponse.success(response, "查询成功");
    }

}
