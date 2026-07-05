package com.reins.bookstore.service.impl;

import com.reins.bookstore.dto.response.UserLoginResponse;
import com.reins.bookstore.entity.User;
import com.reins.bookstore.entity.UserAuth;
import com.reins.bookstore.repository.UserAuthRepository;
import com.reins.bookstore.repository.UserRepository;
import com.reins.bookstore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 用户服务实现类
 *
 * 实现 UserService 接口，处理用户注册、登录、用户列表和状态管理。
 *
 * 设计说明：
 * - 用户信息分两张表存储：user（基本信息）+ user_auth（鉴权信息）
 * - 这样做的好处是敏感数据（密码、身份）与非敏感数据（昵称、邮箱）隔离
 * - 注册时使用 @Transactional 保证 User 和 UserAuth 同时写入或同时回滚
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserAuthRepository userAuthRepository;

    /** 邮箱格式正则 */
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    /**
     * 用户注册
     *
     * 校验流程（按顺序）：
     * 1. 用户名不能为空
     * 2. 密码不能为空
     * 3. 两次输入的密码必须一致
     * 4. 邮箱不能为空
     * 5. 邮箱格式必须正确
     * 6. 用户名不能重复
     *
     * 注册逻辑：
     * 1. 先保存 User 实体（获得自增 ID）
     * 2. 再用这个 ID 创建 UserAuth，关联两个表
     *
     * @param username        用户名
     * @param password        密码
     * @param confirmPassword 确认密码
     * @param nickname        昵称（可选，默认为用户名）
     * @param email           邮箱
     * @return { message: "注册成功" } 或 { error: "错误信息" }
     */
    @Override
    @Transactional
    public Map<String, Object> register(String username, String password, String confirmPassword,
                                        String nickname, String email) {
        Map<String, Object> res = new HashMap<>();

        // 校验用户名是否为空
        if (username == null || username.trim().isEmpty()) {
            res.put("error", "用户名不能为空");
            return res;
        }
        // 校验密码是否为空
        if (password == null || password.isEmpty()) {
            res.put("error", "密码不能为空");
            return res;
        }
        // 校验两次密码是否一致
        if (!password.equals(confirmPassword)) {
            res.put("error", "两次输入的密码不一致");
            return res;
        }
        // 校验邮箱不能为空
        if (email == null || email.trim().isEmpty()) {
            res.put("error", "邮箱不能为空");
            return res;
        }
        // 校验邮箱格式
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            res.put("error", "邮箱格式不正确");
            return res;
        }
        // 校验用户名是否重复
        if (userAuthRepository.findByUsername(username) != null) {
            res.put("error", "用户名已存在");
            return res;
        }

        // 先保存基本信息（User 表，MySQL 自动生成 ID）
        User user = new User();
        user.setNickname(nickname != null ? nickname : username);
        user.setBalance(0L);
        user.setEmail(email);
        User savedUser = userRepository.save(user);

        // 再保存鉴权信息（UserAuth 表，关联 User 的 ID）
        // 这样设计是为了将敏感字段（密码、身份）与非敏感字段（昵称、邮箱）物理隔离
        UserAuth userAuth = new UserAuth();
        userAuth.setUsername(username);
        userAuth.setPassword(password);
        userAuth.setUserId(savedUser.getId());   // 关联 User 表的主键
        userAuth.setIdentity(0);                 // 默认角色：顾客
        userAuth.setEnable(true);                // 默认启用
        userAuthRepository.save(userAuth);

        res.put("message", "注册成功");
        return res;
    }

    /**
     * 用户登录
     *
     * 验证逻辑：
     * 1. 根据用户名查找 UserAuth
     * 2. 检查用户是否存在 + 密码是否正确 → 不对返回 null，Controller 返回 401
     * 3. 检查用户是否被禁用 → 是则返回 identity=-1，Controller 返回 403
     * 4. 查 User 表获取昵称 → 组装 UserLoginResponse
     *
     * @param username 用户名
     * @param password 密码
     * @return UserLoginResponse 或 null（账号/密码错误）或 identity=-1（禁用）
     */
    @Override
    public UserLoginResponse login(String username, String password) {

        //*调用 repository 进行数据查询
        UserAuth userAuth = userAuthRepository.findByUsername(username);

        // 用户名不存在或密码错误
        if (userAuth == null || !password.equals(userAuth.getPassword())) {
            return null;  // Controller 层根据 null 返回 401
        }

        // 检查用户是否被禁用
        // 通过特殊值 identity=-1 让 Controller 层区分"密码错误"和"被禁用"两种场景
        if (userAuth.getEnable() != null && !userAuth.getEnable()) {
            return new UserLoginResponse(
                    userAuth.getUserId(),
                    userAuth.getUsername(),
                    "",
                    -1  // -1 表示被禁用，Controller 层据此返回 403
            );
        }

        // 登录成功，查询 User 表获取昵称
        User user = userRepository.findById(userAuth.getUserId()).orElse(null);
        if (user == null) {
            return null;
        }

        // 返回不含密码的用户信息
        // *组装一个DTO并返回
        return new UserLoginResponse(
                user.getId(),
                userAuth.getUsername(),
                user.getNickname(),
                userAuth.getIdentity()  // 0=顾客，1=管理员
        );
    }

    /**
     * 获取所有用户列表（管理员专用）
     *
     * 关联 User 和 UserAuth 两张表的数据，组装成完整信息。
     *
     * @return List<{ id, userId, username, nickname, email, identity, enable }>
     */
    @Override
    public List<Map<String, Object>> listAllUsers() {
        List<UserAuth> auths = userAuthRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (UserAuth auth : auths) {
            User user = userRepository.findById(auth.getUserId()).orElse(null);
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", auth.getId());
            map.put("userId", auth.getUserId());
            map.put("username", auth.getUsername());
            map.put("nickname", user != null ? user.getNickname() : "");
            map.put("email", user != null ? user.getEmail() : "");
            map.put("identity", auth.getIdentity());
            map.put("enable", auth.getEnable());
            result.add(map);
        }
        return result;
    }

    /**
     * 切换用户启用/禁用状态
     *
     * 如果用户当前是启用 → 禁用；禁用 → 启用。
     * 被禁用的用户登录时会收到"您的账号已经被禁用"的提示。
     *
     * @param userId 目标用户的 ID
     * @return { message: "用户已启用/用户已禁用", enable: true/false }
     */
    @Override
    @Transactional
    public Map<String, Object> toggleUserStatus(Long userId) {
        Map<String, Object> res = new HashMap<>();
        UserAuth auth = userAuthRepository.findByUserId(userId);
        if (auth == null) {
            res.put("error", "用户不存在");
            return res;
        }
        // 取反：当前 true → false，当前 false → true
        boolean newStatus = auth.getEnable() == null || !auth.getEnable();
        auth.setEnable(newStatus);
        userAuthRepository.save(auth);
        res.put("message", newStatus ? "用户已启用" : "用户已禁用");
        res.put("enable", newStatus);
        return res;
    }
}