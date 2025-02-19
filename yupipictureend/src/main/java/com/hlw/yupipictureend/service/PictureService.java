package com.hlw.yupipictureend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hlw.yupipictureend.entity.Picture;
import com.hlw.yupipictureend.entity.User;
import com.hlw.yupipictureend.model.dto.picture.PictureQueryRequest;
import com.hlw.yupipictureend.model.dto.picture.PictureUploadRequest;
import com.hlw.yupipictureend.model.dto.user.UserQueryRequest;
import com.hlw.yupipictureend.vo.LoginUserVO;
import com.hlw.yupipictureend.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

public interface PictureService extends IService<Picture> {
    PictureVO UploadPicture(MultipartFile multipartFile,
                            PictureUploadRequest pictureUploadRequest,
                            User loginUser);

    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);


    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    Page<PictureVO>getPicturVOPage(Page<Picture>picturePage,HttpServletRequest request);

    void validPicture(Picture picture);
}
