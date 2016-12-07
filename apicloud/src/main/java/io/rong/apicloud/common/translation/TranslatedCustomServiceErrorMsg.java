package io.rong.apicloud.common.translation;

/**
 * Created by wangmingqiang on 16/8/26.
 */
public class TranslatedCustomServiceErrorMsg {
    int errorCode;
    String errorMsg;
    public TranslatedCustomServiceErrorMsg(int code, String msg) {
        errorCode = code;
        errorMsg = msg;
    }
}
