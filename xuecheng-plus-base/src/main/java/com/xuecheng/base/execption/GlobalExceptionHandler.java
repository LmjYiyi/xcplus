package com.xuecheng.base.execption;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * 全局异常处理器
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    @ResponseBody//返回json数据格式
    @ExceptionHandler(XueChengPlusExecption.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)//返回状态码
    //处理自定义异常
    public RestErrorResponse customException(XueChengPlusExecption e){
        //记录异常
        log.error("系统异常{}",e.getErrMessage(),e);

        String errMessage = e.getErrMessage();
        RestErrorResponse restErrorResponse = new RestErrorResponse(errMessage);
        return restErrorResponse;
    }

    @ResponseBody
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    //处理自定义以外的异常
    public RestErrorResponse exception(Exception e){
        log.error("【系统异常】{}",e.getMessage(),e);
        e.printStackTrace();
        if(e.getMessage().equals("不允许访问")){
            return new RestErrorResponse("没有操作此功能的权限");
        }
        return new RestErrorResponse(CommonError.UNKOWN_ERROR.getErrMessage());
    }

    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse methodArgumentNotValidException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        List<String> msgList = new ArrayList<>();
        // 将错误信息放在msgList
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            msgList.add(fieldError.getDefaultMessage());
        }
        // 拼接错误信息
        String msg = String.join(", ", msgList);
        log.error("【系统异常】{}", msg);
        return new RestErrorResponse(msg);
    }



}
