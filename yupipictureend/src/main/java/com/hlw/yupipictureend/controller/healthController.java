package com.hlw.yupipictureend.controller;

import com.hlw.yupipictureend.common.BaseResponse;
import com.hlw.yupipictureend.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class healthController {
    @GetMapping("/health")
    @ResponseBody
    public BaseResponse<String> healthCheck() {

        return ResultUtils.success("ok");
    }
}
