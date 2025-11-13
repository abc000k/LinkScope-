package com.nageoffer.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.admin.common.constant.RedisCacheConstant;
import com.nageoffer.shortlink.admin.common.convention.errorcode.BaseErrorCode;
import com.nageoffer.shortlink.admin.common.convention.exception.ClientException;
import com.nageoffer.shortlink.admin.dao.entity.UserDO;
import com.nageoffer.shortlink.admin.dao.mapper.UserMapper;
import com.nageoffer.shortlink.admin.dto.req.UserLoginReqDTO;
import com.nageoffer.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.nageoffer.shortlink.admin.dto.req.UserUpdateReqDTO;
import com.nageoffer.shortlink.admin.dto.resp.UserLoginRespDTO;
import com.nageoffer.shortlink.admin.dto.resp.UserRespDTO;
import com.nageoffer.shortlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static com.nageoffer.shortlink.admin.common.convention.errorcode.BaseErrorCode.USER_NAME_EXIST_ERROR;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;
    //获得Redis
    private final RedissonClient redissonClient;
    //
    private final StringRedisTemplate stringRedisTemplate;
    @Override
    public UserRespDTO getByUsername(String username) {
        final UserDO one = lambdaQuery().eq(UserDO::getUsername, username).one();
        if (one == null){
            //throw new ClientException(BaseErrorCode.USER_NULL);
            throw new ClientException(BaseErrorCode.USER_NULL.message(), BaseErrorCode.USER_NULL);
        }
        UserRespDTO userRespDTO = new UserRespDTO();
        BeanUtils.copyProperties(one, userRespDTO);
        return userRespDTO;
    }

    @Override
    public boolean actualGetByUsername(String username) {
//        final UserDO one = lambdaQuery().eq(UserDO::getUsername, username).one();
//        if (one == null){
//            return false;
//        }
//        return true;
        //bloom过滤器 检测用户名
        log.info("bloom过滤器 检测用户名:{}", username);
        return !userRegisterCachePenetrationBloomFilter.contains(username);
    }

    @Override
    public void register(UserRegisterReqDTO requestParam) {
        //判断用户名是否已存在
        if (!actualGetByUsername(requestParam.getUsername())){
            throw new ClientException(USER_NAME_EXIST_ERROR);
        }
        String KeyUser= RedisCacheConstant.LOCK_USER_REGISTER_KEY+requestParam.getUsername();
        final RLock lock = redissonClient.getLock(KeyUser);
        try{
            if (lock.tryLock()){
                final boolean save = save(BeanUtil.toBean(requestParam, UserDO.class));//插入数据库
                log.info("用户注册成功:{}", save);
                if (!save){
                    throw new ClientException(BaseErrorCode.USER_REGISTER_ERROR);
                }
                //添加bloom过滤器
                userRegisterCachePenetrationBloomFilter.add(requestParam.getUsername());
            }
            else {
                //用户名已存在
                throw new ClientException(USER_NAME_EXIST_ERROR);
            }
        }finally {
            lock.unlock();
        }
    }

    @Override
    public void update(UserUpdateReqDTO requestParam) {
//        //删除
//        remove(lambdaQuery().eq(UserDO::getUsername, requestParam.getUsername()));
//        //在过滤器中删除
//
//        userRegisterCachePenetrationBloomFilter
//        //插入
//        save(BeanUtil.toBean(requestParam, UserDO.class));
//        userRegisterCachePenetrationBloomFilter.add(requestParam.getUsername());
        //判断是否是当前登入用户
        //修改 其实username没有修改
        lambdaUpdate().eq(UserDO::getUsername,requestParam.getUsername()).update(BeanUtil.toBean(requestParam,UserDO.class));
    }
    @Override
    public UserLoginRespDTO login(UserLoginReqDTO requestParam) {
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, requestParam.getUsername())
                .eq(UserDO::getPassword, requestParam.getPassword())
                .eq(UserDO::getDelFlag, 0);
        UserDO userDO = baseMapper.selectOne(queryWrapper);
        if (userDO == null) {
            throw new ClientException("用户不存在");
        }
        Boolean hasLogin = stringRedisTemplate.hasKey("login_" + requestParam.getUsername());
        if (hasLogin != null && hasLogin) {
            throw new ClientException("用户已登录");
        }
        /**
         * Hash
         * Key：login_用户名
         * Value：
         *  Key：token标识
         *  Val：JSON 字符串（用户信息）
         */
        String uuid = UUID.randomUUID().toString();
        stringRedisTemplate.opsForHash().put("login_" + requestParam.getUsername(), uuid, JSON.toJSONString(userDO));
        stringRedisTemplate.expire("login_" + requestParam.getUsername(), 30L, TimeUnit.DAYS);
        return new UserLoginRespDTO(uuid);
    }

    @Override
    public Boolean checkLogin(String username, String token) {
        return stringRedisTemplate.opsForHash().get("login_" + username, token) != null;
    }

    @Override
    public void logout(String username, String token) {
        if (checkLogin(username, token)) {
            stringRedisTemplate.delete("login_" + username);
            return;
        }
        throw new ClientException("用户Token不存在或用户未登录");
    }
}
