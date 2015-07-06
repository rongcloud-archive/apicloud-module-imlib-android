package io.rong.apicloud;

import android.os.Parcel;

import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.MessageContent;


/**
 * Created by DragonJ on 14/12/21.
 */
public class Message {

    private Conversation.ConversationType conversationType;
    private java.lang.String targetId;
    private int messageId;
    private io.rong.imlib.model.Message.MessageDirection messageDirection;
    private java.lang.String senderUserId;
    private Integer receivedStatus;
    private io.rong.imlib.model.Message.SentStatus sentStatus;
    private Long receivedTime;
    private Long sentTime;
    private java.lang.String objectName;
    private MessageContent content;
    private java.lang.String extra;

    public Message(io.rong.imlib.model.Message message) {
        this.setMessageId(message.getMessageId());
        this.setConversationType(message.getConversationType());
        this.setTargetId(message.getTargetId());
        this.setMessageDirection(message.getMessageDirection());
        this.setSenderUserId(message.getSenderUserId());
        this.receivedStatus = message.getReceivedStatus() == null ? null : message.getReceivedStatus().getFlag();
        this.setSentStatus(message.getSentStatus());
        this.setReceivedTime(message.getReceivedTime());
        this.setSentTime(message.getSentTime());
        this.setObjectName(message.getObjectName());
        this.setContent(message.getContent());
        this.setExtra(message.getExtra());

        if (message.getContent() instanceof io.rong.message.ImageMessage) {
            setContent(new ImageMessage((io.rong.message.ImageMessage) message.getContent()));
        } else if (message.getContent() instanceof io.rong.message.VoiceMessage) {
            setContent(new VoiceMessage((io.rong.message.VoiceMessage) message.getContent()));
        } else if (message.getContent() instanceof io.rong.message.LocationMessage) {
            setContent(new LocationMessage((io.rong.message.LocationMessage) message.getContent()));
        } else if (message.getContent() instanceof io.rong.message.TextMessage){
            setContent(new TextMessage((io.rong.message.TextMessage)message.getContent()));
        } else if (message.getContent() instanceof io.rong.message.RichContentMessage){
            setContent(new RichContentMessage((io.rong.message.RichContentMessage)message.getContent()));
        }
    }

    public Message(int id){
        setMessageId(id);
    }

    public Conversation.ConversationType getConversationType() {
        return conversationType;
    }

    public void setConversationType(Conversation.ConversationType conversationType) {
        this.conversationType = conversationType;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public io.rong.imlib.model.Message.MessageDirection getMessageDirection() {
        return messageDirection;
    }

    public void setMessageDirection(io.rong.imlib.model.Message.MessageDirection messageDirection) {
        this.messageDirection = messageDirection;
    }

    public String getSenderUserId() {
        return senderUserId;
    }

    public void setSenderUserId(String senderUserId) {
        this.senderUserId = senderUserId;
    }

    public int getReceivedStatus() {
        return receivedStatus;
    }

    public void setReceivedStatus(int receivedStatus) {
        this.receivedStatus = receivedStatus;
    }

    public io.rong.imlib.model.Message.SentStatus getSentStatus() {
        return sentStatus;
    }

    public void setSentStatus(io.rong.imlib.model.Message.SentStatus sentStatus) {
        this.sentStatus = sentStatus;
    }

    public Long getReceivedTime() {
        return receivedTime;
    }

    public void setReceivedTime(Long receivedTime) {
        this.receivedTime = receivedTime;
    }

    public Long getSentTime() {
        return sentTime;
    }

    public void setSentTime(Long sentTime) {
        this.sentTime = sentTime;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public MessageContent getContent() {
        return content;
    }

    public void setContent(MessageContent content) {
        this.content = content;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public static class TextMessage extends MessageContent {

        String text;
        String extra;
        @Override
        public byte[] encode() {
            return new byte[0];
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {

        }

        public TextMessage(io.rong.message.TextMessage message){
            text = message.getContent();
            extra = message.getExtra();
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getExtra() {
            return extra;
        }

        public void setExtra(String extra) {
            this.extra = extra;
        }
    }

    public static class RichContentMessage extends MessageContent{
        private java.lang.String title;
        private java.lang.String description;
        private java.lang.String imageUrl;
        private java.lang.String extra;


        public RichContentMessage(io.rong.message.RichContentMessage message){
            setTitle(message.getTitle());
            setDescription(message.getContent());
            setImageUrl(message.getUrl());
            setExtra(message.getExtra());
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getExtra() {
            return extra;
        }

        public void setExtra(String extra) {
            this.extra = extra;
        }

        @Override
        public byte[] encode() {
            return new byte[0];
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {

        }
    }

    public static class ImageMessage extends MessageContent {
        String thumbPath;
        String localPath;
        String imageUrl;
        String extra;

        public ImageMessage(io.rong.message.ImageMessage message) {
            setThumbPath(message.getThumUri() == null ? null : message.getThumUri().getPath());
            setLocalPath(message.getLocalUri() == null ? null : message.getLocalUri().getPath());
            setImageUrl(message.getRemoteUri() == null ? null : message.getRemoteUri().toString());
            setExtra(message.getExtra() == null ? null : message.getExtra().toString());
        }

        @Override
        public byte[] encode() {
            return new byte[0];
        }

        public String getThumbPath() {
            return thumbPath;
        }

        public void setThumbPath(String thumbPath) {
            this.thumbPath = thumbPath;
        }

        public String getLocalPath() {
            return localPath;
        }

        public void setLocalPath(String localPath) {
            this.localPath = localPath;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getExtra() {
            return extra;
        }

        public void setExtra(String extra) {
            this.extra = extra;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {

        }
    }

    public static class LocationMessage extends MessageContent {
        double latitude;
        double longitude;
        String poi;
        String imagePath;
        String extra;

        public LocationMessage(io.rong.message.LocationMessage message) {
            setLatitude(message.getLat());
            setLongitude(message.getLng());
            setPoi(message.getPoi());
            setImagePath(message.getImgUri() == null ? null : message.getImgUri().getPath());
            setExtra(message.getExtra());
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        public String getImagePath() {
            return imagePath;
        }

        public void setImagePath(String imagePath) {
            this.imagePath = imagePath;
        }

        public String getPoi() {
            return poi;
        }

        public void setPoi(String poi) {
            this.poi = poi;
        }

        public String getExtra() {
            return extra;
        }

        public void setExtra(String extra) {
            this.extra = extra;
        }

        @Override
        public byte[] encode() {
            return new byte[0];
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {

        }
    }

    public static class VoiceMessage extends MessageContent {
        String voicePath;
        int duration;
        String extra;

        public VoiceMessage(io.rong.message.VoiceMessage message) {
            setVoicePath(message.getUri() == null ? null : message.getUri().getPath());
            setDuration(message.getDuration());
            setExtra(message.getExtra());
        }

        @Override
        public byte[] encode() {
            return new byte[0];
        }

        public String getVoicePath() {
            return voicePath;
        }

        public void setVoicePath(String voicePath) {
            this.voicePath = voicePath;
        }

        public int getDuration() {
            return duration;
        }

        public void setDuration(int duration) {
            this.duration = duration;
        }

        public String getExtra() {
            return extra;
        }

        public void setExtra(String extra) {
            this.extra = extra;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {

        }
    }
}
