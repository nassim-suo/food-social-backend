package com.imooc.diners.controller;

import com.imooc.commons.model.domain.ResultInfo;
import com.imooc.commons.utils.ResultInfoUtil;
import com.imooc.diners.service.DinersService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 食客服务控制层
 */
@RestController
@Api(tags = "食客相关接口")
public class DinersController {

    @Resource
    private DinersService dinersService;

    @Resource
    private HttpServletRequest request;


    /**
     * 登录
     *
     * @param account
     * @param password
     * @return
     */
    @GetMapping("signin")
    public ResultInfo signIn(String account, String password) {
        //String getServletPath()
        //
        //Returns the part of this request's URL that calls the servlet.
        // This path starts with a "/" character and includes either the servlet name or a path to the servlet,
        // but does not include any extra path information or a query string.
        // Same as the value of the CGI variable SCRIPT_NAME.
//        System.out.println("request = " + request.getServletPath());
        return dinersService.signIn(account, password, request.getServletPath());
    }

}
