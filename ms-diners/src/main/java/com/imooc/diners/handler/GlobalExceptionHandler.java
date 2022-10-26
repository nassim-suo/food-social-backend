package com.imooc.diners.handler;

import com.imooc.commons.exception.ParameterException;
import com.imooc.commons.model.domain.ResultInfo;
import com.imooc.commons.utils.ResultInfoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestControllerAdvice // 将输出的内容写入 ResponseBody 中  https://www.jianshu.com/p/47aeeba6414c
@Slf4j
public class GlobalExceptionHandler {

    @Resource
    private HttpServletRequest request;
    // 当一个Controller中有方法加了@ExceptionHandler之后，
    // 这个Controller其他方法中没有捕获的异常就会以参数的形式传入加了@ExceptionHandler注解的那个方法中。
    @ExceptionHandler(ParameterException.class)
    public ResultInfo<Map<String, String>> handlerParameterException(ParameterException ex) {
        // 拿到当前异常请求的地址
        String path = request.getRequestURI();
        ResultInfo<Map<String, String>> resultInfo =
                ResultInfoUtil.buildError(ex.getErrorCode(), ex.getMessage(), path);
        // RestControllerAdvice会自动帮助catch,并匹配相应的ExceptionHandler,
        // 然后重新封装异常信息，返回值，统一格式(ResultInfo<Map<String, String>>)返回给前端
        return resultInfo;
    }

    @ExceptionHandler(Exception.class)
    public ResultInfo<Map<String, String>> handlerException(Exception ex) {
        log.info("未知异常：{}", ex);
        String path = request.getRequestURI();
        ResultInfo<Map<String, String>> resultInfo =
                ResultInfoUtil.buildError(path);
        return resultInfo;
    }

}