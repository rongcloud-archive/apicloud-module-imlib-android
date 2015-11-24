
        var rong = null;
        apiready=function(){
            if(rong == null)
                rong = api.require("rongCloud2");
        }

        function connect() {
            if(rong == null)
                rong = api.require('rongCloud2');

            var param = {
                token: "ThptTWyiPPPvZHvuSiuri82yq+hfEluLjZ78E1qo4hEVSFQNpqdoPu406urMWKN4Z3/olWR+v9JVLAwfOQoLrA=="
            };
            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }
            rong.connect(param, resultCallback);
        }

        function reconnect() {
            if(rong == null)
                rong = api.require('rongCloud2');
            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }
            rong.reconnect(null, resultCallback);
        }

        function addToBlacklist() {
            if(rong == null)
                        rong = api.require('rongCloud2');
                    var resultCallback = function(ret, err) {
                        alert(JSON.stringify(ret));
                    }
            var param = {userId: "54"};

            rong.addToBlacklist(param, resultCallback);
        }

        function removeFromBlacklist() {
            if(rong == null)
                        rong = api.require('rongCloud2');
                    var resultCallback = function(ret, err) {
                        alert(JSON.stringify(ret));
                    }
                    var param = {userId: "54"};

            rong.removeFromBlacklist(param, resultCallback);
        }

        function getBlacklist() {
                if(rong == null)
                        rong = api.require('rongCloud2');
                    var resultCallback = function(ret, err) {
                        alert(JSON.stringify(ret));
                    }

                rong.getBlacklist(resultCallback);
        }

        function getConnectionStatus() {
                        if(rong == null)
                                rong = api.require('rongCloud2');
                            var resultCallback = function(ret, err) {
                                alert(JSON.stringify(ret));
                            }

                        rong.getConnectionStatus(resultCallback);
                }

        function disconnect() {
            if(rong == null)
                rong = api.require('rongCloud2');
            var param = {
                isReceivePush: false
            };
            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }
            rong.disconnect(null, resultCallback);
        }

        function logout() {
            if(rong == null)
                rong = api.require('rongCloud2');
            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }
            rong.logout(null, resultCallback);
        }

        function getGroupConversationList() {
            if(rong == null)
                rong = api.require('rongCloud2');
            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }

            rong.getGroupConversationList(null, resultCallback);
        }

        function getConversation() {
            if(rong == null)
                rong = api.require('rongCloud2');
            var param = {
                conversationType: "PRIVATE",
                targetId: "16"
            };
            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }

            rong.getConversation(param, resultCallback);
        }

        function getConversationList() {
            if(rong == null)
                        rong = api.require('rongCloud2');
            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }

            rong.getConversationList(null, resultCallback);
        }

        function removeConversation() {
            if(rong == null)
                        rong = api.require('rongCloud2');
            var param = {
                conversationType: "PRIVATE",
                targetId: "16"
            };

            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }

            rong.removeConversation(param, resultCallback);
        }

        function setConversationToTop() {
            if(rong == null)
                        rong = api.require('rongCloud2');
            var param = {
                conversationType: "PRIVATE",
                targetId: "16",
                isTop: true
            };

            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }

            rong.setConversationToTop(param, resultCallback);
        }

        function getTotalUnreadCount() {
            if(rong == null)
                        rong = api.require('rongCloud2');

            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }

            rong.getTotalUnreadCount(null, resultCallback);
        }

        function getUnreadCount() {
            if(rong == null)
                        rong = api.require('rongCloud2');
            var param = {
                conversationType: "PRIVATE",
                targetId: "16"
            };

            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }

            rong.getUnreadCount(param, resultCallback);
        }

        function getUnreadCountByConversationTypes() {
            if(rong == null)
                        rong = api.require('rongCloud2');
            var param = {
                conversationTypes: ["PRIVATE", "GROUP", "DISCUSSION"]
            };

            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }

            rong.getUnreadCount(param, resultCallback);
        }

        function getLatestMessages() {
            if(rong == null)
                rong = api.require('rongCloud2');
            var param = {
                conversationType: "PRIVATE",
                targetId: "55",
                count: 2
            };

            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }

            rong.getLatestMessages(param, resultCallback);
        }

        function getHistoryMessages() {
            if(rong == null)
                rong = api.require('rongCloud2');
            var param = {
                conversationType: "PRIVATE",
                targetId: "55",
                count: 2,
                oldestMessageId: 10
            };

            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }

            rong.getHistoryMessages(param, resultCallback);
        }

        function deleteMessages() {
            if(rong == null)
                        rong = api.require('rongCloud2');
            var param = {
                messageIds: [1, 2, 3]
            };

            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }

            rong.deleteMessages(param, resultCallback);
        }

        function clearMessages() {
            if(rong == null)
                        rong = api.require('rongCloud2');
            var param = {
                conversationType: "PRIVATE",
                targetId: "16"
            }

            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }

            rong.clearMessages(param, resultCallback);
        }

        function clearMessagesUnreadStatus() {
            if(rong == null)
                        rong = api.require('rongCloud2');
            var param = {
                conversationType: "PRIVATE",
                targetId: "16"
            }

            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }

            rong.clearMessagesUnreadStatus(param, resultCallback);
        }

        function setMessageExtra() {
            if(rong == null)
                        rong = api.require('rongCloud2');
            var param = {
                messageId: 1,
                value: "test"
            }

            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }

            rong.setMessageExtra(param, resultCallback);
        }

        // READ = 1; LISTENED = 2; DOWNLOADED = 4;
        function setMessageReceivedStatus() {
            if(rong == null)
                        rong = api.require('rongCloud2');
            var param = {
                messageId: 1,
                receivedStatus: "LISTENED"
            }

            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }

            rong.setMessageReceivedStatus(param, resultCallback);
        }

        //SENDING, FAILED, SENT, RECEIVED, READ, DESTROYED;
        function setMessageSentStatus() {
            if(rong == null)
                rong = api.require('rongCloud2');
            var param = {
                messageId: 1,
                sentStatus: "SENDING"
            }

            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }

            rong.setMessageSentStatus(param, resultCallback);
        }

        function getTextMessageDraft() {
            var param = {
                conversationType: "PRIVATE",
                targetId: "16"
            }

            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }

            rong.getTextMessageDraft(param, resultCallback);
        }


        function setTextMessageDraft() {
            var param = {
                conversationType: "PRIVATE",
                targetId: "16",
                content: "test_draft"
            }

            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }

            rong.setTextMessageDraft(param, resultCallback);
        }

        function clearTextMessageDraft() {
            var param = {
                conversationType: "PRIVATE",
                targetId: "16"
            }

            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }

            rong.clearTextMessageDraft(param, resultCallback);
        }

        function createDiscussion() {
            var param = {
                name: "discussion",
                userIdList: ["16", "55", "66"]
            }

            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }

            if(rong == null)
                rong = api.require('rongCloud2');

            rong.createDiscussion(param, resultCallback);
        }

        function getDiscussion() {
            var param = {
                discussionId: "1b9f7abe-a5ae-463d-8ff8-d96deaf40b59"
            }

            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }

            if(rong == null)
                rong = api.require('rongCloud2');
            rong.getDiscussion(param, resultCallback);
        }

        function setDiscussionName() {
            var param = {
                discussionId: "1b9f7abe-a5ae-463d-8ff8-d96deaf40b59",
                name: "test_discussion_name"
            }

            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }

            rong.setDiscussionName(param, resultCallback);
        }

        function addMemberToDiscussion() {
            var param = {
                discussionId: "1b9f7abe-a5ae-463d-8ff8-d96deaf40b59",
                userIds: ["26", "65", "76"]
            }

            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }

            rong.addMemberToDiscussion(param, resultCallback);
        }

        function removeMemberFromDiscussion() {
            var param = {
                discussionId: "1b9f7abe-a5ae-463d-8ff8-d96deaf40b59",
                userId: "26"
            }

            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }

            rong.removeMemberFromDiscussion(param, resultCallback);
        }

        function quitDiscussion() {
            var param = {
                discussionId: "1b9f7abe-a5ae-463d-8ff8-d96deaf40b59"
            }

            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }

            rong.quitDiscussion(param, resultCallback);
        }


        function sendTextMessage() {
            if(rong == null)
                rong = api.require('rongCloud2');
            var param = {
                conversationType: "PRIVATE",
                targetId: "55",
                text: "call this cloud"
            };
            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }

            rong.sendTextMessage(param, resultCallback);
        }

        function sendImageMessage() {
            if(rong == null)
                rong = api.require('rongCloud2');
            var param = {
                conversationType: "PRIVATE",
                targetId: "55",
                thumPath: "file:///sdcard/1.jpg",
                imagePath: "file:///sdcard/1.jpg"
            };
            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }

            rong.sendImageMessage(param, resultCallback);
        }

        function sendVoiceMessage() {
            var param = {
                conversationType: "PRIVATE",
                targetId: "16",
                voiceUri: "file:///XXXX",
                duration: 5000
            };
            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }

            rong.sendVoiceMessage(param, resultCallback);
        }

        function sendRichContentMessage() {
            var param = {
                conversationType: "PRIVATE",
                targetId: "16",
                title: "rich_title",
                content: "rich_content",
                imageUrl: "http://XXXX"
            };
            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }

            rong.sendRichContentMessage(param, resultCallback);
        }

        function sendLocationMessage() {
            if(rong == null)
                rong = api.require('rongCloud2');
            var param = {
                conversationType: "PRIVATE",
                targetId: "55",
                title: "rich_title",
                latitude: 39.8802147,
                longitude: 116.415794,
                poi: "location_poi_info",
                imagePath: "file:///sdcard/1.jpg",
                extra: ""
            };
            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }

            rong.sendLocationMessage(param, resultCallback);
        }

        function sendCommandNotificationMessage() {
            var param = {
                conversationType: "PRIVATE",
                targetId: "16",
                name: "commont_name",
                data: "commont_data"
            };
            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }

            rong.sendCommandNotificationMessage(param, resultCallback);
        }

        function getConversationNotificationStatus() {
            var param = {
                conversationType: "PRIVATE",
                targetId: "16"
            };
            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }

            rong.getConversationNotificationStatus(param, resultCallback);
        }

        //DO_NOT_DISTURB, NOTIFY;
        function setConversationNotificationStatus() {
            var param = {
                conversationType: "PRIVATE",
                targetId: "16",
                conversationNotificationStatus: "DO_NOT_DISTURB"
            };
            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }

            rong.setConversationNotificationStatus(param, resultCallback);
        }

        //CLOSED, OPENED;
        function setDiscussionInviteStatus() {
            var param = {
                targetId: "ac62578f-601b-49aa-99cb-9e90dac84fde",
                discussionInviteStatus: "CLOSED"
            };
            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }

            rong.setDiscussionInviteStatus(param, resultCallback);
        }


        function syncGroup() {
            var param = {
                groups: [{
                    id: "group_id",
                    name: "group_name",
                    portraitUri: "http://XXX"
                }, {
                    id: "group_id2",
                    name: "group_name2",
                    portraitUri: "http://XXX"
                }]
            };
            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }

            rong.syncGroup(param, resultCallback);
        }


        function joinGroup() {
            var param = {
                groupId: "group_id",
                groupName: "group_name"
            };
            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }

            rong.joinGroup(param, resultCallback);
        }

        function quitGroup() {
            var param = {
                groupId: "group_id"
            };
            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }

            rong.quitGroup(param, resultCallback);
        }

        function getUserInfo() {
            var param = {
                userId: "16"
            };
            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }

            rong.getUserInfo(param, resultCallback);
        }

        function getCurrentUserInfo() {
            var param = {
                userId: "16"
            };
            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }

            rong.getCurrentUserInfo(param, resultCallback);
        }

        function getDeltaTime() {
            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }

            rong.getDeltaTime(null, resultCallback);
        }

        function joinChatRoom() {
            var param = {
                chatRoomId: "chatroom",
                defMessageCount: "10"
            };
            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }

            rong.joinChatRoom(param, resultCallback);
        }

        function quitChatRoom() {
            var param = {
                groupId: "chatroom"
            };
            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }

            rong.quitChatRoom(param, resultCallback);
        }

        function clearConversations() {
            var param = {
                conversationTypes: ["GROUP", "DISCUSSION"]
            };
            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }

            rong.clearConversations(param, resultCallback);
        }

        function setOnReceiveMessageListener() {
            if(rong == null)
                rong = api.require('rongCloud2');
            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }

            rong.setOnReceiveMessageListener(null, resultCallback);
        }

        function setOnReceivePushMessageListener() {
                    if(rong == null)
                        rong = api.require('rongCloud2');
                    var resultCallback = function(ret, err) {
                        alert(JSON.stringify(ret));
                    }

                    rong.setOnReceivePushMessageListener(null, resultCallback);
                }

        function setConnectionStatusListener() {
            if(rong == null)
                rong = api.require('rongCloud2');
            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }
            rong.setConnectionStatusListener(null, resultCallback);
        }
