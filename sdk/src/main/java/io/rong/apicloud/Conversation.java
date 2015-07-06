package io.rong.apicloud;

import io.rong.imlib.model.MessageContent;

/**
 * Created by DragonJ on 14/12/21.
 */
public class Conversation {
    private int receivedStatus;
    private io.rong.imlib.model.Conversation.ConversationType conversationType;
    private java.lang.String targetId;
    private java.lang.String conversationTitle;
    private Integer unreadMessageCount;
    private Boolean isTop;
    private io.rong.imlib.model.Message.SentStatus sentStatus;
    private Long receivedTime;
    private Long sentTime;
    private java.lang.String objectName;
    private java.lang.String senderUserId;
    private Integer latestMessageId;
    private MessageContent latestMessage;
    private String draft;
    private io.rong.imlib.model.Conversation.ConversationNotificationStatus notificationStatus;

    public Conversation(io.rong.imlib.model.Conversation conversation) {
        this.setConversationType(conversation.getConversationType());
        this.setTargetId(conversation.getTargetId());
        this.setConversationTitle(conversation.getConversationTitle());
        this.setUnreadMessageCount(conversation.getUnreadMessageCount());
        this.setTop(conversation.isTop());
        this.receivedStatus = conversation.getReceivedStatus() == null ? null : conversation.getReceivedStatus().getFlag();
        this.setSentStatus(conversation.getSentStatus());
        this.setReceivedTime(conversation.getReceivedTime());
        this.setSentTime(conversation.getSentTime());
        this.setObjectName(conversation.getObjectName());
        this.setSenderUserId(conversation.getSenderUserId());
        this.setLatestMessageId(conversation.getLatestMessageId());
        this.setLatestMessage(conversation.getLatestMessage());
        this.setNotificationStatus(conversation.getNotificationStatus());
        this.setDraft(conversation.getDraft());
    }

    public String getDraft() {
        return draft;
    }

    public void setDraft(String draft) {
        this.draft = draft;
    }

    public io.rong.imlib.model.Conversation.ConversationType getConversationType() {
        return conversationType;
    }

    public void setConversationType(io.rong.imlib.model.Conversation.ConversationType conversationType) {
        this.conversationType = conversationType;
    }

    public java.lang.String getTargetId() {
        return this.targetId;
    }

    public void setTargetId(java.lang.String targetId) {
        this.targetId = targetId;
    }

    public java.lang.String getConversationTitle() {
        return conversationTitle;
    }

    public void setConversationTitle(java.lang.String conversationTitle) {
        this.conversationTitle = conversationTitle;
    }

    public int getUnreadMessageCount() {
        return unreadMessageCount;
    }

    public void setUnreadMessageCount(int unreadMessageCount) {
        this.unreadMessageCount = unreadMessageCount;
    }

    public boolean isTop() {
        return isTop;
    }

    public void setTop(boolean isTop) {
        this.isTop = isTop;
    }

    public int getReceivedStatus() {
        return receivedStatus;
    }

    public void setReceivedStatus(io.rong.imlib.model.Message.ReceivedStatus receivedStatus) {
        this.receivedStatus = receivedStatus.getFlag();
    }

    public io.rong.imlib.model.Message.SentStatus getSentStatus() {
        return sentStatus;
    }

    public void setSentStatus(io.rong.imlib.model.Message.SentStatus sentStatus) {
        this.sentStatus = sentStatus;
    }

    public long getReceivedTime() {
        return receivedTime;
    }

    public void setReceivedTime(long receivedTime) {
        this.receivedTime = receivedTime;
    }

    public long getSentTime() {
        return sentTime;
    }

    public void setSentTime(long sentTime) {
        this.sentTime = sentTime;
    }

    public java.lang.String getObjectName() {
        return objectName;
    }

    public void setObjectName(java.lang.String objectName) {
        this.objectName = objectName;
    }

    public int getLatestMessageId() {
        return latestMessageId;
    }

    public void setLatestMessageId(int latestMessageId) {
        this.latestMessageId = latestMessageId;
    }

    public MessageContent getLatestMessage() {
        return latestMessage;
    }

    public void setLatestMessage(MessageContent latestMessage) {

        if (latestMessage instanceof io.rong.message.ImageMessage) {
            this.latestMessage = new Message.ImageMessage((io.rong.message.ImageMessage) latestMessage);
        } else if (latestMessage instanceof io.rong.message.VoiceMessage) {
            this.latestMessage = new Message.VoiceMessage((io.rong.message.VoiceMessage) latestMessage);
        } else if (latestMessage instanceof io.rong.message.LocationMessage) {
            this.latestMessage = new Message.LocationMessage((io.rong.message.LocationMessage) latestMessage);
        } else if (latestMessage instanceof io.rong.message.TextMessage){
            this.latestMessage = new Message.TextMessage((io.rong.message.TextMessage)latestMessage);
        } else if (latestMessage instanceof io.rong.message.RichContentMessage){
            this.latestMessage = new Message.RichContentMessage((io.rong.message.RichContentMessage)latestMessage);
        }
    }

    public java.lang.String getSenderUserId() {
        return senderUserId;
    }

    public void setSenderUserId(java.lang.String senderUserId) {
        this.senderUserId = senderUserId;
    }

    public io.rong.imlib.model.Conversation.ConversationNotificationStatus getNotificationStatus() {
        return notificationStatus;
    }

    public void setNotificationStatus(io.rong.imlib.model.Conversation.ConversationNotificationStatus notificationStatus) {
        this.notificationStatus = notificationStatus;
    }

}
