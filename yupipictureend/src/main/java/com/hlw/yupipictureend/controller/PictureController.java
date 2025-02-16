package com.hlw.yupipictureend.controller;

import com.hlw.yupipictureend.annotation.AuthCheck;
import com.hlw.yupipictureend.common.BaseResponse;
import com.hlw.yupipictureend.common.ResultUtils;
import com.hlw.yupipictureend.constant.UserConstant;
import com.hlw.yupipictureend.entity.User;
import com.hlw.yupipictureend.model.dto.picture.PictureUploadRequest;
import com.hlw.yupipictureend.service.PictureService;
import com.hlw.yupipictureend.service.UserService;
import com.hlw.yupipictureend.vo.PictureVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/picture")
public class PictureController {
    @Resource
    private UserService userService;
    @Resource
    private PictureService pictureService;

    @PostMapping("/upload")
    @AuthCheck(mustRole= UserConstant.ADMIN_ROLE)
    public BaseResponse<PictureVO>uploadPicture(
            @RequestPart MultipartFile file,
            PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request
    )
    {
        User loginUser = userService.getLoginUser(request);
        PictureVO pictureVO = pictureService.UploadPicture(file, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }

}
