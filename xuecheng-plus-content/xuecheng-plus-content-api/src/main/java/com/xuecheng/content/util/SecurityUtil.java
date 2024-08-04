package com.xuecheng.content.util;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.Serializable;
import java.util.Date;

/**
 * 获取当前用户信息的工具类
 */
public class SecurityUtil {

    public static XcUser getUser() {
        try {
            Object principalObj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principalObj instanceof String) {
                //取出用户身份信息
                String principal = principalObj.toString();
                //将json转成对象
                XcUser user = JSON.parseObject(principal, XcUser.class);
                return user;
            }
        } catch (Exception e) {
            System.err.println("获取当前登录用户的信息出错:{}");
            e.printStackTrace();
        }

        return null;
    }

    @Data
    public static class XcUser implements Serializable {

        private String id;

        private String username;

        private String password;

        private String salt;

        private String wxUnionid;

        private String nickname;

        private String name;

        private String userpic;

        private String companyId;

        private String utype;

        private Date birthday;

        private String sex;

        private String email;

        private String cellphone;

        private String qq;

        private String status;

        private Date createTime;

        private Date updateTime;

    }

}