        var rong = null;
        apiready = function() {
            rong = api.require('rongIMClientModule');
        }

        function init() {
            var param = {
                appKey: "z3v5yqkbv8v30"
            };
            rong.init(param);
        }

        function connect() {
            var param = {
                token: "ThptTWyiPPPvZHvuSiuri82yq+hfEluLjZ78E1qo4hEVSFQNpqdoPu406urMWKN4Z3/olWR+v9JVLAwfOQoLrA=="
            };
            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }
            rong.connect(param, resultCallback);
        }

        function reconnect() {
            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }
            rong.reconnect(null, resultCallback);
        }

        function disconnect() {
            var param = {
                isReceivePush: false
            };
            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }
            rong.disconnect(null, resultCallback);
        }

        function getGroupConversationList() {
            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }

            rong.getGroupConversationList(null, resultCallback);
        }

        function getConversation() {
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
            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }

            rong.getConversationList(null, resultCallback);
        }

        function removeConversation() {
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

            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }

            rong.getTotalUnreadCount(null, resultCallback);
        }

        function getUnreadCount() {
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
            var param = {
                conversationTypes: ["PRIVATE", "GROUP", "DISCUSSION"]
            };

            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }

            rong.getUnreadCount(param, resultCallback);
        }

        function getLatestMessages() {
            var param = {
                conversationType: "PRIVATE",
                targetId: "16",
                count: 2
            };

            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }

            rong.getLatestMessages(param, resultCallback);
        }

        function getHistoryMessages() {
            var param = {
                conversationType: "PRIVATE",
                targetId: "16",
                count: 2,
                oldestMessageId: 10
            };

            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }

            rong.getHistoryMessages(param, resultCallback);
        }

        function deleteMessages() {
            var param = {
                messageIds: [1, 2, 3]
            };

            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }

            rong.deleteMessages(param, resultCallback);
        }

        function clearMessages() {
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
            var param = {
                messageId: 1,
                receivedStatus: 1 + 2 + 4
            }

            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }

            rong.setMessageReceivedStatus(param, resultCallback);
        }

        //SENDING, FAILED, SENT, RECEIVED, READ, DESTROYED;
        function setMessageSentStatus() {
            var param = {
                messageId: 1,
                sentStatus: "READ"
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
                userIds: ["16", "55", "66"]
            }

            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }

            rong.createDiscussion(param, resultCallback);
        }

        function getDiscussion() {
            var param = {
                discussionId: "1b9f7abe-a5ae-463d-8ff8-d96deaf40b59"
            }

            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }

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
            var param = {
                conversationType: "PRIVATE",
                targetId: "16",
                content: "call this cloud"
            };
            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }

            rong.sendTextMessage(param, resultCallback);
        }

        function sendImageMessage() {
            var param = {
                conversationType: "PRIVATE",
                targetId: "16",
                thumUri: "file:///XXXX",
                imageUri: "file:///XXXX"
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
            var param = {
                conversationType: "PRIVATE",
                targetId: "16",
                title: "rich_title",
                lat: 39.8802147,
                lng: 116.415794,
                poi: "location_poi_info",
                imageUri: "file:///XXX"
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
            var resultCallback = function(ret, err) {
                alert(escape(ret.result.content.content));
                alert(JSON.stringify(ret));
            }

            rong.setOnReceiveMessageListener(null, resultCallback);
        }

        function setConnectionStatusListener() {
            var resultCallback = function(ret, err) {
                alert(JSON.stringify(ret));
            }

            rong.setConnectionStatusListener(null, resultCallback);
        }