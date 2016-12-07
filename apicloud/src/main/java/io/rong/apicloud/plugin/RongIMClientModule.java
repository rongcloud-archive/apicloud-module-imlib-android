package io.rong.apicloud.plugin;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.uzmap.pkg.uzcore.UZWebView;
import com.uzmap.pkg.uzcore.annotation.UzJavascriptMethod;
import com.uzmap.pkg.uzcore.uzmodule.UZModule;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import io.rong.common.ErrorCode;
import io.rong.common.RongCustomServiceResult;
import io.rong.common.RongException;
import io.rong.common.RongResult;
import io.rong.common.translation.ITranslatedMessage;
import io.rong.common.translation.TranslatedCSGroupItem;
import io.rong.common.translation.TranslatedCSGroupList;
import io.rong.common.translation.TranslatedConversation;
import io.rong.common.translation.TranslatedConversationNtfyStatus;
import io.rong.common.translation.TranslatedCustomServiceDialogID;
import io.rong.common.translation.TranslatedCustomServiceErrorMsg;
import io.rong.common.translation.TranslatedCustomServiceMode;
import io.rong.common.translation.TranslatedCustomServiceQuitMsg;
import io.rong.common.translation.TranslatedDiscussion;
import io.rong.common.translation.TranslatedMessage;
import io.rong.common.translation.TranslatedQuietHour;
import io.rong.imlib.AnnotationNotFoundException;
import io.rong.imlib.CustomServiceConfig;
import io.rong.imlib.ICustomServiceListener;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.CSCustomServiceInfo;
import io.rong.imlib.model.CSGroupItem;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.CustomServiceMode;
import io.rong.imlib.model.Discussion;
import io.rong.imlib.model.Group;
import io.rong.imlib.model.Message;
import io.rong.message.CommandMessage;
import io.rong.message.CommandNotificationMessage;
import io.rong.message.GroupNotificationMessage;
import io.rong.message.ImageMessage;
import io.rong.message.LocationMessage;
import io.rong.message.RichContentMessage;
import io.rong.message.TextMessage;
import io.rong.message.VoiceMessage;

public class RongIMClientModule extends UZModule {
    private final String TAG = "RongIMClientModule";

    static RongIMClient mRongClient;
    Context mContext;
    Gson mGson;
    Handler mHandler;
    static MessageListener mMessageListener;
    private HashMap<String, CustomServiceListener> customServiceCache = new HashMap<String, CustomServiceListener>();

    private TranslatedMessage translateMessage(Message message) {
        return new TranslatedMessage(message);
    }

    private boolean isInBackground() {
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        String appPackageName = mContext.getPackageName();
        List<ActivityManager.RunningTaskInfo> runningTaskInfo = activityManager.getRunningTasks(1);
        String topAppPackageName = runningTaskInfo.get(0).topActivity.getPackageName();
        return !appPackageName.equals(topAppPackageName);
    }

