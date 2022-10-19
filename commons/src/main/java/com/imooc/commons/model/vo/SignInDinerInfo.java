package com.imooc.commons.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 公共的用户登录信息对象
 */
@Getter
@Setter
@ApiModel(value = "SignInDinerInfo", description = "登录用户信息")
public class SignInDinerInfo implements Serializable {
    // Adds and manipulates data of a model property.
    // @ApiModelProperty()用于方法，字段； 表示对model属性的说明或者数据操作更改
    // 注解的属性如果不写属性名，默认为value
    /*  value–字段说明
        name–重写属性名字
        dataType–重写属性类型
        required–是否必填
        example–举例说明
        hidden–隐藏
     */
    @ApiModelProperty("主键")
    private Integer id;
    @ApiModelProperty("用户名")
    private String username;
    @ApiModelProperty("昵称")
    private String nickname;
    @ApiModelProperty("手机号")
    private String phone;
    @ApiModelProperty("邮箱")
    private String email;
    @ApiModelProperty("头像")
    private String avatarUrl;
    @ApiModelProperty("角色")
    private String roles;

}