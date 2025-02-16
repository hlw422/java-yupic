package com.hlw.yupipictureend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hlw.yupipictureend.entity.Picture;
import com.hlw.yupipictureend.entity.User;
import com.hlw.yupipictureend.exception.ErrorCode;
import com.hlw.yupipictureend.exception.ThrowUtils;
import com.hlw.yupipictureend.manager.FileManager;
import com.hlw.yupipictureend.mapper.PictureMapper;
import com.hlw.yupipictureend.model.dto.file.UploadPictureResult;
import com.hlw.yupipictureend.model.dto.picture.PictureUploadRequest;
import com.hlw.yupipictureend.service.PictureService;
import com.hlw.yupipictureend.vo.PictureVO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.Date;

@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture> implements PictureService {
    @Resource
    private FileManager fileManager;
    @Override
    public PictureVO UploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, User loginUser) {
        //校验参数
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        //判断新增还是删除
        Long pictureId = pictureUploadRequest.getId();
        if(pictureId != null){
            boolean exist = this.lambdaQuery().eq(Picture::getId, pictureId).exists();
            ThrowUtils.throwIf(!exist, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
            //删除图片
        }
        //android端上传图片时，图片的路径为public/userId/pictureName.jpg
        String UploadPathPrefix = String.format("public/%s", loginUser.getId());
        UploadPictureResult uploadPictureResult = fileManager.uploadPicture(multipartFile, UploadPathPrefix);

        //设置图片属性
        Picture picture = new Picture();
        picture.setUrl(uploadPictureResult.getUrl());
        picture.setName(uploadPictureResult.getPicName());
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setUserId(loginUser.getId());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        //新增图片
        if(pictureId == null){
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }

        //保存图片
        boolean save = this.saveOrUpdate(picture);
        ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR, "保存图片失败");
        //返回图片信息
        return PictureVO.objToVo(picture);
    }
}
