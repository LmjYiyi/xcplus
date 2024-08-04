package com.xuecheng.base.execption;

/**
 * 项目自定义异常类
 */
public class XueChengPlusExecption extends RuntimeException{
    private String errMessage;

    public XueChengPlusExecption(){
        super();
    }

    public XueChengPlusExecption(String errMessage){
       super(errMessage);
       this.errMessage = errMessage;
    }

    public String getErrMessage(){
        return errMessage;
    }

    //常见错误
    public static void cast(CommonError commonError){
        throw new XueChengPlusExecption(commonError.getErrMessage());
    }
    //可设置
    public static void cast(String errMessage){
        throw new XueChengPlusExecption(errMessage);
    }
}
