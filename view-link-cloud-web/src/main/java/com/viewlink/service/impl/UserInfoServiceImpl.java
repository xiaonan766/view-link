package com.viewlink.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;

import com.viewlink.component.RedisComponent;

import com.viewlink.constants.Constants;
import com.viewlink.entity.dto.CountInfoDto;
import com.viewlink.entity.dto.SysSettingDto;
import com.viewlink.entity.dto.TokenUserInfoDto;
import com.viewlink.entity.dto.UserCountInfoDto;
import com.viewlink.entity.enums.ResponseCodeEnum;
import com.viewlink.entity.enums.UserSexEnum;
import com.viewlink.entity.enums.UserStatusEnum;
import com.viewlink.entity.po.UserFocus;
import com.viewlink.entity.po.VideoInfo;
import com.viewlink.entity.query.UserFocusQuery;
import com.viewlink.entity.query.VideoInfoQuery;

import com.viewlink.exception.BusinessException;
import com.viewlink.mappers.UserFocusMapper;
import com.viewlink.mappers.VideoInfoMapper;
import com.viewlink.utils.CopyTools;

import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.stereotype.Service;

import com.viewlink.entity.enums.PageSize;
import com.viewlink.entity.query.UserInfoQuery;
import com.viewlink.entity.po.UserInfo;
import com.viewlink.entity.vo.PaginationResultVO;
import com.viewlink.entity.query.SimplePage;
import com.viewlink.mappers.UserInfoMapper;
import com.viewlink.service.UserInfoService;
import com.viewlink.utils.StringTools;
import org.springframework.transaction.annotation.Transactional;


/**
 * 用户信息 业务接口实现
 */
@Service("userInfoService")
public class UserInfoServiceImpl implements UserInfoService {

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private UserFocusMapper<UserFocus, UserFocusQuery> userFocusMapper;

