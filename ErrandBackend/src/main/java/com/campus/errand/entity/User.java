package com.campus.errand.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * 用户实体类
 */
@Data
@TableName("users")
public class User {
    private Long id;
    private String username;
    private String password;
    private String realName;
    private String nickname;
    private String avatar;
    private String phone;
    private String email;
    private String role;
    private Integer isVerified;
    private Integer isVip;
    private Integer creditScore;
    private String college;
    private String grade;
    private String major;
    private String gender;
    private String signature;
    private Date registerTime;
    private Date lastLoginTime;
    private Integer status;
}
