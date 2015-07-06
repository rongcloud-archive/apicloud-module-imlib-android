package io.rong.apicloud;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.uzmap.pkg.uzcore.UZWebView;
import com.uzmap.pkg.uzcore.annotation.UzJavascriptMethod;
import com.uzmap.pkg.uzcore.uzmodule.UZModule;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.rong.imlib.AnnotationNotFoundException;
import io.rong.imlib.RongIMClient;
import io.rong.message.CommandNotificationMessage;
import io.rong.message.ImageMessage;
import io.rong.message.LocationMessage;
import io.rong.message.RichContentMessage;
import io.rong.message.TextMessage;
import io.rong.message.VoiceMessage;

/**
 * Created by DragonJ on 14/12/16.
 */
public class RongIMClientModule extends UZModule {

    static RongIMClient mRongClient;
    Context mContext;
    Gson mGson;
    Handler mHandler;
    MessageListener mMessageListener;

    class MessageListener implements RongIMClient.OnReceiveMessageListener {
        UZModuleContext context;

        MessageListener(UZModuleContext context) {
            this.context = context;
        }


        @Override
        public boolean onReceived(io.rong.imlib.model.Message message, int i) {
            callModuleSuccess(context, new ReceiverMessageModel(i, new Message(message)), false);
            return false;
        }
    }


    public RongIMClientModule(UZWebView webView) {
        super(webView);
        mContext = getContext();
        mGson = new Gson();
        HandlerThread thread = new HandlerThread("RongWork");
        thread.start();
        mHandler = new Handler(thread.getLooper());
    }

    @UzJavascriptMethod
    public void jsmethod_init(final UZModuleContext context) {

        if (TextUtils.isEmpty(getFeatureValue("rongCloud", "appKey"))) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }

        RongIMClient.init(mContext);

