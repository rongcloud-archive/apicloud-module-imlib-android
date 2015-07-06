package io.rong.apicloud;

import android.util.Log;

/**
 * Created by DragonJ on 14/12/17.
 */
public enum ErrorCode {

    /**
     * 尚未初始化。
     */
    NOT_INIT(-10000, "Not Init"),

    /**
     * 尚未连接。
     */
    NOT_CONNECTED(-10001, "Not Connected."),

    /**
     * 参数异常。
     */
    ARGUMENT_EXCEPTION(-10002, "Argument Exception."),

    /**
     * 未知错误。
     */
    UNKNOWN(-1, "Unknown error."),

    /**
     * 数据包不完整。 请求数据包有缺失。
     */
    PACKAGE_BROKEN(2002, "Package is broken."),

    /**
     * 服务器不可用。
     */
    SERVER_UNAVAILABLE(2003, "Server is unavailable."),

    /**
     * 错误的令牌（Token），Token 解析失败，请重新向身份认证服务器获取 Token。
     */
    TOKEN_INCORRECT(2004, "Token is incorrect."),

    /**
     * App Key 不可用。
     * <p/>
     * 可能是错误的 App Key，或者 App Key 被服务器积极拒绝。
     */
    APP_KEY_UNAVAILABLE(2005, "App key is unavailable."),

    /**
     * 数据库错误。
     */
    DATABASE_ERROR(2006, "Database is error"),


    /**
     * 发送处理失败。
     */
    HANDLER_EXP(-2, "Handler exp."),


    /**
     * 服务器超时。
     */
    TIMEOUT(3001, "Server is timed out.");

    private int code;
    private String msg;

    /**
     * 构造函数。
     *
     * @param code 错误代码。
     * @param msg  错误消息。
     */
    ErrorCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    /**
     * 获取错误代码值。
     *
     * @return 错误代码值。
     */
    public int getValue() {
        return this.code;
    }

    /**
     * 获取错误消息。
     *
     * @return 错误消息。
     */
    public String getMessage() {
        return this.msg;
    }


    /**
     * 设置错误信息。
     * @param msg   错误信息。
     */
    public void setMessage(String msg){
        this.msg = msg;
    }

    /**
     * 设置错误代码值。
     *
     * @param code 错误代码。
     * @return 错误代码枚举。
     */
    public static ErrorCode setValue(int code) {
        for (ErrorCode c : ErrorCode.values()) {
            if (code == c.getValue()) {
                return c;
            }
        }

        Log.d("RongIMClient", "ConnectCallback---ErrorCode---code:" + code);

        return UNKNOWN;
    }
}