    private void notifyIfNeed(UZModuleContext context, Message message, int left) {

        if (isInQuietTime(mContext)) {
            return;
        }

        RongIMClient.getInstance().getConversationNotificationStatus(message.getConversationType(), message.getTargetId(), new RongIMClient.ResultCallback<Conversation.ConversationNotificationStatus>() {
            @Override
            public void onSuccess(Conversation.ConversationNotificationStatus conversationNotificationStatus) {
                if (Conversation.ConversationNotificationStatus.NOTIFY == conversationNotificationStatus) {
                    sendNotification();
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {

            }
        });
    }

    private void sendNotification() {
        Notification notification;
        Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(mContext.getPackageName());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PackageManager pm = mContext.getPackageManager();
        ApplicationInfo ai = mContext.getApplicationInfo();
        String title = (String) pm.getApplicationLabel(ai);
        String tickerText = mContext.getResources().getString(mContext.getResources().getIdentifier("rc_notification_ticker_text", "string", mContext.getPackageName()));
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
        if (android.os.Build.VERSION.SDK_INT < 11) {
            try {
                notification = new Notification(ai.icon, tickerText, System.currentTimeMillis());
                Method method;
                Class<?> classType = Notification.class;
                method = classType.getMethod("setLatestEventInfo", new Class[]{Context.class, CharSequence.class, CharSequence.class, PendingIntent.class});
                method.invoke(notification, new Object[]{mContext, title, tickerText, pendingIntent});
                notification.flags = Notification.FLAG_AUTO_CANCEL;
                notification.defaults = Notification.DEFAULT_SOUND;
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        } else {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) ai.loadIcon(pm);
            Bitmap appIcon = bitmapDrawable.getBitmap();
            Notification.Builder builder = new Notification.Builder(mContext);
            builder.setLargeIcon(appIcon);
            builder.setSmallIcon(mContext.getApplicationInfo().icon);
            builder.setTicker(tickerText);
            builder.setContentTitle(title);
            builder.setContentText(tickerText);
            builder.setContentIntent(pendingIntent);
            builder.setAutoCancel(true);
            builder.setDefaults(Notification.DEFAULT_ALL);
            notification = builder.getNotification();
        }
        NotificationManager nm = (NotificationManager) mContext.getSystemService(mContext.NOTIFICATION_SERVICE);
        nm.notify(0, notification);
    }

    class MessageListener implements RongIMClient.OnReceiveMessageListener {
        UZModuleContext context;

        MessageListener(UZModuleContext context) {
            this.context = context;
        }

        @Override
        public boolean onReceived(Message message, int left) {
            Log.d(TAG, "onReceived " + message.getObjectName());

            if (isInBackground() && !notificationDisabled) {
                notifyIfNeed(context, message, left);
            }
            TranslatedMessage msg = translateMessage(message);
            callModule(context, new ReceiveMessageModel(left, msg), false);
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

    private boolean notificationDisabled = false;

    @UzJavascriptMethod
    public void jsmethod_disableLocalNotification(UZModuleContext context) {
        notificationDisabled = true;
        callModuleSuccess(context, null);
    }

    @UzJavascriptMethod
    public void jsmethod_init(final UZModuleContext context) {

        String key = getFeatureValue("rongCloud20", "appKey");
        if (TextUtils.isEmpty(key)) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }

        RongIMClient.init(mContext, key);
        try {
            RongIMClient.registerMessageType(GroupNotificationMessage.class);
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

    public class ConnectionStatusResult {
        AdaptConnectionStatus connectionStatus;

        public ConnectionStatusResult(int code) {
            connectionStatus = AdaptConnectionStatus.setValue(code);
        }
    }

    @UzJavascriptMethod
    public void jsmethod_logout(final UZModuleContext context) {
        if (mRongClient == null) {
            callModuleError(context, new RongException(ErrorCode.NOT_CONNECTED));
            return;
        }

        mRongClient.logout();
        callModuleSuccess(context, null);
    }

    @UzJavascriptMethod
    public void jsmethod_disconnect(final UZModuleContext context) {
        if (mRongClient == null) {
            callModuleError(context, new RongException(ErrorCode.NOT_CONNECTED));
            return;
        }

        boolean isReceivePush = context.optBoolean("isReceivePush", true);
        Log.e("RongLog", isReceivePush + "" );
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
                mRongClient.getConversationList(new RongIMClient.ResultCallback<List<Conversation>>() {
                    @Override
                    public void onSuccess(List<Conversation> conversations) {
                        ArrayList<TranslatedConversation> list = new ArrayList<TranslatedConversation>();
                        if (conversations == null || conversations.size() == 0) {
                            callModuleSuccess(context, list);
                            return;
                        }

                        for (Conversation conversation : conversations) {
                            TranslatedConversation tc = new TranslatedConversation(conversation);
                            list.add(tc);
                        }
                        callModuleSuccess(context, list);
                    }

                    @Override
                    public void onError(RongIMClient.ErrorCode e) {
                        callModuleError(context, new RongException(e.getValue()));
                    }
                });
            }
        });
    }

    @UzJavascriptMethod
    public void jsmethod_setOnReceiveMessageListener(final UZModuleContext context) {
        mMessageListener = new MessageListener(context);

        if (mRongClient != null) {
            mRongClient.setOnReceiveMessageListener(mMessageListener);
        }
    }

    public static class ReceiveMessageModel {
        int left;
        ITranslatedMessage message;

        public ReceiveMessageModel(int left, ITranslatedMessage message) {
            this.left = left;
            this.message = message;
        }

        public ReceiveMessageModel(ITranslatedMessage message) {
            this.message = message;
        }

        public ITranslatedMessage getMessage() {
            return message;
        }

        public void setMessage(ITranslatedMessage message) {
            this.message = message;
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
                mRongClient.getConversationList(new RongIMClient.ResultCallback<List<Conversation>>() {
                    @Override
                    public void onSuccess(List<Conversation> conversations) {
                        if (conversations == null || conversations.size() == 0) {
                            callModuleSuccess(context, "");
                            return;
                        }

                        ArrayList<TranslatedConversation> list = new ArrayList<TranslatedConversation>();
                        for (Conversation conversation : conversations) {
                            TranslatedConversation tc = new TranslatedConversation(conversation);
                            list.add(tc);
                        }
                        callModuleSuccess(context, list);
                    }

                    @Override
                    public void onError(RongIMClient.ErrorCode e) {
                        callModuleError(context, new RongException(e.getValue()));
                    }
                });
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
                Conversation.ConversationType conversationType = Conversation.ConversationType.valueOf(type);

                mRongClient.getConversation(conversationType, targetId, new RongIMClient.ResultCallback<Conversation>() {
                    @Override
                    public void onSuccess(Conversation conversation) {
                        TranslatedConversation tc = null;
                        if (conversation == null) {
                            callModuleSuccess(context, "");
                        } else {
                            tc = new TranslatedConversation(conversation);
                            callModuleSuccess(context, tc);
                        }
                    }

                    @Override
                    public void onError(RongIMClient.ErrorCode e) {
                        callModuleError(context, new RongException(e.getValue()));
                    }
                });
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

        Conversation.ConversationType conversationType = Conversation.ConversationType.valueOf(type);

        mRongClient.removeConversation(conversationType, targetId, new RongIMClient.ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                callModuleSuccess(context, aBoolean);
            }

            @Override
            public void onError(RongIMClient.ErrorCode e) {
                callModuleError(context, new RongException(e.getValue()));
            }
        });
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

        Conversation.ConversationType conversationType = Conversation.ConversationType.valueOf(type);

        mRongClient.setConversationToTop(conversationType, targetId, isTop, new RongIMClient.ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                callModuleSuccess(context, aBoolean);
            }

            @Override
            public void onError(RongIMClient.ErrorCode e) {
                callModuleError(context, new RongException(e.getValue()));
            }
        });
    }

    @UzJavascriptMethod
    public void jsmethod_getTotalUnreadCount(final UZModuleContext context) {
        if (mRongClient == null) {
            callModuleError(context, new RongException(ErrorCode.NOT_CONNECTED));
            return;
        }

        mRongClient.getTotalUnreadCount(new RongIMClient.ResultCallback<Integer>() {
            @Override
            public void onSuccess(Integer integer) {
                callModuleSuccess(context, integer);
            }

            @Override
            public void onError(RongIMClient.ErrorCode e) {
                callModuleError(context, new RongException(e.getValue()));
            }
        });

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
            Conversation.ConversationType conversationType = Conversation.ConversationType.valueOf(type);
            mRongClient.getUnreadCount(conversationType, targetId, new RongIMClient.ResultCallback<Integer>() {
                @Override
                public void onSuccess(Integer integer) {
                    callModuleSuccess(context, integer);
                }

                @Override
                public void onError(RongIMClient.ErrorCode e) {
                    callModuleError(context, new RongException(e.getValue()));
                }
            });
        } else {
            int i = 0;

            Conversation.ConversationType[] conversationTypes = new Conversation.ConversationType[jsonArray.length()];
            while (i < jsonArray.length()) {
                String item = jsonArray.optString(i);
                Conversation.ConversationType conversationType = Conversation.ConversationType.valueOf(item);
                conversationTypes[i] = conversationType;
                i++;
            }

            mRongClient.getUnreadCount(conversationTypes, new RongIMClient.ResultCallback<Integer>() {
                @Override
                public void onSuccess(Integer integer) {
                    callModuleSuccess(context, integer);
                }

                @Override
                public void onError(RongIMClient.ErrorCode e) {
                    callModuleError(context, new RongException(e.getValue()));
                }
            });
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

                Conversation.ConversationType conversationType = Conversation.ConversationType.valueOf(type);

                mRongClient.getLatestMessages(conversationType, targetId, count, new RongIMClient.ResultCallback<List<Message>>() {
                    @Override
                    public void onSuccess(List<Message> messages) {
                        ArrayList<TranslatedMessage> list = new ArrayList<TranslatedMessage>();

                        if (messages == null || messages.size() == 0) {
                            callModuleSuccess(context, list);
                            return;
                        }
                        for (Message message : messages) {
                            TranslatedMessage tm = new TranslatedMessage(message);
                            list.add(tm);
                        }
                        callModuleSuccess(context, list);
                    }

                    @Override
                    public void onError(RongIMClient.ErrorCode e) {
                        callModuleError(context, new RongException(e.getValue()));
                    }
                });
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
                Conversation.ConversationType conversationType = Conversation.ConversationType.valueOf(type);

                mRongClient.getHistoryMessages(conversationType, targetId, oldestMessageId, count, new RongIMClient.ResultCallback<List<Message>>() {
                    @Override
                    public void onSuccess(List<Message> messages) {
                        if (messages == null || messages.size() == 0) {
                            callModuleSuccess(context, "");
                            return;
                        }

                        ArrayList<TranslatedMessage> list = new ArrayList<TranslatedMessage>();
                        for (Message message : messages) {
                            TranslatedMessage tm = new TranslatedMessage(message);
                            list.add(tm);
                        }
                        callModuleSuccess(context, list);
                    }

                    @Override
                    public void onError(RongIMClient.ErrorCode e) {
                        callModuleError(context, new RongException(e.getValue()));
                    }
                });
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
                Conversation.ConversationType conversationType = Conversation.ConversationType.valueOf(type);

                mRongClient.getHistoryMessages(conversationType, targetId, objectName, oldestMessageId, count, new RongIMClient.ResultCallback<List<Message>>() {
                    @Override
                    public void onSuccess(List<Message> messages) {
                        if (messages == null || messages.size() == 0) {
                            callModuleSuccess(context, "");
                            return;
                        }

                        ArrayList<TranslatedMessage> list = new ArrayList<TranslatedMessage>();
                        for (Message message : messages) {
                            TranslatedMessage tm = new TranslatedMessage(message);
                            list.add(tm);
                        }
                        callModuleSuccess(context, list);
                    }

                    @Override
                    public void onError(RongIMClient.ErrorCode e) {
                        callModuleError(context, new RongException(e.getValue()));
                    }
                });
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

        mRongClient.deleteMessages(ids, new RongIMClient.ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                callModuleSuccess(context, aBoolean);
            }

            @Override
            public void onError(RongIMClient.ErrorCode e) {
                callModuleError(context, new RongException(e.getValue()));
            }
        });
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

        Conversation.ConversationType conversationType = Conversation.ConversationType.valueOf(type);
        mRongClient.clearMessages(conversationType, targetId, new RongIMClient.ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                callModuleSuccess(context, aBoolean);
            }

            @Override
            public void onError(RongIMClient.ErrorCode e) {
                callModuleError(context, new RongException(e.getValue()));
            }
        });
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

        Conversation.ConversationType conversationType = Conversation.ConversationType.valueOf(type);
        mRongClient.clearMessagesUnreadStatus(conversationType, targetId, new RongIMClient.ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                callModuleSuccess(context, aBoolean);
            }

            @Override
            public void onError(RongIMClient.ErrorCode e) {
                callModuleError(context, new RongException(e.getValue()));
            }
        });
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

        mRongClient.setMessageExtra(messageId, value, new RongIMClient.ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                callModuleSuccess(context, aBoolean);
            }

            @Override
            public void onError(RongIMClient.ErrorCode e) {
                callModuleError(context, new RongException(e.getValue()));
            }
        });
    }

    @UzJavascriptMethod
    public void jsmethod_setMessageReceivedStatus(final UZModuleContext context) {
        int messageId = context.optInt("messageId", 0);
        String status = context.optString("receivedStatus", null);

        if (messageId < 1 || status == null) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }
        int value;
        if (status.equals("UNREAD"))
            value = 0;
        else if (status.equals("READ"))
            value = 1;
        else if (status.equals("LISTENED"))
            value = 2;
        else if (status.equals("DOWNLOADED"))
            value = 4;
        else {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }
        if (mRongClient == null) {
            callModuleError(context, new RongException(ErrorCode.NOT_CONNECTED));
            return;
        }

        Message.ReceivedStatus receivedStatus = new Message.ReceivedStatus(value);
        mRongClient.setMessageReceivedStatus(messageId, receivedStatus, new RongIMClient.ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                callModuleSuccess(context, aBoolean);
            }

            @Override
            public void onError(RongIMClient.ErrorCode e) {
                callModuleError(context, new RongException(e.getValue()));
            }
        });
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

        Conversation.ConversationType conversationType = Conversation.ConversationType.valueOf(type);
        mRongClient.getTextMessageDraft(conversationType, targetId, new RongIMClient.ResultCallback<String>() {
            @Override
            public void onSuccess(String content) {
                if (content == null)
                    callModuleSuccess(context, "");
                else
                    callModuleSuccess(context, content);
            }

            @Override
            public void onError(RongIMClient.ErrorCode e) {
                callModuleError(context, new RongException(e.getValue()));
            }
        });
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

        Conversation.ConversationType conversationType = Conversation.ConversationType.valueOf(type);
        mRongClient.saveTextMessageDraft(conversationType, targetId, content, new RongIMClient.ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                callModuleSuccess(context, aBoolean);
            }

            @Override
            public void onError(RongIMClient.ErrorCode e) {
                callModuleError(context, new RongException(e.getValue()));
            }
        });
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

        Conversation.ConversationType conversationType = Conversation.ConversationType.valueOf(type);

        mRongClient.clearTextMessageDraft(conversationType, targetId, new RongIMClient.ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                callModuleSuccess(context, aBoolean);
            }

            @Override
            public void onError(RongIMClient.ErrorCode e) {
                callModuleError(context, new RongException(e.getValue()));
            }
        });
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
                callModuleSuccess(context, new DiscussionModel(s));
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                callModuleError(context, errorCode.getValue());
            }
        });
    }

    private class DiscussionModel {
        String discussionId;
        DiscussionModel(String discussionId) {
            this.discussionId = discussionId;
        }
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

        mRongClient.getDiscussion(discussionId, new RongIMClient.ResultCallback<Discussion>() {
            @Override
            public void onSuccess(Discussion discussion) {
                TranslatedDiscussion td = null;
                if (discussion == null) {
                    callModuleSuccess(context, "");
                } else {
                    td = new TranslatedDiscussion(discussion);
                    callModuleSuccess(context, td);
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                callModuleError(context, new RongException(errorCode.getValue()));
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
                callModuleError(context, new RongException(errorCode.getValue()));
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
                callModuleError(context, new RongException(errorCode.getValue()));
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
                callModuleError(context, new RongException(errorCode.getValue()));
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
                callModuleError(context, new RongException(errorCode.getValue()));
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

        Conversation.ConversationType conversationType = Conversation.ConversationType.valueOf(type);

        TextMessage textMessage = TextMessage.obtain(content);
        if (!TextUtils.isEmpty(extra))
            textMessage.setExtra(extra);

        mRongClient.sendMessage(conversationType, targetId, textMessage, null, null, new RongIMClient.SendMessageCallback() {
            @Override
            public void onError(Integer id, RongIMClient.ErrorCode errorCode) {
                callModuleError(context, new ProgressModel(id), errorCode.getValue());
            }

            @Override
            public void onSuccess(Integer id) {
                callModuleSuccess(context, new ProgressModel(id));
            }
        }, new RongIMClient.ResultCallback<Message>() {
            @Override
            public void onSuccess(Message message) {
                TranslatedMessage translatedMessage = new TranslatedMessage(message);
                callModulePrepare(context, new ProgressModel(translatedMessage));
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                callModuleError(context, new RongException(errorCode.getValue()));
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

        final Conversation.ConversationType conversationType = Conversation.ConversationType.valueOf(type);

        mHandler.post(new Runnable() {
            @Override
            public void run() {

                final ImageMessage imageMessage = ImageMessage.obtain(imageUri, imageUri);

                if (!TextUtils.isEmpty(extra))
                    imageMessage.setExtra(extra);

                mRongClient.sendImageMessage(conversationType, targetId, imageMessage, null, null, new RongIMClient.SendImageMessageCallback() {
                    @Override
                    public void onAttached(Message message) {
                        TranslatedMessage translatedMessage = new TranslatedMessage(message);
                        callModulePrepare(context, new ProgressModel(translatedMessage));
                    }

                    @Override
                    public void onError(Message message, RongIMClient.ErrorCode errorCode) {
                        callModuleError(context, new ProgressModel(message.getMessageId()), errorCode.getValue());
                    }

                    @Override
                    public void onSuccess(Message message) {
                        callModuleSuccess(context, new ProgressModel(message.getMessageId()));
                    }

                    @Override
                    public void onProgress(Message message, int i) {
                        callModuleProgress(context, new ProgressModel(message.getMessageId(), i));
                    }
                });
            }
        });
    }

    public static class ProgressModel {
        TranslatedMessage message;
        Integer progress;

        public ProgressModel(int msgId, int progress) {
            message = new TranslatedMessage();
            message.setMessageId(msgId);
            this.progress = progress;
        }

        public ProgressModel(int msgId) {
            message = new TranslatedMessage();
            message.setMessageId(msgId);
        }

        public ProgressModel(TranslatedMessage message) {
            this.message = message;
        }

        public ProgressModel(TranslatedMessage message, int progress) {
            this.message = message;
            this.progress = progress;
        }

        public TranslatedMessage getMessage() {
            return message;
        }

        public void setMessage(TranslatedMessage message) {
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
        final Conversation.ConversationType conversationType = Conversation.ConversationType.valueOf(type);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                VoiceMessage voiceMessage = VoiceMessage.obtain(voiceUri, duration);
                if (!TextUtils.isEmpty(extra))
                    voiceMessage.setExtra(extra);

                mRongClient.sendMessage(conversationType, targetId, voiceMessage, null, null, new RongIMClient.SendMessageCallback() {
                    @Override
                    public void onError(Integer id, RongIMClient.ErrorCode errorCode) {
                        callModuleError(context, new ProgressModel(id), errorCode.getValue());
                    }

                    @Override
                    public void onSuccess(Integer id) {
                        callModuleSuccess(context, new ProgressModel(id));
                    }
                }, new RongIMClient.ResultCallback<Message>() {
                    @Override
                    public void onSuccess(Message message) {
                        TranslatedMessage translatedMessage = new TranslatedMessage(message);
                        callModulePrepare(context, new ProgressModel(translatedMessage));
                    }

                    @Override
                    public void onError(RongIMClient.ErrorCode errorCode) {
                        callModuleError(context, new RongException(errorCode.getValue()));
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
        Conversation.ConversationType conversationType = Conversation.ConversationType.valueOf(type);
        RichContentMessage richContentMessage = RichContentMessage.obtain(title, content, imageUrl);
        if (!TextUtils.isEmpty(extra))
            richContentMessage.setExtra(extra);

        mRongClient.sendMessage(conversationType, targetId, richContentMessage, null, null, new RongIMClient.SendMessageCallback() {
            @Override
            public void onError(Integer id, RongIMClient.ErrorCode errorCode) {
                callModuleError(context, new ProgressModel(id), errorCode.getValue());
            }

            @Override
            public void onSuccess(Integer id) {
                callModuleSuccess(context, new ProgressModel(id));
            }
        }, new RongIMClient.ResultCallback<Message>() {
            @Override
            public void onSuccess(Message message) {
                TranslatedMessage translatedMessage = new TranslatedMessage(message);
                callModulePrepare(context, new ProgressModel(translatedMessage));
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                callModuleError(context, new RongException(errorCode.getValue()));
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

        final Conversation.ConversationType conversationType = Conversation.ConversationType.valueOf(type);
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
                mRongClient.sendMessage(conversationType, targetId, locationMessage, null, null, new RongIMClient.SendMessageCallback() {
                    @Override
                    public void onError(Integer id, RongIMClient.ErrorCode errorCode) {
                        callModuleError(context, new ProgressModel(id), errorCode.getValue());
                    }

                    @Override
                    public void onSuccess(Integer id) {
                        callModuleSuccess(context, new ProgressModel(id));
                    }
                }, new RongIMClient.ResultCallback<Message>() {
                    @Override
                    public void onSuccess(Message message) {
                        TranslatedMessage translatedMessage = new TranslatedMessage(message);
                        callModulePrepare(context, new ProgressModel(translatedMessage));
                    }

                    @Override
                    public void onError(RongIMClient.ErrorCode errorCode) {
                        callModuleError(context, new RongException(errorCode.getValue()));
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

        Conversation.ConversationType conversationType = Conversation.ConversationType.valueOf(type);
        mRongClient.sendMessage(conversationType, targetId, CommandNotificationMessage.obtain(name, data), null, null, new RongIMClient.SendMessageCallback() {
            @Override
            public void onError(Integer id, RongIMClient.ErrorCode errorCode) {
                callModuleError(context, new ProgressModel(id), errorCode.getValue());
            }

            @Override
            public void onSuccess(Integer id) {
                callModuleSuccess(context, new ProgressModel(id));
            }
        }, new RongIMClient.ResultCallback<Message>() {
            @Override
            public void onSuccess(Message message) {
                TranslatedMessage translatedMessage = new TranslatedMessage(message);
                callModulePrepare(context, new ProgressModel(translatedMessage));
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                callModuleError(context, new RongException(errorCode.getValue()));
            }
        });
    }

    @UzJavascriptMethod
    public void jsmethod_sendCommandMessage(final UZModuleContext context) {
        String type = context.optString("conversationType", null);
        String targetId = context.optString("targetId", null);
        String name = context.optString("name", null);
        String data = context.optString("data", null);

        if (TextUtils.isEmpty(type) || TextUtils.isEmpty(targetId) || TextUtils.isEmpty(name) || TextUtils.isEmpty(data)) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }

        Conversation.ConversationType conversationType = Conversation.ConversationType.valueOf(type);
        mRongClient.sendMessage(conversationType, targetId, CommandMessage.obtain(name, data), null, null, new RongIMClient.SendMessageCallback() {
            @Override
            public void onError(Integer id, RongIMClient.ErrorCode errorCode) {
                callModuleError(context, new ProgressModel(id), errorCode.getValue());
            }

            @Override
            public void onSuccess(Integer id) {
                callModuleSuccess(context, new ProgressModel(id));
            }
        }, new RongIMClient.ResultCallback<Message>() {
            @Override
            public void onSuccess(Message message) {
                TranslatedMessage translatedMessage = new TranslatedMessage(message);
                callModulePrepare(context, new ProgressModel(translatedMessage));
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                callModuleError(context, new RongException(errorCode.getValue()));
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

        Conversation.ConversationType conversationType = Conversation.ConversationType.valueOf(type);
        mRongClient.getConversationNotificationStatus(conversationType, targetId, new RongIMClient.ResultCallback<Conversation.ConversationNotificationStatus>() {
            @Override
            public void onSuccess(Conversation.ConversationNotificationStatus conversationNotificationStatus) {
                TranslatedConversationNtfyStatus state = new TranslatedConversationNtfyStatus(conversationNotificationStatus);
                callModuleSuccess(context, state);
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                callModuleError(context, new RongException(errorCode.getValue()));
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

        Conversation.ConversationType conversationType = Conversation.ConversationType.valueOf(type);
        Conversation.ConversationNotificationStatus conversationNotificationStatus = Conversation.ConversationNotificationStatus.valueOf(status);

        mRongClient.setConversationNotificationStatus(conversationType, targetId, conversationNotificationStatus,
        new RongIMClient.ResultCallback<Conversation.ConversationNotificationStatus>() {
            @Override
            public void onSuccess(Conversation.ConversationNotificationStatus conversationNotificationStatus) {
                TranslatedConversationNtfyStatus state = new TranslatedConversationNtfyStatus(conversationNotificationStatus);
                callModuleSuccess(context, state);
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                callModuleSuccess(context, errorCode.getValue());
            }
        });
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
        mRongClient.setDiscussionInviteStatus(targetId, discussionInviteStatus, new RongIMClient.OperationCallback() {
            @Override
            public void onSuccess() {
                callModuleSuccess(context, null);
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                callModuleError(context, new RongException(errorCode.getValue()));

            }
        });
    }

    @UzJavascriptMethod
    public void jsmethod_syncGroup(final UZModuleContext context) {
        JSONArray array = context.optJSONArray("groups");
        if (array == null || array.length() == 0) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }
        if (mRongClient == null) {
            callModuleError(context, new RongException(ErrorCode.NOT_CONNECTED));
            return;
        }
        List<Group> groups = new ArrayList<Group>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.optJSONObject(i);
            if (TextUtils.isEmpty(object.optString("groupId")) || TextUtils.isEmpty(object.optString("groupName"))) {
                callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
                return;
            }
            Group group = new Group(object.optString("groupId"),
                                    object.optString("groupName"),
                                    TextUtils.isEmpty(object.optString("portraitUrl")) ? null : Uri.parse(object.optString("portraitUrl")));
            groups.add(group);
        }
        mRongClient.syncGroup(groups, new RongIMClient.OperationCallback() {
            @Override
            public void onSuccess() {
                callModuleSuccess(context, null);
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                callModuleError(context, new RongException(errorCode.getValue()));
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
                callModuleError(context, new RongException(errorCode.getValue()));
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
                callModuleError(context, new RongException(errorCode.getValue()));
            }
        });
    }

    @UzJavascriptMethod
    public void jsmethod_setConnectionStatusListener(final UZModuleContext context) {
        RongIMClient.setConnectionStatusListener(new RongIMClient.ConnectionStatusListener() {
            @Override
            public void onChanged(ConnectionStatus connectionStatus) {
                callModule(context, new ConnectionStatusResult(connectionStatus.getValue()), false);
            }
        });
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
                callModuleError(context, new RongException(errorCode.getValue()));
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
                callModuleError(context, new RongException(errorCode.getValue()));
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
        Conversation.ConversationType[] conversationTypes = new Conversation.ConversationType[jsonArray.length()];
        while (i < jsonArray.length()) {
            String item = jsonArray.optString(i);
            Conversation.ConversationType conversationType = Conversation.ConversationType.valueOf(item);
            conversationTypes[i] = conversationType;
            i++;
        }

        mRongClient.clearConversations(new RongIMClient.ResultCallback() {
            @Override
            public void onSuccess(Object o) {
                callModuleSuccess(context, o);
            }

            @Override
            public void onError(RongIMClient.ErrorCode e) {
                callModuleError(context, new RongException(e.getValue()));
            }
        }, conversationTypes);
    }

    @UzJavascriptMethod
    public void jsmethod_getConnectionStatus(final UZModuleContext context) {
        RongIMClient.ConnectionStatusListener.ConnectionStatus status;
        if (mRongClient == null) {
            callModuleError(context, new RongException(ErrorCode.NOT_CONNECTED));
            return;
        }
        status = mRongClient.getCurrentConnectionStatus();
        int code = -1;
        if (status != null)
            code = status.getValue();
        callModuleSuccess(context, new ConnectionStatusResult(code));
    }

    @UzJavascriptMethod
    public void jsmethod_getRemoteHistoryMessages(final UZModuleContext context) {
        final String type = context.optString("conversationType", null);
        final String targetId = context.optString("targetId", null);
        final long dateTime = context.optLong("dateTime", 0);
        final int count = context.optInt("count", 0);

        if (TextUtils.isEmpty(type) || TextUtils.isEmpty(targetId)) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }
        Conversation.ConversationType conversationType = Conversation.ConversationType.valueOf(type);
        mRongClient.getRemoteHistoryMessages(conversationType, targetId, dateTime, count, new RongIMClient.ResultCallback<List<Message>>() {
            @Override
            public void onSuccess(List<Message> messages) {
                if (messages == null || messages.size() == 0) {
                    callModuleSuccess(context, "");
                    return;
                }

                ArrayList<TranslatedMessage> list = new ArrayList<TranslatedMessage>();
                for (Message message : messages) {
                    TranslatedMessage tm = new TranslatedMessage(message);
                    list.add(tm);
                }
                callModuleSuccess(context, list);
            }

            @Override
            public void onError(RongIMClient.ErrorCode e) {
                callModuleError(context, new RongException(e.getValue()));
            }
        });
    }

    @UzJavascriptMethod
    public void jsmethod_setMessageSentStatus(final UZModuleContext context) {
        final String state = context.optString("sentStatus", null);
        final int id = context.optInt("messageId", 0);
        if (state == null) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }

        Message.SentStatus status = Message.SentStatus.valueOf(state);
        if (id <= 0 || status == null) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }

        mRongClient.setMessageSentStatus(id, status, new RongIMClient.ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                callModuleSuccess(context, aBoolean);
            }

            @Override
            public void onError(RongIMClient.ErrorCode e) {
                callModuleError(context, new RongException(e.getValue()));
            }
        });
    }

    @UzJavascriptMethod
    public void jsmethod_getCurrentUserId(final UZModuleContext context) {
        String id = mRongClient.getCurrentUserId();
        callModuleSuccess(context, id);
    }

    @UzJavascriptMethod
    public void jsmethod_getDeltaTime(final UZModuleContext context) {
        long time = mRongClient.getDeltaTime();
        callModuleSuccess(context, time);
    }

    @UzJavascriptMethod
    public void jsmethod_addToBlacklist(final UZModuleContext context) {
        String id = context.optString("userId", null);
        if (TextUtils.isEmpty(id)) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }

        mRongClient.addToBlacklist(id, new RongIMClient.OperationCallback() {
            @Override
            public void onSuccess() {
                callModuleSuccess(context, null);
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                callModuleError(context, new RongException(errorCode.getValue()));
            }
        });
    }

    @UzJavascriptMethod
    public void jsmethod_removeFromBlacklist(final UZModuleContext context) {
        String id = context.optString("userId", null);
        if (TextUtils.isEmpty(id)) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }
        mRongClient.removeFromBlacklist(id, new RongIMClient.OperationCallback() {
            @Override
            public void onSuccess() {
                callModuleSuccess(context, null);
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                callModuleError(context, new RongException(errorCode.getValue()));
            }
        });
    }

    @UzJavascriptMethod
    public void jsmethod_getBlacklistStatus(final UZModuleContext context) {
        String id = context.optString("userId", null);
        if (TextUtils.isEmpty(id)) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }

        mRongClient.getBlacklistStatus(id, new RongIMClient.ResultCallback<RongIMClient.BlacklistStatus>() {
            @Override
            public void onSuccess(RongIMClient.BlacklistStatus blacklistStatus) {
                if (blacklistStatus == null)
                    callModuleSuccess(context, 1);
                else
                    callModuleSuccess(context, blacklistStatus.getValue());
            }

            @Override
            public void onError(RongIMClient.ErrorCode e) {
                callModuleError(context, new RongException(e.getValue()));
            }
        });
    }

    @UzJavascriptMethod
    public void jsmethod_getBlacklist(final UZModuleContext context) {

        mRongClient.getBlacklist(new RongIMClient.GetBlacklistCallback() {
            @Override
            public void onSuccess(String[] strings) {
                if (strings == null || strings.length == 0) {
                    callModuleSuccess(context, new String[0]);
                    return;
                }
                callModuleSuccess(context, strings);
            }

            @Override
            public void onError(RongIMClient.ErrorCode e) {
                callModuleError(context, new RongException(e.getValue()));
            }
        });
    }

    @UzJavascriptMethod
    public void jsmethod_setNotificationQuietHours(final UZModuleContext context) {
        final String startTime = context.optString("startTime", null);
        final int spanMinutes = context.optInt("spanMinutes", 0);
        if (TextUtils.isEmpty(startTime)) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }

        mRongClient.setNotificationQuietHours(startTime, spanMinutes, new RongIMClient.OperationCallback() {
            @Override
            public void onSuccess() {
                callModuleSuccess(context, null);
                saveNotificationQuietHours(mContext, startTime, spanMinutes);
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                callModuleError(context, new RongException(errorCode.getValue()));
            }
        });
    }

    @UzJavascriptMethod
    public void jsmethod_removeNotificationQuietHours(final UZModuleContext context) {
        mRongClient.removeNotificationQuietHours(new RongIMClient.OperationCallback() {
            @Override
            public void onSuccess() {
                callModuleSuccess(context, null);
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                callModuleError(context, new RongException(errorCode.getValue()));
            }
        });
    }

    @UzJavascriptMethod
    public void jsmethod_getNotificationQuietHours(final UZModuleContext context) {
        mRongClient.getNotificationQuietHours(new RongIMClient.GetNotificationQuietHoursCallback() {
            @Override
            public void onSuccess(String startTime, int spanMinutes) {
                TranslatedQuietHour quiet = new TranslatedQuietHour(startTime, spanMinutes);
                callModuleSuccess(context, quiet);
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                callModuleError(context, new RongException(errorCode.getValue()));
            }
        });
    }

    private final <T> void callModuleSuccess(UZModuleContext context, T model) {
        final RongResult<T> result = new RongResult<T>();
        result.setStatus(RongResult.Status.success);
        result.setResult(model);
        context.success(getJsonStringResult(result), true, true);
    }

    private final <T> void callModule(UZModuleContext context, T model, boolean rmJsFunc) {
        final RongResult<T> result = new RongResult<T>();
        result.setResult(model);
        context.success(getJsonStringResult(result), true, rmJsFunc);
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

    private final <T> void callCustomServiceSuccess(UZModuleContext context) {
        final RongCustomServiceResult<T> result = new RongCustomServiceResult<T>();
        result.setStatus(RongCustomServiceResult.Status.success);
        context.success(getJsonStringResult(result), true, false);
    }

    private final <T> void callCustomServiceError(UZModuleContext context, T modle) {
        final RongCustomServiceResult result = new RongCustomServiceResult();
        result.setStatus(RongCustomServiceResult.Status.error);
        result.setResult(modle);
        context.error(getJsonObjectResult(result), null, false);
    }

    private final <T> void callCustomServiceModeChanged(UZModuleContext context, T model) {
        final RongCustomServiceResult<T> result = new RongCustomServiceResult<T>();
        result.setStatus(RongCustomServiceResult.Status.modeChanged);
        result.setResult(model);
        context.success(getJsonStringResult(result), true, false);
    }

    private final <T> void callCustomServiceQuit(UZModuleContext context, T model) {
        final RongCustomServiceResult<T> result = new RongCustomServiceResult<T>();
        result.setStatus(RongCustomServiceResult.Status.quit);
        result.setResult(model);
        context.success(getJsonStringResult(result), true, true);
    }

    private final <T> void callCustomServicePullEvaluation(UZModuleContext context, T model) {
        final RongCustomServiceResult<T> result = new RongCustomServiceResult<T>();
        result.setStatus(RongCustomServiceResult.Status.pullEvaluation);
        result.setResult(model);
        context.success(getJsonStringResult(result), true, false);
    }

    private final <T> void callCustomServiceSelectGroup(UZModuleContext context, T model) {
        final RongCustomServiceResult<T> result = new RongCustomServiceResult<T>();
        result.setStatus(RongCustomServiceResult.Status.selectGroup);
        result.setResult(model);
        context.success(getJsonStringResult(result), true, false);
    }

    private final <T> JSONObject getJsonObjectResult(T result) {
        String json = mGson.toJson(result);
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
        return json;
    }

    private enum AdaptConnectionStatus {
        NETWORK_UNAVAILABLE(-1, "NETWORK_UNAVAILABLE"),
        CONNECTED(0, "CONNECTED"),
        CONNECTING(1, "CONNECTING"),
        DISCONNECTED(2, "DISCONNECTED"),
        KICKED(3, "KICKED"),
        TOKEN_INCORRECT(4, "TOKEN_INCORRECT"),
        SERVER_INVALID(5, "SERVER_INVALID");

        Integer code;
        String msg;
        AdaptConnectionStatus(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        static AdaptConnectionStatus setValue(int code) {
            for (AdaptConnectionStatus c : AdaptConnectionStatus.values()) {
                if (code == c.code) {
                    return c;
                }
            }
            return NETWORK_UNAVAILABLE;
        }
    }

    /**
     * 本地化通知免打扰时间。
     *
     * @param startTime   默认  “-1”
     * @param spanMinutes 默认 -1
     */
    public static void saveNotificationQuietHours(Context mContext, String startTime, int spanMinutes) {

        SharedPreferences mPreferences = null;

        if (mContext != null)
            mPreferences = mContext.getSharedPreferences("RONG_SDK", Context.MODE_PRIVATE);

        if (mPreferences != null) {
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putString("QUIET_HOURS_START_TIME", startTime);
            editor.putInt("QUIET_HOURS_SPAN_MINUTES", spanMinutes);
            editor.commit();
        }
    }

    /**
     * 获取通知免打扰开始时间
     *
     * @return
     */
    public static String getNotificationQuietHoursForStartTime(Context mContext) {
        SharedPreferences mPreferences = null;

        if (mPreferences == null && mContext != null)
            mPreferences = mContext.getSharedPreferences("RONG_SDK", Context.MODE_PRIVATE);

        if (mPreferences != null) {
            return mPreferences.getString("QUIET_HOURS_START_TIME", "");
        }

        return "";
    }

    /**
     * 获取通知免打扰时间间隔
     *
     * @return
     */
    public static int getNotificationQuietHoursForSpanMinutes(Context mContext) {
        SharedPreferences mPreferences = null;

        if (mPreferences == null && mContext != null)
            mPreferences = mContext.getSharedPreferences("RONG_SDK", Context.MODE_PRIVATE);

        if (mPreferences != null) {
            return mPreferences.getInt("QUIET_HOURS_SPAN_MINUTES", 0);
        }

        return 0;
    }

    private boolean isInQuietTime(Context context) {

        String startTimeStr = getNotificationQuietHoursForStartTime(context);

        int hour = -1;
        int minute = -1;
        int second = -1;

        if (!TextUtils.isEmpty(startTimeStr) && startTimeStr.indexOf(":") != -1) {
            String[] time = startTimeStr.split(":");

            try {
                if (time.length >= 3) {
                    hour = Integer.parseInt(time[0]);
                    minute = Integer.parseInt(time[1]);
                    second = Integer.parseInt(time[2]);
                }
            } catch (NumberFormatException e) {
            }
        }

        if (hour == -1 || minute == -1 || second == -1) {
            return false;
        }

        Calendar startCalendar = Calendar.getInstance();
        startCalendar.set(Calendar.HOUR_OF_DAY, hour);
        startCalendar.set(Calendar.MINUTE, minute);
        startCalendar.set(Calendar.SECOND, second);


        long spanTime = getNotificationQuietHoursForSpanMinutes(context) * 60;
        long startTime = startCalendar.getTimeInMillis() / 1000;

        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTimeInMillis(startTime * 1000 + spanTime * 1000);

        Calendar currentCalendar = Calendar.getInstance();

        if (currentCalendar.after(startCalendar) && currentCalendar.before(endCalendar)) {
            return true;
        } else {
            return false;
        }
    }

    class CustomServiceListener implements ICustomServiceListener {

        UZModuleContext context;

        CustomServiceListener(UZModuleContext context) {
            this.context = context;
        }

        @Override
        public void onSuccess(CustomServiceConfig config) {
            //TranslatedCustomServiceConfig cfg = new TranslatedCustomServiceConfig(config);
            callCustomServiceSuccess(context);
        }

        @Override
        public void onError(int code, String msg) {
            TranslatedCustomServiceErrorMsg customServiceErrorMsg = new TranslatedCustomServiceErrorMsg(code, msg);
            callCustomServiceError(context, customServiceErrorMsg);
        }

        @Override
        public void onModeChanged(CustomServiceMode mode) {
            int csMode = mode.getValue();
            TranslatedCustomServiceMode customerServiceMode = new TranslatedCustomServiceMode(csMode);
            callCustomServiceModeChanged(context, customerServiceMode);
        }

        @Override
        public void onQuit(String msg) {
            TranslatedCustomServiceQuitMsg customServiceQuitMsg = new TranslatedCustomServiceQuitMsg(msg);
            callCustomServiceQuit(context, customServiceQuitMsg);
        }

        @Override
        public void onPullEvaluation(String dialogId) {
            TranslatedCustomServiceDialogID customServiceDialogID = new TranslatedCustomServiceDialogID(dialogId);
            callCustomServicePullEvaluation(context, customServiceDialogID);
        }

        @Override
        public void onSelectGroup(List<CSGroupItem> groups) {
            TranslatedCSGroupList csGroupList = new TranslatedCSGroupList(groups);
            callCustomServiceSelectGroup(context, csGroupList);
        }
    }

    @UzJavascriptMethod
    public void jsmethod_startCustomService(UZModuleContext context) {

        String kefuId = context.optString("kefuId", null);
        if (TextUtils.isEmpty(kefuId)) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }

        if (mRongClient == null) {
            callModuleError(context, new RongException(ErrorCode.NOT_CONNECTED));
            return;
        }

        String nickName = context.optString("nickName", null);

        CSCustomServiceInfo info = new CSCustomServiceInfo.Builder()
        .nickName(nickName).build();

        CustomServiceListener csListener = null;
        if (customServiceCache != null)
            csListener = customServiceCache.get(kefuId);
        if (csListener == null)
            csListener = new CustomServiceListener(context);

        mRongClient.startCustomService(kefuId, csListener, info);
    }

    @UzJavascriptMethod
    public void jsmethod_switchToHumanMode(UZModuleContext context) {

        String kefuId = context.optString("kefuId", null);
        if (TextUtils.isEmpty(kefuId)) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }

        if (mRongClient == null) {
            callModuleError(context, new RongException(ErrorCode.NOT_CONNECTED));
            return;
        }

        mRongClient.switchToHumanMode(kefuId);
    }

    @UzJavascriptMethod
    public void jsmethod_selectCustomServiceGroup(UZModuleContext context) {

        String kefuId = context.optString("kefuId", null);
        if (TextUtils.isEmpty(kefuId)) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }

        if (mRongClient == null) {
            callModuleError(context, new RongException(ErrorCode.NOT_CONNECTED));
            return;
        }

        String groupId = context.optString("groupId", null);
        mRongClient.selectCustomServiceGroup(kefuId, groupId);
    }

    @UzJavascriptMethod
    public void jsmethod_evaluateRobotCustomerService(UZModuleContext context) {

        String kefuId = context.optString("kefuId", null);
        if (TextUtils.isEmpty(kefuId)) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }

        if (mRongClient == null) {
            callModuleError(context, new RongException(ErrorCode.NOT_CONNECTED));
            return;
        }
        Boolean isRobotResolved = context.optBoolean("isRobotResolved");
        String knowledgeId = context.optString("knowledgeId", null);
        mRongClient.evaluateCustomService(kefuId, isRobotResolved, knowledgeId);
    }

    @UzJavascriptMethod
    public void jsmethod_evaluateHumanCustomerService(UZModuleContext context) {
        String kefuId = context.optString("kefuId", null);
        if (TextUtils.isEmpty(kefuId)) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }

        if (mRongClient == null) {
            callModuleError(context, new RongException(ErrorCode.NOT_CONNECTED));
            return;
        }
        int source = context.optInt("source", 5);
        String suggest = context.optString("suggest", null);
        String dialogId = context.optString("dialogId", null);
        mRongClient.evaluateCustomService(kefuId, source, suggest, dialogId);
    }

    @UzJavascriptMethod
    public void jsmethod_stopCustomService(UZModuleContext context) {

        String kefuId = context.optString("kefuId", null);
        if (TextUtils.isEmpty(kefuId)) {
            callModuleError(context, new RongException(ErrorCode.ARGUMENT_EXCEPTION));
            return;
        }

        if (mRongClient == null) {
            callModuleError(context, new RongException(ErrorCode.NOT_CONNECTED));
            return;
        }

        mRongClient.stopCustomService(kefuId);
        CustomServiceListener customServiceListener = customServiceCache.get(kefuId);
        if (customServiceListener != null)
            customServiceCache.remove(kefuId);
        customServiceListener = null;
    }


}