        try {
            RongIMClient.registerMessageType(RichContentMessage.class);
        } catch (AnnotationNotFoundException e) {
            e.printStackTrace();
        }
        callModuleSuccess(context, null);
    }

    @UzJavascriptMethod
    public void jsmethod_connect(final UZModuleContext context) {

        String token = context.optString("token");

        if (TextUtils.isEmpty(token)) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }

        final RongResult<String> result = new RongResult<String>();
        try {
            mRongClient = RongIMClient.connect(token, new RongIMClient.ConnectCallback() {
                @Override
                public void onSuccess(String s) {
                    if (mMessageListener != null) {
                        mRongClient.setOnReceiveMessageListener(mMessageListener);
                    }
                    callModuleSuccess(context, new ConnectResult(s));
                }

                @Override
                public void onTokenIncorrect() {
                    callModuleError(context, RongIMClient.ErrorCode.RC_CONN_USER_OR_PASSWD_ERROR.getValue());
                }

                @Override
                public void onError(RongIMClient.ErrorCode errorCode) {
                    callModuleError(context, errorCode.getValue());
                }

            });
        } catch (Exception e) {
            e.printStackTrace();
            callModuleError(context, new RongException(e));
        }
    }

    public static class ConnectResult {
        String userId;

        public ConnectResult(String userId) {
            this.userId = userId;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }
    }

    @UzJavascriptMethod
    public void jsmethod_reconnect(final UZModuleContext context) {
        if (mRongClient == null) {
            callModuleError(context, new RongException(ErrorCode.NOT_CONNECTED));
            return;
        }

        mRongClient.disconnect();

        mRongClient.reconnect(new RongIMClient.ConnectCallback() {
            @Override
            public void onSuccess(String s) {
                callModuleSuccess(context, new ConnectResult(s));
            }

            @Override
            public void onTokenIncorrect() {
                callModuleError(context, RongIMClient.ErrorCode.RC_CONN_USER_OR_PASSWD_ERROR.getValue());
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                callModuleError(context, errorCode.getValue());
            }
        });
    }

    @UzJavascriptMethod
    public void jsmethod_disconnect(final UZModuleContext context) {
        if (mRongClient == null) {
            callModuleError(context, new RongException(ErrorCode.NOT_CONNECTED));
            return;
        }

        boolean isReceivePush = context.optBoolean("isReceivePush", true);

        mRongClient.disconnect(isReceivePush);
        callModuleSuccess(context, null);
    }

    @UzJavascriptMethod
    public void jsmethod_getConversationList(final UZModuleContext context) {
        if (mRongClient == null) {
            callModuleError(context, new RongException(ErrorCode.NOT_CONNECTED));
            return;
        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                List<io.rong.imlib.model.Conversation> conversations = mRongClient.getConversationList();
                callModuleSuccess(context, tranConversations(conversations));
            }
        });


    }

    @UzJavascriptMethod
    public void jsmethod_setOnReceiveMessageListener(final UZModuleContext context) {
        if (mRongClient == null) {
            mMessageListener = new MessageListener(context);
        } else {
            mRongClient.setOnReceiveMessageListener(new RongIMClient.OnReceiveMessageListener() {
                @Override
                public boolean onReceived(final io.rong.imlib.model.Message message, int i) {
                    callModuleSuccess(context, new ReceiverMessageModel(i, new Message(message)), false);
                    return false;
                }
            });
        }
    }

    public static class ReceiverMessageModel {
        int left;
        Message message;

        public ReceiverMessageModel(int left, Message message) {
            this.left = left;
            this.message = message;
        }

        public Message getMessage() {
            return message;
        }

        public void setMessage(Message message) {
            this.message = message;
        }

        public int getLeft() {
            return left;
        }

        public void setLeft(int left) {
            this.left = left;
        }
    }

    @UzJavascriptMethod
    public void jsmethod_getGroupConversationList(final UZModuleContext context) {

        if (mRongClient == null) {
            callModuleError(context, new RongException(ErrorCode.NOT_CONNECTED));
            return;
        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                List<io.rong.imlib.model.Conversation> result = mRongClient.getConversationList(io.rong.imlib.model.Conversation.ConversationType.GROUP);
                callModuleSuccess(context, tranConversations(result));
            }
        });
    }

    @UzJavascriptMethod
    public void jsmethod_getConversation(final UZModuleContext context) {
        final String type = context.optString("conversationType", null);
        final String targetId = context.optString("targetId", null);

        if (TextUtils.isEmpty(type) || TextUtils.isEmpty(targetId)) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }

        if (mRongClient == null) {
            callModuleError(context, new RongException(ErrorCode.NOT_CONNECTED));
            return;
        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                io.rong.imlib.model.Conversation.ConversationType conversationType = io.rong.imlib.model.Conversation.ConversationType.valueOf(type);

                io.rong.imlib.model.Conversation conversation = mRongClient.getConversation(conversationType, targetId);
                if (conversation == null)
                    callModuleSuccess(context, null);
                else
                    callModuleSuccess(context, new Conversation(conversation));
            }
        });
    }

    @UzJavascriptMethod
    public void jsmethod_removeConversation(final UZModuleContext context) {
        String type = context.optString("conversationType", null);
        String targetId = context.optString("targetId", null);

        if (TextUtils.isEmpty(type) || TextUtils.isEmpty(targetId)) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }

        if (mRongClient == null) {
            callModuleError(context, new RongException(ErrorCode.NOT_CONNECTED));
            return;
        }

        io.rong.imlib.model.Conversation.ConversationType conversationType = io.rong.imlib.model.Conversation.ConversationType.valueOf(type);

        boolean result = mRongClient.removeConversation(conversationType, targetId);
        if (result) {
            callModuleSuccess(context, null);
        } else {
            callModuleError(context);
        }
    }

    @UzJavascriptMethod
    public void jsmethod_setConversationToTop(final UZModuleContext context) {
        String type = context.optString("conversationType", null);
        String targetId = context.optString("targetId", null);
        boolean isTop = context.optBoolean("isTop", true);

        if (TextUtils.isEmpty(type) || TextUtils.isEmpty(targetId)) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }

        if (mRongClient == null) {
            callModuleError(context, new RongException(ErrorCode.NOT_CONNECTED));
            return;
        }

        io.rong.imlib.model.Conversation.ConversationType conversationType = io.rong.imlib.model.Conversation.ConversationType.valueOf(type);

        boolean result = mRongClient.setConversationToTop(conversationType, targetId, isTop);

        if (result)
            callModuleSuccess(context, result);
        else
            callModuleError(context);
    }

    @UzJavascriptMethod
    public void jsmethod_getTotalUnreadCount(final UZModuleContext context) {
        if (mRongClient == null) {
            callModuleError(context, new RongException(ErrorCode.NOT_CONNECTED));
            return;
        }

        int count = mRongClient.getTotalUnreadCount();

        callModuleSuccess(context, count);
    }

    @UzJavascriptMethod
    public void jsmethod_getUnreadCount(final UZModuleContext context) {
        String type = context.optString("conversationType", null);
        String targetId = context.optString("targetId", null);
        JSONArray jsonArray = context.optJSONArray("conversationTypes");


        if ((TextUtils.isEmpty(type) || TextUtils.isEmpty(targetId)) && (jsonArray == null || jsonArray.length() == 0)) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }

        if (mRongClient == null) {
            callModuleError(context, new RongException(ErrorCode.NOT_CONNECTED));
            return;
        }

        if (!TextUtils.isEmpty(type) && !TextUtils.isEmpty(targetId)) {

            io.rong.imlib.model.Conversation.ConversationType conversationType = io.rong.imlib.model.Conversation.ConversationType.valueOf(type);

            int count = mRongClient.getUnreadCount(conversationType, targetId);

            callModuleSuccess(context, count);
        } else {
            int i = 0;

            io.rong.imlib.model.Conversation.ConversationType[] conversationTypes = new io.rong.imlib.model.Conversation.ConversationType[jsonArray.length()];
            while (i < jsonArray.length()) {
                String item = jsonArray.optString(i);
                io.rong.imlib.model.Conversation.ConversationType conversationType = io.rong.imlib.model.Conversation.ConversationType.valueOf(item);
                conversationTypes[i] = conversationType;
                i++;
            }


            int count = mRongClient.getUnreadCount((io.rong.imlib.model.Conversation.ConversationType[]) conversationTypes);

            callModuleSuccess(context, count);
        }
    }

    @UzJavascriptMethod
    public void jsmethod_getUnreadCountByConversationTypes(final UZModuleContext context) {
        jsmethod_getUnreadCount(context);
    }

    @UzJavascriptMethod
    public void jsmethod_getLatestMessages(final UZModuleContext context) {
        final String type = context.optString("conversationType", null);
        final String targetId = context.optString("targetId", null);
        final int count = context.optInt("count", 20);

        if (TextUtils.isEmpty(type) || TextUtils.isEmpty(targetId)) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }

        if (mRongClient == null) {
            callModuleError(context, new RongException(ErrorCode.NOT_CONNECTED));
            return;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {

                io.rong.imlib.model.Conversation.ConversationType conversationType = io.rong.imlib.model.Conversation.ConversationType.valueOf(type);

                List<io.rong.imlib.model.Message> messages = mRongClient.getLatestMessages(conversationType, targetId, count);

                callModuleSuccess(context, tranMessages(messages));
            }
        });
    }

    @UzJavascriptMethod
    public void jsmethod_getHistoryMessages(final UZModuleContext context) {
        final String type = context.optString("conversationType", null);
        final String targetId = context.optString("targetId", null);
        final int oldestMessageId = context.optInt("oldestMessageId", -1);
        final int count = context.optInt("count", 20);

        if (TextUtils.isEmpty(type) || TextUtils.isEmpty(targetId)) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }

        if (mRongClient == null) {
            callModuleError(context, new RongException(ErrorCode.NOT_CONNECTED));
            return;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                io.rong.imlib.model.Conversation.ConversationType conversationType = io.rong.imlib.model.Conversation.ConversationType.valueOf(type);

                List<io.rong.imlib.model.Message> messages = mRongClient.getHistoryMessages(conversationType, targetId, oldestMessageId, count);

                callModuleSuccess(context, tranMessages(messages));
            }
        });

    }

    @UzJavascriptMethod
    public void jsmethod_getHistoryMessagesByObjectName(final UZModuleContext context) {
        final String type = context.optString("conversationType", null);
        final String targetId = context.optString("targetId", null);
        final int oldestMessageId = context.optInt("oldestMessageId", -1);
        final String objectName = context.optString("objectName", null);
        final int count = context.optInt("count", 20);

        if (TextUtils.isEmpty(type) || TextUtils.isEmpty(targetId)) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }

        if (mRongClient == null) {
            callModuleError(context, new RongException(ErrorCode.NOT_CONNECTED));
            return;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                io.rong.imlib.model.Conversation.ConversationType conversationType = io.rong.imlib.model.Conversation.ConversationType.valueOf(type);

                List<io.rong.imlib.model.Message> messages = mRongClient.getHistoryMessages(conversationType, targetId, objectName, oldestMessageId, count);

                callModuleSuccess(context, tranMessages(messages));
            }
        });

    }

    @UzJavascriptMethod
    public void jsmethod_deleteMessages(final UZModuleContext context) {
        JSONArray jsonArray = context.optJSONArray("messageIds");

        if (jsonArray == null || jsonArray.length() == 0) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }

        if (mRongClient == null) {
            callModuleError(context, new RongException(ErrorCode.NOT_CONNECTED));
            return;
        }

        int[] ids = new int[jsonArray.length()];
        int i = 0;
        while (i < jsonArray.length()) {
            ids[i] = jsonArray.optInt(i);
            i++;
        }

        boolean result = mRongClient.deleteMessages(ids);

        if (result)
            callModuleSuccess(context, result);
        else
            callModuleError(context);
    }

    @UzJavascriptMethod
    public void jsmethod_clearMessages(final UZModuleContext context) {
        String type = context.optString("conversationType", null);
        String targetId = context.optString("targetId", null);

        if (TextUtils.isEmpty(type) || TextUtils.isEmpty(targetId)) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }

        if (mRongClient == null) {
            callModuleError(context, new RongException(ErrorCode.NOT_CONNECTED));
            return;
        }

        io.rong.imlib.model.Conversation.ConversationType conversationType = io.rong.imlib.model.Conversation.ConversationType.valueOf(type);

        boolean result = mRongClient.clearMessages(conversationType, targetId);


        if (result)
            callModuleSuccess(context, result);
        else
            callModuleError(context);
    }

    @UzJavascriptMethod
    public void jsmethod_clearMessagesUnreadStatus(final UZModuleContext context) {
        String type = context.optString("conversationType", null);
        String targetId = context.optString("targetId", null);

        if (TextUtils.isEmpty(type) || TextUtils.isEmpty(targetId)) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }

        if (mRongClient == null) {
            callModuleError(context, new RongException(ErrorCode.NOT_CONNECTED));
            return;
        }

        io.rong.imlib.model.Conversation.ConversationType conversationType = io.rong.imlib.model.Conversation.ConversationType.valueOf(type);

        boolean result = mRongClient.clearMessagesUnreadStatus(conversationType, targetId);


        if (result)
            callModuleSuccess(context, result);
        else
            callModuleError(context);
    }

    @UzJavascriptMethod
    public void jsmethod_setMessageExtra(final UZModuleContext context) {
        int messageId = context.optInt("messageId", -1);
        String value = context.optString("value", null);

        if (messageId < 0 || TextUtils.isEmpty(value)) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }

        if (mRongClient == null) {
            callModuleError(context, new RongException(ErrorCode.NOT_CONNECTED));
            return;
        }


        boolean result = mRongClient.setMessageExtra(messageId, value);

        if (result)
            callModuleSuccess(context, result);
        else
            callModuleError(context);
    }

    @UzJavascriptMethod
    public void jsmethod_setMessageReceivedStatus(final UZModuleContext context) {
        int messageId = context.optInt("messageId", 0);
        int status = context.optInt("receivedStatus", -1);

        if (messageId < 1 || status < 0) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }

        if (mRongClient == null) {
            callModuleError(context, new RongException(ErrorCode.NOT_CONNECTED));
            return;
        }

        io.rong.imlib.model.Message.ReceivedStatus receivedStatus = new io.rong.imlib.model.Message.ReceivedStatus(status);

        boolean result = mRongClient.setMessageReceivedStatus(messageId, receivedStatus);

        if (result)
            callModuleSuccess(context, null);
        else
            callModuleError(context);
    }


    @UzJavascriptMethod
    public void jsmethod_getTextMessageDraft(final UZModuleContext context) {
        String type = context.optString("conversationType", null);
        String targetId = context.optString("targetId", null);

        if (TextUtils.isEmpty(type) || TextUtils.isEmpty(targetId)) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }

        if (mRongClient == null) {
            callModuleError(context, new RongException(ErrorCode.NOT_CONNECTED));
            return;
        }

        io.rong.imlib.model.Conversation.ConversationType conversationType = io.rong.imlib.model.Conversation.ConversationType.valueOf(type);

        String result = mRongClient.getTextMessageDraft(conversationType, targetId);

        callModuleSuccess(context, result);
    }

    @UzJavascriptMethod
    public void jsmethod_saveTextMessageDraft(final UZModuleContext context) {
        String type = context.optString("conversationType", null);
        String targetId = context.optString("targetId", null);
        String content = context.optString("content", null);

        if (TextUtils.isEmpty(type) || TextUtils.isEmpty(targetId) || TextUtils.isEmpty(content)) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }

        if (mRongClient == null) {
            callModuleError(context, new RongException(ErrorCode.NOT_CONNECTED));
            return;
        }

        io.rong.imlib.model.Conversation.ConversationType conversationType = io.rong.imlib.model.Conversation.ConversationType.valueOf(type);

        boolean result = mRongClient.saveTextMessageDraft(conversationType, targetId, content);
        if (result)
            callModuleSuccess(context, null);
        else
            callModuleError(context);
    }


    @UzJavascriptMethod
    public void jsmethod_clearTextMessageDraft(final UZModuleContext context) {
        String type = context.optString("conversationType", null);
        String targetId = context.optString("targetId", null);

        if (TextUtils.isEmpty(type) || TextUtils.isEmpty(targetId)) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }

        if (mRongClient == null) {
            callModuleError(context, new RongException(ErrorCode.NOT_CONNECTED));
            return;
        }

        io.rong.imlib.model.Conversation.ConversationType conversationType = io.rong.imlib.model.Conversation.ConversationType.valueOf(type);

        boolean result = mRongClient.clearTextMessageDraft(conversationType, targetId);

        if (result)
            callModuleSuccess(context, null);
        else
            callModuleError(context);


    }

    @UzJavascriptMethod
    public void jsmethod_createDiscussion(final UZModuleContext context) {
        String name = context.optString("name", null);
        JSONArray jsonArray = context.optJSONArray("userIdList");


        if (TextUtils.isEmpty(name) || jsonArray == null || jsonArray.length() == 0) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }

        if (mRongClient == null) {
            callModuleError(context, new RongException(ErrorCode.NOT_CONNECTED));
            return;
        }

        List<String> ids = new ArrayList<String>(jsonArray.length());
        int i = 0;
        while (i < jsonArray.length()) {
            ids.add(jsonArray.optString(i));
            i++;
        }


        mRongClient.createDiscussion(name, ids, new RongIMClient.CreateDiscussionCallback() {
            @Override
            public void onSuccess(String s) {
                callModuleSuccess(context, s);
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                callModuleError(context, errorCode.getValue());
            }
        });
    }


    @UzJavascriptMethod
    public void jsmethod_getDiscussion(final UZModuleContext context) {
        String discussionId = context.optString("discussionId", null);

        if (TextUtils.isEmpty(discussionId)) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }

        if (mRongClient == null) {
            callModuleError(context, new RongException(ErrorCode.NOT_CONNECTED));
            return;
        }


        mRongClient.getDiscussion(discussionId, new RongIMClient.ResultCallback<io.rong.imlib.model.Discussion>() {
            @Override
            public void onSuccess(io.rong.imlib.model.Discussion discussion) {
                callModuleSuccess(context, new Discussion(discussion));
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                callModuleError(context, errorCode.getValue());
            }
        });
    }

    @UzJavascriptMethod
    public void jsmethod_setDiscussionName(final UZModuleContext context) {
        String discussionId = context.optString("discussionId", null);
        String name = context.optString("name", null);

        if (TextUtils.isEmpty(discussionId) || TextUtils.isEmpty(name)) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }

        if (mRongClient == null) {
            callModuleError(context, new RongException(ErrorCode.NOT_CONNECTED));
            return;
        }


        mRongClient.setDiscussionName(discussionId, name, new RongIMClient.OperationCallback() {
            @Override
            public void onSuccess() {
                callModuleSuccess(context, null);
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                callModuleError(context, errorCode.getValue());
            }
        });
    }

    @UzJavascriptMethod
    public void jsmethod_addMemberToDiscussion(final UZModuleContext context) {
        String discussionId = context.optString("discussionId", null);
        JSONArray jsonArray = context.optJSONArray("userIdList");

        if (TextUtils.isEmpty(discussionId) || jsonArray == null || jsonArray.length() == 0) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }

        if (mRongClient == null) {
            callModuleError(context, new RongException(ErrorCode.NOT_CONNECTED));
            return;
        }

        List<String> ids = new ArrayList<String>(jsonArray.length());
        int i = 0;
        while (i < jsonArray.length()) {
            ids.add(jsonArray.optString(i));
            i++;
        }


        mRongClient.addMemberToDiscussion(discussionId, ids, new RongIMClient.OperationCallback() {
            @Override
            public void onSuccess() {
                callModuleSuccess(context, null);
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                callModuleError(context, errorCode.getValue());
            }
        });
    }

    @UzJavascriptMethod
    public void jsmethod_removeMemberFromDiscussion(final UZModuleContext context) {
        String discussionId = context.optString("discussionId", null);
        String userId = context.optString("userId", null);

        if (TextUtils.isEmpty(discussionId) || TextUtils.isEmpty(userId)) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }

        if (mRongClient == null) {
            callModuleError(context, new RongException(ErrorCode.NOT_CONNECTED));
            return;
        }


        mRongClient.removeMemberFromDiscussion(discussionId, userId, new RongIMClient.OperationCallback() {
            @Override
            public void onSuccess() {
                callModuleSuccess(context, null);
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                callModuleError(context, errorCode.getValue());
            }
        });
    }

    @UzJavascriptMethod
    public void jsmethod_quitDiscussion(final UZModuleContext context) {
        String discussionId = context.optString("discussionId", null);

        if (TextUtils.isEmpty(discussionId)) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }

        if (mRongClient == null) {
            callModuleError(context, new RongException(ErrorCode.NOT_CONNECTED));
            return;
        }


        mRongClient.quitDiscussion(discussionId, new RongIMClient.OperationCallback() {
            @Override
            public void onSuccess() {
                callModuleSuccess(context, null);
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                callModuleError(context, errorCode.getValue());
            }
        });
    }


    @UzJavascriptMethod
    public void jsmethod_sendTextMessage(final UZModuleContext context) {
        String type = context.optString("conversationType", null);
        String targetId = context.optString("targetId", null);
        String content = context.optString("text", null);
        String extra = context.optString("extra", null);

        if (TextUtils.isEmpty(type) || TextUtils.isEmpty(targetId) || TextUtils.isEmpty(content)) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }

        if (mRongClient == null) {
            callModuleError(context, new RongException(ErrorCode.NOT_CONNECTED));
            return;
        }


        io.rong.imlib.model.Conversation.ConversationType conversationType = io.rong.imlib.model.Conversation.ConversationType.valueOf(type);

        TextMessage textMessage = TextMessage.obtain(content);
        if (!TextUtils.isEmpty(extra))
            textMessage.setExtra(extra);

        mRongClient.sendMessage(conversationType, targetId, textMessage, null, new RongIMClient.SendMessageCallback() {
            @Override
            public void onError(Integer id, RongIMClient.ErrorCode errorCode) {
                callModuleError(context, new ProgressModel(id), errorCode.getValue());
            }

            @Override
            public void onSuccess(Integer id) {
                callModuleSuccess(context, new ProgressModel(id));
            }
        }, new RongIMClient.ResultCallback<io.rong.imlib.model.Message>() {
            @Override
            public void onSuccess(io.rong.imlib.model.Message message) {
                callModulePrepare(context, new ProgressModel(message));
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                callModuleError(context, errorCode.getValue());
            }
        });

    }

    @UzJavascriptMethod
    public void jsmethod_sendImageMessage(final UZModuleContext context) {
        String type = context.optString("conversationType", null);
        final String targetId = context.optString("targetId", null);
        String image = context.optString("imagePath", null);
        final String extra = context.optString("extra", null);

        if (TextUtils.isEmpty(type) || TextUtils.isEmpty(targetId) || TextUtils.isEmpty(image)) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }

        File file = new File(makeRealPath(image));
        final Uri imageUri = Uri.fromFile(file);

        if (!file.exists()) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }


        if (mRongClient == null) {
            callModuleError(context, new RongException(ErrorCode.NOT_CONNECTED));
            return;
        }

        final io.rong.imlib.model.Conversation.ConversationType conversationType = io.rong.imlib.model.Conversation.ConversationType.valueOf(type);


        mHandler.post(new Runnable() {
            @Override
            public void run() {

                final ImageMessage imageMessage = ImageMessage.obtain(imageUri, imageUri);

                if (!TextUtils.isEmpty(extra))
                    imageMessage.setExtra(extra);

                mRongClient.sendImageMessage(conversationType, targetId, imageMessage, null, new RongIMClient.SendImageMessageCallback() {
                    @Override
                    public void onAttached(io.rong.imlib.model.Message message) {
                        callModuleSuccess(context, new ProgressModel(message));
                    }

                    @Override
                    public void onError(io.rong.imlib.model.Message message, RongIMClient.ErrorCode errorCode) {
                        callModuleError(context, new ProgressModel(message.getMessageId()), errorCode.getValue());
                    }

                    @Override
                    public void onSuccess(io.rong.imlib.model.Message message) {
                        callModuleSuccess(context, new ProgressModel(message.getMessageId()));
                    }

                    @Override
                    public void onProgress(io.rong.imlib.model.Message message, int i) {
                        callModuleProgress(context, new ProgressModel(message.getMessageId(), i));

                    }
                });
            }
        });
    }

    public static class ProgressModel {
        Message message;
        Integer progress;

        public ProgressModel(int msgId, int progress) {
            message = new Message(msgId);
            this.progress = progress;
        }

        public ProgressModel(int msgId) {
            message = new Message(msgId);
        }

        public ProgressModel(io.rong.imlib.model.Message message) {
            this.message = new Message(message);
        }

        public ProgressModel(io.rong.imlib.model.Message message, int progress) {
            this.message = new Message(message);
            this.progress = progress;
        }

        public Message getMessage() {
            return message;
        }

        public void setMessage(Message message) {
            this.message = message;
        }

        public int getProgress() {
            return progress;
        }

        public void setProgress(int progress) {
            this.progress = progress;
        }
    }

    @UzJavascriptMethod
    public void jsmethod_sendVoiceMessage(final UZModuleContext context) {
        String type = context.optString("conversationType", null);
        final String targetId = context.optString("targetId", null);
        String voice = context.optString("voicePath", null);
        final int duration = context.optInt("duration", 0);
        final String extra = context.optString("extra", null);

        if (TextUtils.isEmpty(type) || TextUtils.isEmpty(targetId) || duration == 0 || TextUtils.isEmpty(voice)) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }

        final Uri voiceUri = Uri.fromFile(new File(makeRealPath(voice)));

        File file = new File(voice);

        if (!"file".equals(voiceUri.getScheme()) || !file.exists()) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }

        if (mRongClient == null) {
            callModuleError(context, new RongException(ErrorCode.NOT_CONNECTED));
            return;
        }


        final io.rong.imlib.model.Conversation.ConversationType conversationType = io.rong.imlib.model.Conversation.ConversationType.valueOf(type);


        mHandler.post(new Runnable() {
            @Override
            public void run() {


                VoiceMessage voiceMessage = VoiceMessage.obtain(voiceUri, duration);
                if (!TextUtils.isEmpty(extra))
                    voiceMessage.setExtra(extra);

                mRongClient.sendMessage(conversationType, targetId, voiceMessage, null, new RongIMClient.SendMessageCallback() {
                    @Override
                    public void onError(Integer id, RongIMClient.ErrorCode errorCode) {
                        callModuleError(context, new ProgressModel(id), errorCode.getValue());
                    }

                    @Override
                    public void onSuccess(Integer id) {
                        callModuleSuccess(context, new ProgressModel(id));
                    }
                }, new RongIMClient.ResultCallback<io.rong.imlib.model.Message>() {
                    @Override
                    public void onSuccess(io.rong.imlib.model.Message message) {
                        callModulePrepare(context, new ProgressModel(message));
                    }

                    @Override
                    public void onError(RongIMClient.ErrorCode errorCode) {
                        callModuleError(context, errorCode.getValue());
                    }
                });

            }
        });

    }

    @UzJavascriptMethod
    public void jsmethod_sendRichContentMessage(final UZModuleContext context) {
        String type = context.optString("conversationType", null);
        String targetId = context.optString("targetId", null);
        String title = context.optString("title", null);
        String content = context.optString("description", null);
        String imageUrl = context.optString("imageUrl", null);
        final String extra = context.optString("extra", null);

        if (TextUtils.isEmpty(type) || TextUtils.isEmpty(targetId) || TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }


        io.rong.imlib.model.Conversation.ConversationType conversationType = io.rong.imlib.model.Conversation.ConversationType.valueOf(type);

        RichContentMessage richContentMessage = RichContentMessage.obtain(title, content, imageUrl);
        if (!TextUtils.isEmpty(extra))
            richContentMessage.setExtra(extra);

        mRongClient.sendMessage(conversationType, targetId, richContentMessage, null, new RongIMClient.SendMessageCallback() {
            @Override
            public void onError(Integer id, RongIMClient.ErrorCode errorCode) {
                callModuleError(context, new ProgressModel(id), errorCode.getValue());
            }

            @Override
            public void onSuccess(Integer id) {
                callModuleSuccess(context, new ProgressModel(id));
            }
        }, new RongIMClient.ResultCallback<io.rong.imlib.model.Message>() {
            @Override
            public void onSuccess(io.rong.imlib.model.Message message) {
                callModulePrepare(context, new ProgressModel(message));

            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                callModuleError(context, errorCode.getValue());
            }
        });

    }

    @UzJavascriptMethod
    public void jsmethod_sendLocationMessage(final UZModuleContext context) {
        String type = context.optString("conversationType", null);
        final String targetId = context.optString("targetId", null);

        final double lat = context.optDouble("latitude", 0);
        final double lng = context.optDouble("longitude", 0);
        final String poi = context.optString("poi", null);
        final String imagePath = context.optString("imagePath", null);
        final String extra = context.optString("extra", null);


        if (TextUtils.isEmpty(type) || TextUtils.isEmpty(targetId) || TextUtils.isEmpty(imagePath)) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }

        final io.rong.imlib.model.Conversation.ConversationType conversationType = io.rong.imlib.model.Conversation.ConversationType.valueOf(type);

        File file = new File(makeRealPath(imagePath));
        final Uri imageUri = Uri.fromFile(file);

        if (!file.exists()) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                LocationMessage locationMessage = LocationMessage.obtain(lat, lng, poi, imageUri);
                if (!TextUtils.isEmpty(extra))
                    locationMessage.setExtra(extra);
                mRongClient.sendMessage(conversationType, targetId, locationMessage, null, new RongIMClient.SendMessageCallback() {
                    @Override
                    public void onError(Integer id, RongIMClient.ErrorCode errorCode) {
                        callModuleError(context, new ProgressModel(id), errorCode.getValue());
                    }

                    @Override
                    public void onSuccess(Integer id) {
                        callModuleSuccess(context, new ProgressModel(id));
                    }
                }, new RongIMClient.ResultCallback<io.rong.imlib.model.Message>() {
                    @Override
                    public void onSuccess(io.rong.imlib.model.Message message) {
                        callModulePrepare(context, new ProgressModel(message));
                    }

                    @Override
                    public void onError(RongIMClient.ErrorCode errorCode) {
                        callModuleError(context, errorCode.getValue());
                    }
                });

            }
        });

    }

    @UzJavascriptMethod
    public void jsmethod_sendCommandNotificationMessage(final UZModuleContext context) {
        String type = context.optString("conversationType", null);
        String targetId = context.optString("targetId", null);

        String name = context.optString("name", null);
        String data = context.optString("data", null);

        if (TextUtils.isEmpty(type) || TextUtils.isEmpty(targetId) || TextUtils.isEmpty(name) || TextUtils.isEmpty(data)) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }


        io.rong.imlib.model.Conversation.ConversationType conversationType = io.rong.imlib.model.Conversation.ConversationType.valueOf(type);


        mRongClient.sendMessage(conversationType, targetId, CommandNotificationMessage.obtain(name, data) , null, new RongIMClient.SendMessageCallback() {
            @Override
            public void onError(Integer id, RongIMClient.ErrorCode errorCode) {
                callModuleError(context, new ProgressModel(id), errorCode.getValue());
            }

            @Override
            public void onSuccess(Integer id) {
                callModuleSuccess(context, new ProgressModel(id));
            }
        }, new RongIMClient.ResultCallback<io.rong.imlib.model.Message>() {
            @Override
            public void onSuccess(io.rong.imlib.model.Message message) {
                callModulePrepare(context, new ProgressModel(message));
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                callModuleError(context, errorCode.getValue());
            }
        });
    }


    @UzJavascriptMethod
    public void jsmethod_getConversationNotificationStatus(final UZModuleContext context) {
        String type = context.optString("conversationType", null);
        String targetId = context.optString("targetId", null);

        if (TextUtils.isEmpty(type) || TextUtils.isEmpty(targetId)) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }

        if (mRongClient == null) {
            callModuleError(context, new RongException(ErrorCode.NOT_CONNECTED));
            return;
        }


        io.rong.imlib.model.Conversation.ConversationType conversationType = io.rong.imlib.model.Conversation.ConversationType.valueOf(type);

        mRongClient.getConversationNotificationStatus(conversationType, targetId, new RongIMClient.ResultCallback<io.rong.imlib.model.Conversation.ConversationNotificationStatus>() {
            @Override
            public void onSuccess(io.rong.imlib.model.Conversation.ConversationNotificationStatus conversationNotificationStatus) {
                callModuleSuccess(context, new ConversationNotificationStatus(conversationNotificationStatus.getValue(), conversationNotificationStatus.toString()));

            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                callModuleError(context, errorCode.getValue());

            }
        });
    }

    @UzJavascriptMethod
    public void jsmethod_setConversationNotificationStatus(final UZModuleContext context) {
        String type = context.optString("conversationType", null);
        String targetId = context.optString("targetId", null);
        String status = context.optString("notificationStatus", null);

        if (TextUtils.isEmpty(type) || TextUtils.isEmpty(targetId) || TextUtils.isEmpty(status)) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }

        if (mRongClient == null) {
            callModuleError(context, new RongException(ErrorCode.NOT_CONNECTED));
            return;
        }

        io.rong.imlib.model.Conversation.ConversationType conversationType = io.rong.imlib.model.Conversation.ConversationType.valueOf(type);

        io.rong.imlib.model.Conversation.ConversationNotificationStatus conversationNotificationStatus = io.rong.imlib.model.Conversation.ConversationNotificationStatus.valueOf(status);

        mRongClient.setConversationNotificationStatus(conversationType, targetId, conversationNotificationStatus, new RongIMClient.ResultCallback<io.rong.imlib.model.Conversation.ConversationNotificationStatus>() {
            @Override
            public void onSuccess(io.rong.imlib.model.Conversation.ConversationNotificationStatus conversationNotificationStatus) {
                callModuleSuccess(context, new ConversationNotificationStatus(conversationNotificationStatus.getValue(), conversationNotificationStatus.toString()));
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                callModuleSuccess(context, errorCode.getValue());
            }
        });
    }

    public static class ConversationNotificationStatus {
        int code;
        String notificationStatus;

        public ConversationNotificationStatus(int code, String conversationNotificationStatus) {
            this.code = code;
            this.notificationStatus = conversationNotificationStatus;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getNotificationStatus() {
            return notificationStatus;
        }

        public void setNotificationStatus(String notificationStatus) {
            this.notificationStatus = notificationStatus;
        }
    }

    @UzJavascriptMethod
    public void jsmethod_setDiscussionInviteStatus(final UZModuleContext context) {
        String targetId = context.optString("discussionId", null);
        String status = context.optString("inviteStatus", null);

        if (TextUtils.isEmpty(targetId) || TextUtils.isEmpty(targetId) || TextUtils.isEmpty(status)) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }

        if (mRongClient == null) {
            callModuleError(context, new RongException(ErrorCode.NOT_CONNECTED));
            return;
        }

        RongIMClient.DiscussionInviteStatus discussionInviteStatus = RongIMClient.DiscussionInviteStatus.valueOf(status);


        mRongClient.setDiscussionInviteStatus(targetId, discussionInviteStatus, new RongIMClient.Callback() {
            @Override
            public void onSuccess() {
                callModuleSuccess(context, null);

            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                callModuleError(context, errorCode.getValue());

            }
        });
    }

    @UzJavascriptMethod
    public void jsmethod_syncGroup(final UZModuleContext context) {
        JSONArray object = context.optJSONArray("groups");

        if (object == null) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }

        if (mRongClient == null) {
            callModuleError(context, new RongException(ErrorCode.NOT_CONNECTED));
            return;
        }

        TypeToken<ArrayList<Group>> typeToken = new TypeToken<ArrayList<Group>>() {
        };

        List<Group> groups = mGson.fromJson(object.toString(), typeToken.getType());

        if (groups == null || groups.size() == 0) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }

        List<io.rong.imlib.model.Group> list = new ArrayList<io.rong.imlib.model.Group>(groups.size());
        for (Group group : groups) {
            io.rong.imlib.model.Group item = new io.rong.imlib.model.Group(group.getGroupId(), group.getGroupName(), null);
            list.add(item);
        }

        mRongClient.syncGroup(list, new RongIMClient.OperationCallback() {
            @Override
            public void onSuccess() {
                callModuleSuccess(context, null);
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                callModuleError(context, errorCode.getValue());
            }
        });


    }

    @UzJavascriptMethod
    public void jsmethod_joinGroup(final UZModuleContext context) {
        String groupId = context.optString("groupId", null);
        String groupName = context.optString("groupName", null);

        if (TextUtils.isEmpty(groupId) || TextUtils.isEmpty(groupName)) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }

        if (mRongClient == null) {
            callModuleError(context, new RongException(ErrorCode.NOT_CONNECTED));
            return;
        }


        mRongClient.joinGroup(groupId, groupName, new RongIMClient.OperationCallback() {
            @Override
            public void onSuccess() {
                callModuleSuccess(context, null);
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                callModuleError(context, errorCode.getValue());
            }
        });
    }


    @UzJavascriptMethod
    public void jsmethod_quitGroup(final UZModuleContext context) {
        String groupId = context.optString("groupId", null);

        if (TextUtils.isEmpty(groupId)) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }

        if (mRongClient == null) {
            callModuleError(context, new RongException(ErrorCode.NOT_CONNECTED));
            return;
        }


        mRongClient.quitGroup(groupId, new RongIMClient.OperationCallback() {
            @Override
            public void onSuccess() {
                callModuleSuccess(context, null);
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                callModuleError(context, errorCode.getValue());
            }
        });
    }

    @UzJavascriptMethod
    public void jsmethod_setConnectionStatusListener(final UZModuleContext context) {
        RongIMClient.setConnectionStatusListener(new RongIMClient.ConnectionStatusListener() {
            @Override
            public void onChanged(ConnectionStatus connectionStatus) {
                callModuleSuccess(context, new RongIMClientModule.ConnectionStatus(connectionStatus.getMessage(), connectionStatus.getValue()), false);
            }
        });
    }

    public static class ConnectionStatus {
        String connectionStatus;
        int code;

        public ConnectionStatus(String status, int code) {
            connectionStatus = status;
            this.code = code;
        }

        public String getConnectionStatus() {
            return connectionStatus;
        }

        public void setConnectionStatus(String connectionStatus) {
            this.connectionStatus = connectionStatus;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }
    }

    @UzJavascriptMethod
    public void jsmethod_joinChatRoom(final UZModuleContext context) {
        String chatRoomId = context.optString("chatRoomId", null);
        int defMessageCount = context.optInt("defMessageCount", 10);

        if (TextUtils.isEmpty(chatRoomId)) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }

        if (mRongClient == null) {
            callModuleError(context, new RongException(ErrorCode.NOT_CONNECTED));
            return;
        }


        mRongClient.joinChatRoom(chatRoomId, defMessageCount, new RongIMClient.OperationCallback() {
            @Override
            public void onSuccess() {
                callModuleSuccess(context, null);
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                callModuleError(context, errorCode.getValue());
            }
        });
    }

    @UzJavascriptMethod
    public void jsmethod_quitChatRoom(final UZModuleContext context) {
        String chatRoomId = context.optString("chatRoomId", null);

        if (TextUtils.isEmpty(chatRoomId)) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }

        if (mRongClient == null) {
            callModuleError(context, new RongException(ErrorCode.NOT_CONNECTED));
            return;
        }


        mRongClient.quitChatRoom(chatRoomId, new RongIMClient.OperationCallback() {
            @Override
            public void onSuccess() {
                callModuleSuccess(context, null);
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                callModuleError(context, errorCode.getValue());
            }
        });
    }

    @UzJavascriptMethod
    public void jsmethod_clearConversations(final UZModuleContext context) {
        JSONArray jsonArray = context.optJSONArray("conversationTypes");


        if (jsonArray == null || jsonArray.length() == 0) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }

        if (mRongClient == null) {
            callModuleError(context, new RongException(ErrorCode.NOT_CONNECTED));
            return;
        }

        int i = 0;

        io.rong.imlib.model.Conversation.ConversationType[] conversationTypes = new io.rong.imlib.model.Conversation.ConversationType[jsonArray.length()];
        while (i < jsonArray.length()) {
            String item = jsonArray.optString(i);
            io.rong.imlib.model.Conversation.ConversationType conversationType = io.rong.imlib.model.Conversation.ConversationType.valueOf(item);
            conversationTypes[i] = conversationType;
            i++;
        }

        boolean result = mRongClient.clearConversations((io.rong.imlib.model.Conversation.ConversationType[]) conversationTypes);

        if (result)
            callModuleSuccess(context, null);
        else
            callModuleError(context);


    }

    private final <T> void callModuleSuccess(UZModuleContext context, T model) {
        final RongResult<T> result = new RongResult<T>();
        result.setStatus(RongResult.Status.success);
        result.setResult(model);
        context.success(getJsonStringResult(result), true, true);
    }

    private final void callModuleError(UZModuleContext context, RongException e) {
        final RongResult result = new RongResult();
        result.setStatus(RongResult.Status.error);

        context.error(getJsonObjectResult(result), getJsonObjectResult(e), true);
    }

    private final void callModuleError(UZModuleContext context) {
        final RongResult result = new RongResult();
        result.setStatus(RongResult.Status.error);

        context.error(getJsonObjectResult(result), null, true);
    }

    private final <T> void callModuleError(UZModuleContext context, T modle, RongException e) {
        final RongResult result = new RongResult();
        result.setResult(modle);
        result.setStatus(RongResult.Status.error);

        context.error(getJsonObjectResult(result), getJsonObjectResult(e), true);
    }

    private final <T> void callModuleError(UZModuleContext context, T modle, int code) {
        final RongResult result = new RongResult();
        result.setStatus(RongResult.Status.error);
        result.setResult(modle);
        RongException e = new RongException(code);
        context.error(getJsonObjectResult(result), getJsonObjectResult(e), true);
    }

    private final void callModuleError(UZModuleContext context, int code) {
        final RongResult result = new RongResult();
        result.setStatus(RongResult.Status.error);
        RongException e = new RongException(code);
        context.error(getJsonObjectResult(result), getJsonObjectResult(e), true);
    }

    private final <T> void callModuleSuccess(UZModuleContext context, T model, boolean rmJsFunc) {
        final RongResult<T> result = new RongResult<T>();
        result.setStatus(RongResult.Status.success);
        result.setResult(model);
        context.success(getJsonStringResult(result), true, rmJsFunc);
    }

    private final <T> void callModuleProgress(UZModuleContext context, T model) {
        final RongResult<T> result = new RongResult<T>();
        result.setStatus(RongResult.Status.progress);
        result.setResult(model);
        context.success(getJsonStringResult(result), true, false);
    }

    private final <T> void callModulePrepare(UZModuleContext context, T model) {
        final RongResult<T> result = new RongResult<T>();
        result.setStatus(RongResult.Status.prepare);
        result.setResult(model);
        context.success(getJsonStringResult(result), true, false);
    }

    private final void callModuleError(UZModuleContext context, RongException e, boolean rmJsFunc) {
        final RongResult result = new RongResult();
        result.setStatus(RongResult.Status.error);
        context.error(getJsonObjectResult(result), getJsonObjectResult(e), rmJsFunc);
    }

    private final void callModuleError(UZModuleContext context, int code, boolean rmJsFunc) {
        final RongResult result = new RongResult();
        result.setStatus(RongResult.Status.error);
        RongException e = new RongException(code);
        context.error(getJsonObjectResult(result), getJsonObjectResult(e), rmJsFunc);
    }


    private final <T> JSONObject getJsonObjectResult(T result) {
        String json = mGson.toJson(result);
        Log.d("ApiCloud", json);
        JSONObject object = null;
        try {
            object = new JSONObject(json);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return object;
    }

    private final <T> String getJsonStringResult(T result) {
        String json = mGson.toJson(result);
        Log.d("ApiCloud", json);
        return json;
    }

    private final List<Message> tranMessages(List<io.rong.imlib.model.Message> messages) {
        if (messages == null || messages.size() == 0)
            return null;
        List<Message> results = new ArrayList<Message>(messages.size());
        for (io.rong.imlib.model.Message item : messages) {
            results.add(new Message(item));
        }
        return results;
    }

    private final List<Conversation> tranConversations(List<io.rong.imlib.model.Conversation> conversations) {
        if (conversations == null || conversations.size() == 0)
            return null;
        List<Conversation> results = new ArrayList<Conversation>(conversations.size());
        for (io.rong.imlib.model.Conversation item : conversations) {
            results.add(new Conversation(item));
        }
        return results;
    }
}
