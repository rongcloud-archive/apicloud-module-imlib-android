package io.rong.apicloud;

import java.util.List;

import io.rong.imlib.RongIMClient;

/**
 * Created by DragonJ on 15/1/20.
 */
public class Discussion {
    private String id;
    private String name;
    private String creatorId;
    private String inviteStatus;
    private List<String> memberIdList;

    public Discussion(String id, String name, String creatorId, String inviteStatus, List<String> memberIdList) {
        this.id = id;
        this.name = name;
        this.creatorId = creatorId;
        this.inviteStatus = inviteStatus;
        this.memberIdList = memberIdList;
    }

    public Discussion(io.rong.imlib.model.Discussion discussion){
        setId(discussion.getId());
        setName(discussion.getName());
        setCreatorId(discussion.getCreatorId());
        setInviteStatus(discussion.isOpen()?"OPENED":"CLOSED");
        setMemberIdList(discussion.getMemberIdList());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public String getInviteStatus() {
        return inviteStatus;
    }

    public void setInviteStatus(String inviteStatus) {
        this.inviteStatus = inviteStatus;
    }

    public List<String> getMemberIdList() {
        return memberIdList;
    }

    public void setMemberIdList(List<String> memberIdList) {
        this.memberIdList = memberIdList;
    }
}
