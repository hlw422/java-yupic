package com.hlw.yupipictureend.controller;

import com.hlw.yupipictureend.annotation.AuthCheck;
import com.hlw.yupipictureend.common.BaseResponse;
import com.hlw.yupipictureend.constant.UserConstant;
import com.hlw.yupipictureend.model.dto.picture.PictureUploadRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/css")
@Slf4j
public class CssController {
    /**
     * a-row 测试数据
     *
     * @return
     */
    @PostMapping("/aRowData")
    public BaseResponse<String> GetArowData() {
        return new BaseResponse<>(0, "a-row 测试数据");
    }
}
