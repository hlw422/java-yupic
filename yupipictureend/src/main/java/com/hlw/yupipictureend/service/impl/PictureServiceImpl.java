package com.hlw.yupipictureend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hlw.yupipictureend.constant.UserConstant;
import com.hlw.yupipictureend.entity.Picture;
import com.hlw.yupipictureend.entity.User;
import com.hlw.yupipictureend.exception.BusinessException;
import com.hlw.yupipictureend.exception.ErrorCode;
import com.hlw.yupipictureend.exception.ThrowUtils;
import com.hlw.yupipictureend.manager.FileManager;
import com.hlw.yupipictureend.manager.upload.FilePictureUpload;
import com.hlw.yupipictureend.manager.upload.PictureUploadTemplate;
import com.hlw.yupipictureend.manager.upload.UrlPictureUpload;
import com.hlw.yupipictureend.mapper.PictureMapper;
import com.hlw.yupipictureend.model.dto.file.UploadPictureResult;
import com.hlw.yupipictureend.model.dto.picture.PictureQueryRequest;
import com.hlw.yupipictureend.model.dto.picture.PictureReviewRequest;
import com.hlw.yupipictureend.model.dto.picture.PictureUploadRequest;
import com.hlw.yupipictureend.model.enums.PictureReviewStatusEnum;
import com.hlw.yupipictureend.service.PictureService;
import com.hlw.yupipictureend.service.UserService;
import com.hlw.yupipictureend.vo.PictureVO;
import com.hlw.yupipictureend.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture> implements PictureService {
    @Resource
    private FileManager fileManager;

    @Resource
    private UserService userService;

    @Resource
    private FilePictureUpload filePictureUpload;
    @Resource
    private UrlPictureUpload urlPictureUpload;

    @Override
    public PictureVO UploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser) {
        //校验参数
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        //判断新增还是删除
        Long pictureId = pictureUploadRequest.getId();
        if (pictureId != null) {
            Picture oldPicture = this.getById(pictureId);
            ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
            //仅本人或管理员可以更新图片
            ThrowUtils.throwIf(!loginUser.getId().equals(oldPicture.getUserId()) && !loginUser.getUserRole().equals(UserConstant.ADMIN_ROLE), ErrorCode.NO_AUTH_ERROR);
            /*
            boolean exist = this.lambdaQuery().eq(Picture::getId, pictureId).exists();
            ThrowUtils.throwIf(!exist, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
            //删除图片
             */
        }
        //android端上传图片时，图片的路径为public/userId/pictureName.jpg
        String UploadPathPrefix = String.format("public/%s", loginUser.getId());
        //修改为模板方法。
        PictureUploadTemplate pictureUploadTemplate = filePictureUpload;
        if(inputSource instanceof String){
            pictureUploadTemplate = urlPictureUpload;
        }
        UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(inputSource, UploadPathPrefix);

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
        if (pictureId == null) {
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }
        //补充审核参数
        fillReviewParams(picture, loginUser);
        //保存图片
        boolean save = this.saveOrUpdate(picture);
        ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR, "保存图片失败");
        //返回图片信息
        return PictureVO.objToVo(picture);
    }

    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        if (pictureQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        Long userId = pictureQueryRequest.getUserId();
        String searchText = pictureQueryRequest.getSearchText();

        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();

        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
        String reviewMessage = pictureQueryRequest.getReviewMessage();
        Long reviewerId = pictureQueryRequest.getReviewerId();

        if (StrUtil.isNotBlank(searchText)) {
            queryWrapper.and(t -> t.like("name", searchText).or().like("introduction", searchText));
        }
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(name), "name", name);
        queryWrapper.like(ObjUtil.isNotEmpty(introduction), "introduction", introduction);
        queryWrapper.like(ObjUtil.isNotEmpty(category), "category", category);
        queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "pic_size", picSize);
        queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "pic_width", picWidth);
        queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "pic_height", picHeight);
        queryWrapper.eq(ObjUtil.isNotEmpty(picScale), "pic_scale", picScale);
        queryWrapper.like(ObjUtil.isNotEmpty(picFormat), "pic_format", picFormat);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "user_id", userId);

        queryWrapper.eq(ObjUtil.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus);
        queryWrapper.like(ObjUtil.isNotEmpty(reviewMessage), "reviewMessage", reviewMessage);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewerId), "reviewerId", reviewerId);

        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        queryWrapper.orderBy(StrUtil.isNotBlank(sortField), sortOrder.equals("ascend"), sortField);

        return queryWrapper;
    }

    @Override
    public PictureVO getPictureVO(Picture picture, HttpServletRequest request) {
        PictureVO pictureVO = PictureVO.objToVo(picture);
        //关联用户查询信息
        Long id = picture.getUserId();
        if (id != null && id > 0) {
            User user = userService.getById(id);
            UserVO userVO = userService.getUserVO(user);
            pictureVO.setUser(userVO);
        }
        return pictureVO;
    }

    @Override
    public Page<PictureVO> getPicturVOPage(Page<Picture> picturePage, HttpServletRequest request) {
        List<Picture>pictureList = picturePage.getRecords();
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if(CollUtil.isEmpty(pictureList)) {
            return pictureVOPage;
        }
        List<PictureVO>pictureVOList=pictureList.stream().map(PictureVO::objToVo).collect(Collectors.toList());
        Set<Long> userIds=pictureVOList.stream().map(PictureVO::getUserId).collect(Collectors.toSet());
        Map<Long,List<User>> userMap=userService.listByIds(userIds).stream().collect(Collectors.groupingBy(User::getId));
        pictureVOList.forEach(pictureVO -> {
            Long userId=pictureVO.getUserId();
            if(userMap.containsKey(userId)) {
                User user=userMap.get(userId).get(0);
                UserVO userVO=userService.getUserVO(user);
                pictureVO.setUser(userVO);
            }
        });
        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }

    @Override
    public void validPicture(Picture picture) {
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "图片id不能为空");
        if(StrUtil.isNotBlank(url)) {
            ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "图片url长度不能超过1024");
        }
        if(StrUtil.isNotBlank(introduction)) {
            ThrowUtils.throwIf(introduction.length() > 500, ErrorCode.PARAMS_ERROR, "图片介绍长度不能超过500");
        }
    }

    @Override
    public void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {
        /**
         * 参数校验
         * 校验图片是否存在
         * 校验审核状态是否合法
         * 校验审核状态是否有变化
         * 校验用户是否有权限操作
         * 更新图片审核状态
         */
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
        Long pictureId = pictureReviewRequest.getId();
        Integer reviewStatus = pictureReviewRequest.getReviewStatus();
        ThrowUtils.throwIf(pictureId == null, ErrorCode.PARAMS_ERROR, "图片id不能为空");
        ThrowUtils.throwIf(reviewStatus == null, ErrorCode.PARAMS_ERROR, "审核状态不能为空");
        Picture picture = this.getById(pictureId);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        PictureReviewStatusEnum reviewStatusEnum = PictureReviewStatusEnum.getEnumByValue(reviewStatus);
        ThrowUtils.throwIf(reviewStatusEnum == null, ErrorCode.PARAMS_ERROR, "审核状态不合法");
        ThrowUtils.throwIf(reviewStatus.equals(picture.getReviewStatus()), ErrorCode.PARAMS_ERROR, "审核状态没有变化");

        Picture updatePicture = new Picture();
        /**
         * 只更新传过来的字段，其他字段不更新
         */
        BeanUtils.copyProperties(pictureReviewRequest, updatePicture);
        updatePicture.setReviewerId(loginUser.getId());
        updatePicture.setReviewTime(new Date());
        boolean update = this.updateById(updatePicture);
        ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "更新图片审核状态失败");

    }

    /**
     * 填充审核参数
     * @param picture
     * @param loginUser
     */
    @Override
    public void fillReviewParams(Picture picture, User loginUser) {
        /**
         * 管理员可以直接通过
         * 其他人需要审核
         */
        if(userService.isAdmin(loginUser)) {
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewerId(loginUser.getId());
            picture.setReviewTime(new Date());
            picture.setReviewMessage("管理员自动审核通过");
        }
        else{
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
            picture.setReviewMessage("等待管理员审核");
        }
    }


}
