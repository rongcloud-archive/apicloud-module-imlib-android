package io.rong.apicloud;

/**
 * Created by DragonJ on 14/12/16.
 */
public class RongException{

    private int code;
    private String msg;

    public RongException(ErrorCode errorCode){
        this.code = errorCode.getValue();
        this.msg = errorCode.getMessage();
    }

    public RongException(int code){
        ErrorCode errorCode = ErrorCode.setValue(code);
        this.code = errorCode.getValue();
        this.msg = errorCode.getMessage();
    }

    public RongException(Throwable throwable){
        ErrorCode errorCode = ErrorCode.UNKNOWN;
        this.code = errorCode.getValue();
        this.msg = errorCode.getMessage();

    }

    public RongException(IllegalArgumentException e){
        ErrorCode errorCode  = ErrorCode.ARGUMENT_EXCEPTION;
        this.code = errorCode.getValue();
        this.msg = errorCode.getMessage();
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
