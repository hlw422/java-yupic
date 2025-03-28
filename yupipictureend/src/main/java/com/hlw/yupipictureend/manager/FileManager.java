package com.hlw.yupipictureend.manager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.hlw.yupipictureend.config.CosClientConfig;
import com.hlw.yupipictureend.exception.BusinessException;
import com.hlw.yupipictureend.exception.ErrorCode;
import com.hlw.yupipictureend.exception.ThrowUtils;
import com.hlw.yupipictureend.manager.upload.PictureUploadTemplate;
import com.hlw.yupipictureend.model.dto.file.UploadPictureResult;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@Deprecated
/**
 * 已废弃，请使用 {@link FilePictureUpload}
 */
public class FileManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;


    /**
     * 上传图片
     *
     * @param multipartFile    文件
     * @param uploadPathPrefix 上传路径前缀
     * @return
     */
    public UploadPictureResult uploadPicture(MultipartFile multipartFile, String uploadPathPrefix) {
        // 校验图片
        validPicture(multipartFile);
        // 图片上传地址
        String uuid = RandomUtil.randomString(16);
        String originFilename = multipartFile.getOriginalFilename();
        //yyyy-MM-dd_16位随机数.后缀名
        String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid,
                FileUtil.getSuffix(originFilename));
        //上传的文件路径
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFilename);
        File file = null;
        try {
            // 创建临时文件
            file = File.createTempFile(uploadPath, null);
            multipartFile.transferTo(file);
            // 上传图片
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            // 封装返回结果
            UploadPictureResult uploadPictureResult = new UploadPictureResult();
            int picWidth = imageInfo.getWidth();
            int picHeight = imageInfo.getHeight();
            double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
            uploadPictureResult.setPicName(FileUtil.mainName(originFilename));
            uploadPictureResult.setPicWidth(picWidth);
            uploadPictureResult.setPicHeight(picHeight);
            uploadPictureResult.setPicScale(picScale);
            uploadPictureResult.setPicFormat(imageInfo.getFormat());
            uploadPictureResult.setPicSize(FileUtil.size(file));
            uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + uploadPath);
            return uploadPictureResult;
        } catch (Exception e) {
            log.error("图片上传到对象存储失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            this.deleteTempFile(file);
        }
    }

    /*
    /**
     * 校验文件
     *
     * @param multipartFile multipart 文件
     */
    public void validPicture(MultipartFile multipartFile) {
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "文件不能为空");
        // 1. 校验文件大小
        long fileSize = multipartFile.getSize();
        final long ONE_M = 1024 * 1024L;
        ThrowUtils.throwIf(fileSize > 2 * ONE_M, ErrorCode.PARAMS_ERROR, "文件大小不能超过 2M");
        // 2. 校验文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        // 允许上传的文件后缀
        final List<String> ALLOW_FORMAT_LIST = Arrays.asList("jpeg", "jpg", "png", "webp");
        ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(fileSuffix), ErrorCode.PARAMS_ERROR, "文件类型错误");
    }

    /**
     * 删除临时文件
     */
    public void deleteTempFile(File file) {
        if (file == null) {
            return;
        }
        // 删除临时文件
        boolean deleteResult = file.delete();
        if (!deleteResult) {
            log.error("file delete error, filepath = {}", file.getAbsolutePath());
        }
    }
    public void validPicture(String url) {
        ThrowUtils.throwIf(StrUtil.isBlank(url), ErrorCode.PARAMS_ERROR, "图片 url 不能为空");
        try {
            new URL(url);
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片 url 地址错误");
        }
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片 url 必须以http://或https://开头");
        }
        HttpResponse response = null;
        try {
            if (response.getStatus() != HttpStatus.HTTP_OK) {
                return;
            }
            String contentType = response.header("Content-Type");
            if (StrUtil.isNotBlank(contentType)) {
                final List<String> ALLOW_FORMAT_LIST = Arrays.asList("image/jpeg", "image/jpg", "image/png", "image/webp");
                ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(contentType.toLowerCase()), ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
            String contentLength = response.header("Content-Length");     // 图片大小
            if (StrUtil.isNotBlank(contentLength)) {
                try {
                    long contentLengthLong = Long.parseLong(contentLength);
                    final long ONE_M = 1024 * 1024L;
                    ThrowUtils.throwIf(contentLengthLong > 2 * ONE_M, ErrorCode.PARAMS_ERROR, "文件大小不能超过 2M");
                } catch (NumberFormatException e) {
                    ThrowUtils.throwIf(true, ErrorCode.PARAMS_ERROR, "图片大小错误");
                }

            }
        } catch (Exception e) {

        } finally {
            if (response != null) {
                response.close();
            }
        }
    }
}