    @Resource
    private VideoInfoMapper<VideoInfo, VideoInfoQuery> videoInfoMapper;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<UserInfo> findListByParam(UserInfoQuery param) {
        return this.userInfoMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(UserInfoQuery param) {
        return this.userInfoMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<UserInfo> findListByPage(UserInfoQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<UserInfo> list = this.findListByParam(param);
        PaginationResultVO<UserInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(UserInfo bean) {
        return this.userInfoMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<UserInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userInfoMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<UserInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userInfoMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 多条件更新
     */
    @Override
    public Integer updateByParam(UserInfo bean, UserInfoQuery param) {
        StringTools.checkParam(param);
        return this.userInfoMapper.updateByParam(bean, param);
    }

    /**
     * 多条件删除
     */
    @Override
    public Integer deleteByParam(UserInfoQuery param) {
        StringTools.checkParam(param);
        return this.userInfoMapper.deleteByParam(param);
    }

    /**
     * 根据UserId获取对象
     */
    @Override
    public UserInfo getUserInfoByUserId(String userId) {
        return this.userInfoMapper.selectByUserId(userId);
    }

    /**
     * 根据UserId修改
     */
    @Override
    public Integer updateUserInfoByUserId(UserInfo bean, String userId) {
        return this.userInfoMapper.updateByUserId(bean, userId);
    }

    /**
     * 根据UserId删除
     */
    @Override
    public Integer deleteUserInfoByUserId(String userId) {
        return this.userInfoMapper.deleteByUserId(userId);
    }

    /**
     * 根据Email获取对象
     */
    @Override
    public UserInfo getUserInfoByEmail(String email) {
        return this.userInfoMapper.selectByEmail(email);
    }

    /**
     * 根据Email修改
     */
    @Override
    public Integer updateUserInfoByEmail(UserInfo bean, String email) {
        return this.userInfoMapper.updateByEmail(bean, email);
    }

    /**
     * 根据Email删除
     */
    @Override
    public Integer deleteUserInfoByEmail(String email) {
        return this.userInfoMapper.deleteByEmail(email);
    }

    /**
     * 根据NickName获取对象
     */
    @Override
    public UserInfo getUserInfoByNickName(String nickName) {
        return this.userInfoMapper.selectByNickName(nickName);
    }

    /**
     * 根据NickName修改
     */
    @Override
    public Integer updateUserInfoByNickName(UserInfo bean, String nickName) {
        return this.userInfoMapper.updateByNickName(bean, nickName);
    }

    /**
     * 根据NickName删除
     */
    @Override
    public Integer deleteUserInfoByNickName(String nickName) {
        return this.userInfoMapper.deleteByNickName(nickName);
    }

    @Override
    public void register(String email, String nickName, String registerPassword) {
        /*UserInfo userInfo = this.userInfoMapper.selectByEmail(email);
        if (userInfo != null) {
            UserInfo nickNameUser = this.userInfoMapper.selectByNickName(nickName);
            if (nickNameUser != null) {
                userInfo = new UserInfo();
                String userId = StringTools.getRandomNumber(Constants.LENGTH_10);
                userInfo.setUserId(userId);
                userInfo.setNickName(nickName);
                userInfo.setPassword(StringTools.encodeByMd5(registerPassword));
                userInfo.setJoinTime(new Date());
                userInfo.setStatus(UserStatusEnum.ENABLE.getStatus());
                userInfo.setSex(UserSexEnum.SECRECY.getType());
                userInfo.setTheme(Constants.ONE);
                this.userInfoMapper.insert(userInfo);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }*/
        UserInfo userInfo = this.userInfoMapper.selectByEmail(email);
        if (null != userInfo) {
            throw new BusinessException("邮箱账号已存在");
        }
        UserInfo nickNameUser = this.userInfoMapper.selectByNickName(nickName);
        if (null != nickNameUser) {
            throw new BusinessException("昵称已存在");
        }
        //设置账户相关信息
        userInfo = new UserInfo();
        String userId = StringTools.getRandomNumber(Constants.LENGTH_10);
        userInfo.setUserId(userId);
        userInfo.setNickName(nickName);
        userInfo.setEmail(email);
        userInfo.setPassword(StringTools.encodeByMd5(registerPassword));
        userInfo.setJoinTime(new Date());
        userInfo.setStatus(UserStatusEnum.ENABLE.getStatus());
        userInfo.setSex(UserSexEnum.SECRECY.getType());
        userInfo.setTheme(Constants.ONE);
        //初始化用户硬币数量
        SysSettingDto sysSettingDto = redisComponent.getSysSettingDto();
        userInfo.setCurrentCoinCount(sysSettingDto.getRegisterCoinCount());
        userInfo.setTotalCoinCount(sysSettingDto.getRegisterCoinCount());
        //将账户相关信息插入到数据库中
        this.userInfoMapper.insert(userInfo);
    }

    @Override
    public TokenUserInfoDto login(String email, String password, String ip) {
        //通过输入的email获取用户对象
        UserInfo userInfo = this.userInfoMapper.selectByEmail(email);
        //通过之前获取到的对象，判断账号是否存在或者密码是否正确
        if (null == userInfo || !userInfo.getPassword().equals(password)) {
            throw new BusinessException("账号或者密码错误");
        }
        //判断账户是否被禁用
        if (UserStatusEnum.DISABLE.getStatus().equals(userInfo.getStatus())) {
            throw new BusinessException("账号已禁用");
        }
        //更新最新登录时间和登录ip
        UserInfo updateInfo = new UserInfo();
        updateInfo.setLastLoginTime(new Date());
        updateInfo.setLastLoginIp(ip);
        //调用mapper更新数据库中的数据
        this.userInfoMapper.updateByUserId(updateInfo, userInfo.getUserId());

        //将userInfo信息拷贝到tokenUserInfoDto中
        TokenUserInfoDto tokenUserInfoDto = CopyTools.copy(userInfo, TokenUserInfoDto.class);

        //将数据放到redis中
        redisComponent.saveTokenInfo(tokenUserInfoDto);

        //返回 tokenUserInfoDto
        return tokenUserInfoDto;
    }

    @Override
    public UserInfo getUserDetailInfo(String currentUserId, String userId) {
        UserInfo userInfo = getUserInfoByUserId(userId);
        if (userInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_404);
        }
        //获赞数播放数
        CountInfoDto countInfoDto= videoInfoMapper.selectSumCountInfo(userId);
        userInfo.setLikeCount(countInfoDto.getLikeCount());
        userInfo.setPlayCount(countInfoDto.getPlayCount());
        Integer fansCount = userFocusMapper.selectFansCount(userId);
        Integer focusCount = userFocusMapper.selectFocusCount(userId);
        userInfo.setFansCount(fansCount);
        userInfo.setFocusCount(focusCount);
        //未登录则直接标记未关注
        if (currentUserId == null) {
            userInfo.setHaveFocus(false);
        } else {
            UserFocus userFocus = userFocusMapper.selectByUserIdAndFocusUserId(currentUserId, userId);
            userInfo.setHaveFocus(userFocus != null);
        }
        return userInfo;
    }

    /**
     * 更新用户信息
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public void updateUserInfo(UserInfo userInfo, @NotEmpty String userId, TokenUserInfoDto tokenUserInfoDto) {
        //修改名称需要消耗硬币
        UserInfo dbUserInfo = this.userInfoMapper.selectByUserId(userId);
        //判断是否修改名称且当前硬币数是否小于5
        if (!dbUserInfo.getNickName().equals(userInfo.getNickName()) && dbUserInfo.getCurrentCoinCount() < Constants.UPDATE_NICK_NAME_COIN) {
            throw new BusinessException("硬币不足，无法修改昵称");
        }
        if (!dbUserInfo.getNickName().equals(userInfo.getNickName())) {
            Integer count = this.userInfoMapper.updateCoinCountInfo(userId, -Constants.UPDATE_NICK_NAME_COIN);
            if (count == 0) {
                throw new BusinessException("硬币不足，无法修改昵称");
            }
        }
        //进行数据库用户更新操作
        this.userInfoMapper.updateByUserId(userInfo, userId);
        //更新token
        Boolean updateTokenInfo = false;
        //判断是否修改头像
        if (!userInfo.getAvatar().equals(tokenUserInfoDto.getAvatar())) {
            tokenUserInfoDto.setAvatar(userInfo.getAvatar());
            updateTokenInfo = true;
        }
        //判断是否修改名称
        if (!userInfo.getNickName().equals(tokenUserInfoDto.getNickName())) {
            tokenUserInfoDto.setNickName(userInfo.getNickName());
            updateTokenInfo = true;
        }
        if (updateTokenInfo == true) {
            //更新redis中的用户token信息
            redisComponent.updateTokenInfo(tokenUserInfoDto);
        }
    }

    /**
     * 获取用户粉丝数、关注数、当前硬币数
     */
    @Override
    public UserCountInfoDto getUserCountInfo(String userId) {
        UserInfo userInfo = getUserInfoByUserId(userId);
        Integer focusCount = userFocusMapper.selectFocusCount(userId);
        Integer fansCount = userFocusMapper.selectFansCount(userId);
        UserCountInfoDto userCountInfoDto = new UserCountInfoDto();
        userCountInfoDto.setFansCount(fansCount);
        userCountInfoDto.setFocusCount(focusCount);
        userCountInfoDto.setCurrentCoinCount(userInfo.getCurrentCoinCount());
        return userCountInfoDto;
    }

    /**
     * 修改用户状态
     */
    @Override
    public void changeUserStatus(String userId, Integer status) {
        UserInfo userInfo=new UserInfo();
        userInfo.setStatus(status);
        this.userInfoMapper.updateByUserId(userInfo,userId);
    }

    /**
     * 修改用户硬币数量
     */
    @Override
    public Integer updateCoinCountInfo(String userId, Integer changeCount) {
        return userInfoMapper.updateCoinCountInfo(userId,changeCount);
    }

}