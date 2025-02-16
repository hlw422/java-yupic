package com.hlw.yupipictureend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hlw.yupipictureend.entity.Picture;
import com.hlw.yupipictureend.entity.User;
import com.hlw.yupipictureend.model.dto.picture.PictureUploadRequest;
import com.hlw.yupipictureend.vo.LoginUserVO;
import com.hlw.yupipictureend.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

public interface PictureService extends IService<Picture> {
    PictureVO UploadPicture(MultipartFile multipartFile,
                            PictureUploadRequest pictureUploadRequest,
                            User loginUser);
}
