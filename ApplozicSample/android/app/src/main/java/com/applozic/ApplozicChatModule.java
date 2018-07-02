package com.applozic;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.applozic.mobicomkit.ALGroupInfoTask;
import com.applozic.mobicomkit.Applozic;
import com.applozic.mobicomkit.api.account.register.RegistrationResponse;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.account.user.User;
import com.applozic.mobicomkit.api.account.user.UserClientService;
import com.applozic.mobicomkit.api.account.user.UserLoginTask;
import com.applozic.mobicomkit.api.account.user.PushNotificationTask;
import com.applozic.mobicomkit.api.conversation.MobiComConversationService;
import com.applozic.mobicomkit.api.conversation.database.MessageDatabaseService;
import com.applozic.mobicomkit.api.people.ChannelInfo;
import com.applozic.mobicomkit.channel.service.ChannelService;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.listners.ApplozicUIListener;
import com.applozic.mobicomkit.listners.MediaDownloadProgressHandler;
import com.applozic.mobicomkit.listners.MediaUploadProgressHandler;
import com.applozic.mobicomkit.uiwidgets.async.AlGroupInformationAsyncTask;
import com.applozic.mobicomkit.uiwidgets.async.ApplozicChannelAddMemberTask;
import com.applozic.mobicomkit.uiwidgets.conversation.ConversationUIService;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.ConversationActivity;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.contact.Contact;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.applozic.mobicomkit.uiwidgets.async.ApplozicChannelRemoveMemberTask;
import com.applozic.mobicomkit.api.conversation.ApplozicConversation;
import com.applozic.mobicomkit.listners.MessageListHandler;
import com.applozic.mobicomkit.exception.ApplozicException;
import com.applozic.mobicomkit.api.conversation.Message;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;


import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ApplozicChatModule extends ReactContextBaseJavaModule implements ActivityEventListener {

    ApplozicUIListener listener;

    public ApplozicChatModule(ReactApplicationContext reactContext) {
        super(reactContext);
        reactContext.addActivityEventListener(this);
    }

    @Override
    public String getName() {
        return "ApplozicChat";
    }

    @ReactMethod
    public void login(final ReadableMap config, final Callback callback) {
        final Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            callback.invoke("Activity doesn't exist", null);

            return;
        }

        UserLoginTask.TaskListener listener = new UserLoginTask.TaskListener() {
            @Override
            public void onSuccess(RegistrationResponse registrationResponse, Context context) {
                //After successful registration with Applozic server the callback will come here
                if (MobiComUserPreference.getInstance(currentActivity).isRegistered()) {
                    String json = GsonUtils.getJsonFromObject(registrationResponse, RegistrationResponse.class);
                    callback.invoke(null, json);

                    PushNotificationTask pushNotificationTask = null;

                    PushNotificationTask.TaskListener listener = new PushNotificationTask.TaskListener() {
                        public void onSuccess(RegistrationResponse registrationResponse) {

                        }

                        @Override
                        public void onFailure(RegistrationResponse registrationResponse, Exception exception) {
                        }
                    };
                    String registrationId = Applozic.getInstance(context).getDeviceRegistrationId();
                    pushNotificationTask = new PushNotificationTask(registrationId, listener, currentActivity);
                    pushNotificationTask.execute((Void) null);
                } else {
                    String json = GsonUtils.getJsonFromObject(registrationResponse, RegistrationResponse.class);
                    callback.invoke(json, null);

                }

            }

            @Override
            public void onFailure(RegistrationResponse registrationResponse, Exception exception) {
                //If any failure in registration the callback  will come here
                if (registrationResponse != null) {
                    callback.invoke(null, registrationResponse.toString());
                }
                if (exception != null) {
                    callback.invoke(exception.toString(), null);
                }

            }
        };

        User user = new User();
        user.setUserId(config.getString("userId")); //userId it can be any unique user identifier
        user.setDisplayName(config.getString("displayName")); //displayName is the name of the user which will be shown in chat messages
        user.setEmail(config.getString("email")); //optional
        user.setAuthenticationTypeId(User.AuthenticationType.APPLOZIC.getValue());  //User.AuthenticationType.APPLOZIC.getValue() for password verification from Applozic server and User.AuthenticationType.CLIENT.getValue() for access Token verification from your server set access token as password
        user.setPassword(config.getString("password")); //optional, leave it blank for testing purpose, read this if you want to add additional security by verifying password from your server https://www.applozic.com/docs/configuration.html#access-token-url
        user.setImageLink("");//optional,pass your image link
        user.setApplicationId("applozic-sample-app");
        new UserLoginTask(user, listener, currentActivity).execute((Void) null);
    }

    @ReactMethod
    public void openChat() {
        Activity currentActivity = getCurrentActivity();

        if (currentActivity == null) {
            Log.i("OpenChat Error ", "Activity doesn't exist");
            return;
        }

        Intent intent = new Intent(currentActivity, ConversationActivity.class);
        currentActivity.startActivity(intent);
    }

    @ReactMethod
    public void openChatWithUser(String userId) {
        Activity currentActivity = getCurrentActivity();

        if (currentActivity == null) {
            Log.i("open ChatWithUser  ", "Activity doesn't exist");
            return;
        }

        Intent intent = new Intent(currentActivity, ConversationActivity.class);

        if (userId != null) {

            intent.putExtra(ConversationUIService.USER_ID, userId);
            intent.putExtra(ConversationUIService.TAKE_ORDER, true);

        }
        currentActivity.startActivity(intent);
    }

    @ReactMethod
    public void openChatWithGroup(Integer groupId, final Callback callback) {

        Activity currentActivity = getCurrentActivity();
        Intent intent = new Intent(currentActivity, ConversationActivity.class);

        if (groupId != null) {

            ChannelService channelService = ChannelService.getInstance(currentActivity);
            Channel channel = channelService.getChannel(groupId);

            if (channel == null) {
                callback.invoke("Channel dose not exist", null);
                return;
            }
            intent.putExtra(ConversationUIService.GROUP_ID, channel.getKey());
            intent.putExtra(ConversationUIService.TAKE_ORDER, true);
            currentActivity.startActivity(intent);
            callback.invoke(null, "success");

        } else {
            callback.invoke("unable to launch group chat, check your groupId/ClientGroupId", "success");
        }

    }

    @ReactMethod
    public void openChatWithClientGroupId(String clientGroupId, final Callback callback) {

        Activity currentActivity = getCurrentActivity();
        Intent intent = new Intent(currentActivity, ConversationActivity.class);

        if (TextUtils.isEmpty(clientGroupId)) {
            callback.invoke("unable to launch group chat, check your groupId/ClientGroupId", "success");
        } else {
            ChannelService channelService = ChannelService.getInstance(currentActivity);
            Channel channel = channelService.getChannelByClientGroupId(clientGroupId);

            if (channel == null) {
                callback.invoke("Channel dose not exist", null);
                return;
            }
            intent.putExtra(ConversationUIService.GROUP_ID, channel.getKey());
            intent.putExtra(ConversationUIService.TAKE_ORDER, true);
            currentActivity.startActivity(intent);
            callback.invoke(null, "success");

        }

    }

    @ReactMethod
    public void logoutUser(final Callback callback) {

        Activity currentActivity = getCurrentActivity();

        if (currentActivity == null) {
            callback.invoke("Activity doesn't exist");
            return;
        }

        new UserClientService(currentActivity).logout();
        callback.invoke(null, "success");
    }

    //============================================ Group Method ==============================================

    /***
     *
     * @param config
     * @param callback
     */
    @ReactMethod
    public void createGroup(final ReadableMap config, final Callback callback) {

        final Activity currentActivity = getCurrentActivity();

        if (currentActivity == null) {

            callback.invoke("Activity doesn't exist", null);
            return;

        }

        if (TextUtils.isEmpty(config.getString("groupName"))) {

            callback.invoke("Group name must be passed", null);
            return;
        }

        List<String> channelMembersList = (List<String>) (Object) (config.getArray("groupMemberList").toArrayList());

        final ChannelInfo channelInfo = new ChannelInfo(config.getString("groupName"), channelMembersList);

        if (!TextUtils.isEmpty(config.getString("clientGroupId"))) {
            channelInfo.setClientGroupId(config.getString("clientGroupId"));
        }
        if (config.hasKey("type")) {
            channelInfo.setType(config.getInt("type")); //group type
        } else {
            channelInfo.setType(Channel.GroupType.PUBLIC.getValue().intValue()); //group type
        }
        channelInfo.setImageUrl(config.getString("imageUrl")); //pass group image link URL
        Map<String, String> metadata = (HashMap<String, String>) (Object) (config.getMap("metadata").toHashMap());
        channelInfo.setMetadata(metadata);

        new Thread(new Runnable() {
            @Override
            public void run() {

                Channel channel = ChannelService.getInstance(currentActivity).createChannel(channelInfo);
                if (channel != null && channel.getKey() != null) {
                    callback.invoke(null, channel.getKey());
                } else {
                    callback.invoke("error", null);
                }
            }
        }).start();
    }

    /**
     * @param config
     * @param callback
     */
    @ReactMethod
    public void addMemberToGroup(final ReadableMap config, final Callback callback) {

        final Activity currentActivity = getCurrentActivity();

        if (currentActivity == null) {

            callback.invoke("Activity doesn't exist", null);
            return;

        }

        Integer channelKey = null;
        String userId = config.getString("userId");

        if (!TextUtils.isEmpty(config.getString("clientGroupId"))) {
            Channel channel = ChannelService.getInstance(currentActivity).getChannelByClientGroupId(config.getString("clientGroupId"));
            channelKey = channel != null ? channel.getKey() : null;

        } else if (!TextUtils.isEmpty(config.getString("groupId"))) {
            channelKey = Integer.parseInt(config.getString("groupId"));
        }

        if (channelKey == null) {
            callback.invoke("groupId/clientGroupId not passed", null);
            return;
        }

        ApplozicChannelAddMemberTask.ChannelAddMemberListener channelAddMemberListener = new ApplozicChannelAddMemberTask.ChannelAddMemberListener() {
            @Override
            public void onSuccess(String response, Context context) {
                //Response will be "success" if user is added successfully
                Log.i("ApplozicChannelMember", "Add Response:" + response);
                callback.invoke(null, response);
            }

            @Override
            public void onFailure(String response, Exception e, Context context) {
                callback.invoke(response, null);

            }
        };

        ApplozicChannelAddMemberTask applozicChannelAddMemberTask = new ApplozicChannelAddMemberTask(currentActivity, channelKey, userId, channelAddMemberListener);//pass channel key and userId whom you want to add to channel
        applozicChannelAddMemberTask.execute((Void) null);
    }


    /**
     * @param config
     * @param callback
     */
    @ReactMethod
    public void removeUserFromGroup(final ReadableMap config, final Callback callback) {

        final Activity currentActivity = getCurrentActivity();

        if (currentActivity == null) {

            callback.invoke("Activity doesn't exist", null);
            return;

        }

        Integer channelKey = null;
        String userId = config.getString("userId");

        if (!TextUtils.isEmpty(config.getString("clientGroupId"))) {
            Channel channel = ChannelService.getInstance(currentActivity).getChannelByClientGroupId(config.getString("clientGroupId"));
            channelKey = channel != null ? channel.getKey() : null;

        } else if (!TextUtils.isEmpty(config.getString("groupId"))) {
            channelKey = Integer.parseInt(config.getString("groupId"));
        }

        if (channelKey == null) {
            callback.invoke("groupId/clientGroupId not passed", null);
            return;
        }

        ApplozicChannelRemoveMemberTask.ChannelRemoveMemberListener channelRemoveMemberListener = new ApplozicChannelRemoveMemberTask.ChannelRemoveMemberListener() {
            @Override
            public void onSuccess(String response, Context context) {
                callback.invoke(null, response);
                //Response will be "success" if user is removed successfully
                Log.i("ApplozicChannel", "remove member response:" + response);
            }

            @Override
            public void onFailure(String response, Exception e, Context context) {
                callback.invoke(response, null);

            }
        };

        ApplozicChannelRemoveMemberTask applozicChannelRemoveMemberTask = new ApplozicChannelRemoveMemberTask(currentActivity, channelKey, userId, channelRemoveMemberListener);//pass channelKey and userId whom you want to remove from channel
        applozicChannelRemoveMemberTask.execute((Void) null);
    }
    //======================================================================================================

    @ReactMethod
    public void getUnreadCountForUser(String userId, final Callback callback) {

        Activity currentActivity = getCurrentActivity();

        if (currentActivity == null) {
            callback.invoke("Activity doesn't exist", null);
            return;
        }

        int contactUnreadCount = new MessageDatabaseService(getCurrentActivity()).getUnreadMessageCountForContact(userId);
        callback.invoke(null, contactUnreadCount);

    }

    @ReactMethod
    public void getUnreadCountForChannel(ReadableMap config, final Callback callback) {
        Activity currentActivity = getCurrentActivity();

        if (currentActivity == null) {
            callback.invoke("Activity doesn't exist", null);
            return;
        }
        if (config != null && config.hasKey("clientGroupId")) {
            ChannelService channelService = ChannelService.getInstance(currentActivity);
            Channel channel = channelService.getChannelByClientGroupId(config.getString("clientGroupId"));

            if (channel == null) {
                callback.invoke("Channel dose not exist", null);
                return;
            }

            int channelUnreadCount = new MessageDatabaseService(currentActivity).getUnreadMessageCountForChannel(channel.getKey());
            callback.invoke(null, channelUnreadCount);

        } else if (config != null && config.hasKey("groupId")) {

            int channelUnreadCount = new MessageDatabaseService(currentActivity).getUnreadMessageCountForChannel((Integer.parseInt(config.getString("channelKey"))));
            callback.invoke(null, channelUnreadCount);

        }
    }

    @ReactMethod
    public void totalUnreadCount(final Callback callback) {
        Activity currentActivity = getCurrentActivity();

        if (currentActivity == null) {
            callback.invoke("Activity doesn't exist", null);
            return;
        }

        int totalUnreadCount = new MessageDatabaseService(currentActivity).getTotalUnreadCount();
        callback.invoke(null, totalUnreadCount);

    }

    @ReactMethod
    public void isUserLogIn(final Callback successCallback) {

        Activity currentActivity = getCurrentActivity();
        MobiComUserPreference mobiComUserPreference = MobiComUserPreference.getInstance(currentActivity);
        successCallback.invoke(mobiComUserPreference.isLoggedIn());
    }

    @Override
    public void onActivityResult(Activity activity, final int requestCode, final int resultCode, final Intent intent) {
    }

    @Override
    public void onNewIntent(Intent intent) {
    }
    //============================================ Custom UI ==============================================

    @ReactMethod
    public void getLatestMessageList(final ReadableMap args, final Callback messageListCallback) {
        final Activity currentActivity = getCurrentActivity();
        ApplozicConversation.getLatestMessageList(currentActivity, "true".equals(args.getString("isScroll")), new MessageListHandler() {
            @Override
            public void onResult(List<Message> messageList, ApplozicException e) {
                if (e == null) {
                    messageListCallback.invoke(null, GsonUtils.getJsonFromObject(messageList.toArray(), Message[].class));
                } else {
                    messageListCallback.invoke(GsonUtils.getJsonFromObject(e, ApplozicException.class), null);
                }
            }
        });
    }

    @ReactMethod
    public void addLatestMessage(final ReadableMap args, final Callback addMessageCallback) {

        List<Message> messageList = Arrays.asList((Message[]) GsonUtils.getObjectFromJson(args.getString("messageList"), Message[].class));
        Message message = (Message) GsonUtils.getObjectFromJson(args.getString("message"), Message.class);

        ApplozicConversation.addLatestMessage(message, messageList);
        addMessageCallback.invoke(GsonUtils.getJsonFromObject(messageList.toArray(), Message[].class));
    }

    @ReactMethod
    public void getMessageListForContact(final ReadableMap args, final Callback listCallback) {
        final Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            listCallback.invoke("Activity doesn't exists..", null);
            return;
        }

        Long time = null;

        if(args.hasKey("createdAtTime")){
            time = (long) args.getDouble("createdAtTime");
        }

        ApplozicConversation.getMessageListForContact(currentActivity, args.getString("userId"), time, new MessageListHandler() {
            @Override
            public void onResult(List<Message> messageList, ApplozicException e) {
                if (e == null) {
                    listCallback.invoke(null, GsonUtils.getJsonFromObject(messageList.toArray(), Message[].class));
                } else {
                    listCallback.invoke(GsonUtils.getJsonFromObject(e, ApplozicException.class), null);
                }
            }
        });
    }

    @ReactMethod
    public void getMessageListForChannel(final ReadableMap args, final Callback listCallback) {
        final Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            listCallback.invoke("Activity doesn't exists..", null);
            return;
        }

        Long time = null;

        if(args.hasKey("createdAtTime")){
            time = (long) args.getDouble("createdAtTime");
        }

        ApplozicConversation.getMessageListForChannel(currentActivity, args.getInt("groupId"), time, new MessageListHandler() {
            @Override
            public void onResult(List<Message> messageList, ApplozicException e) {
                if (e == null) {
                    listCallback.invoke(null, GsonUtils.getJsonFromObject(messageList.toArray(), Message[].class));
                } else {
                    listCallback.invoke(GsonUtils.getJsonFromObject(e, ApplozicException.class), null);
                }
            }
        });
    }

    @ReactMethod
    public void sendMessage(final String messageJson, final Callback callback) {
        final Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            callback.invoke("error", "Activity doesn't exists..");
            return;
        }

        final Message message = (Message) GsonUtils.getObjectFromJson(messageJson, Message.class);

        if (message == null) {
            callback.invoke("error", "Unable to parse data to Applozic Message");
            return;
        }

        new MobiComConversationService(currentActivity).sendMessage(message, new MediaUploadProgressHandler() {
            @Override
            public void onUploadStarted(ApplozicException e) {
                //Utils.printLog(currentActivity, "UpTest", "Upload started " + message.getCreatedAtTime() + ", " + "Exception : " + e);
                if (e == null) {
                    WritableMap map = Arguments.createMap();
                    map.putDouble("createdAtTime", message.getCreatedAtTime());
                    sendEvent("UploadStarted", map);
                } else if (message.getFilePaths() != null && !message.getFilePaths().isEmpty()) {
                    callback.invoke("error", e.getCause());
                }
            }

            @Override
            public void onProgressUpdate(int percentage, ApplozicException e) {
                //Utils.printLog(currentActivity, "UpTest", "Upload progress " + message.getCreatedAtTime() + ", " + "Exception : " + e + ", " + percentage);
                if (e == null) {
                    WritableMap map = Arguments.createMap();
                    map.putDouble("createdAtTime", message.getCreatedAtTime());
                    map.putInt("uploadProgress", percentage);
                    sendEvent("UploadProgress", map);
                } else if (message.getFilePaths() != null && !message.getFilePaths().isEmpty()) {
                    callback.invoke("error : ", e.getCause());
                }
            }

            @Override
            public void onCancelled(ApplozicException e) {
                //Utils.printLog(currentActivity, "UpTest", "Upload cancelled" + message.getCreatedAtTime() + ", " + "Exception : " + e);
                if (e == null) {
                    WritableMap map = Arguments.createMap();
                    map.putDouble("createdAtTime", message.getCreatedAtTime());
                    sendEvent("UploadCancelled", map);
                } else if (message.getFilePaths() != null && !message.getFilePaths().isEmpty()) {
                    callback.invoke("error : ", e.getCause());
                }
            }

            @Override
            public void onCompleted(ApplozicException e) {
                //Utils.printLog(currentActivity, "UpTest", "Upload completed " + message.getCreatedAtTime() + ", " + "Exception : " + e);
                if (e == null) {
                    WritableMap map = Arguments.createMap();
                    map.putDouble("createdAtTime", message.getCreatedAtTime());
                    sendEvent("UploadCompleted", map);
                } else if (message.getFilePaths() != null && !message.getFilePaths().isEmpty()) {
                    callback.invoke("error : ", e.getCause());
                }
            }

            @Override
            public void onSent(Message message) {
                callback.invoke("Message sent", GsonUtils.getJsonFromObject(message, Message.class));
            }
        });
    }

    @ReactMethod
    public void prepareMessage(String message, final Callback callback) {
        callback.invoke(GsonUtils.getJsonFromObject((Message) GsonUtils.getObjectFromJson((message), Message.class), Message.class));
    }

    @ReactMethod
    public void downloadMessage(String messageKeyString, final Callback callback) {
        final Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            callback.invoke("Activity doesn't exists..", null);
            return;
        }
        final Message message = new MessageDatabaseService(currentActivity).getMessage(messageKeyString);

        if (message == null) {
            callback.invoke("Unable to parse data to Applozic Message", null);
            return;
        }

        ApplozicConversation.downloadMessage(currentActivity, message, new MediaDownloadProgressHandler() {
            @Override
            public void onDownloadStarted() {
                WritableMap map = Arguments.createMap();
                map.putString("messageKey", message.getKeyString());
                sendEvent("DownloadStarted", map);
            }

            @Override
            public void onProgressUpdate(int percentage, ApplozicException e) {
                WritableMap map = Arguments.createMap();
                map.putString("messageKey", message.getKeyString());
                map.putInt("downloadProgress", percentage);
                if (e != null) {
                    callback.invoke("Error", e.getMessage());
                }
                sendEvent("DownloadProgress", map);
            }

            @Override
            public void onCompleted(Message message, ApplozicException e) {
                if (e != null) {
                    callback.invoke("Error", e.getMessage());
                } else {
                    callback.invoke("Success", GsonUtils.getJsonFromObject(message, Message.class));
                }
            }
        });
    }

    @ReactMethod
    public void getChannelFromChannelKey(Integer channelKey, final Callback callback) {
        final Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            callback.invoke("Activity doesn't exists..", null);
            return;
        }

        ALGroupInfoTask.ChannelInfoListener listener = new ALGroupInfoTask.ChannelInfoListener() {
            @Override
            public void onSuccess(ALGroupInfoTask.ChannelInfoModel channelInfoModel, String response, Context context) {
                callback.invoke("Success", GsonUtils.getJsonFromObject(channelInfoModel, ALGroupInfoTask.ChannelInfoModel.class));
            }

            @Override
            public void onFailure(String response, Exception e, Context context) {
                callback.invoke("Error", response);
            }
        };

        new ALGroupInfoTask(currentActivity, channelKey, null, true, listener).execute();
    }


    @ReactMethod
    public void getContactById(String userId, final Callback callback) {
        final Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            callback.invoke("Activity doesn't exists..", null);
            return;
        }

        Contact contact = new AppContactService(currentActivity).getContactById(userId);

        if (contact != null) {
            callback.invoke("Success", GsonUtils.getJsonFromObject(contact, Contact.class));
        } else {
            callback.invoke("Error", null);
        }
    }

    @ReactMethod
    public void getMessageByKey(String messageKey, final Callback callback) {
        final Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            callback.invoke("Activity doesn't exists..", null);
            return;
        }

        Message message = new MessageDatabaseService(currentActivity).getMessage(messageKey);

        if (message != null) {
            callback.invoke("Success", GsonUtils.getJsonFromObject(message, Message.class));
        } else {
            callback.invoke("Error", "Some error occurred");
        }
    }

    @ReactMethod
    public void getMessageAttachmentType(String messageKey, final Callback callback) {
        final Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            callback.invoke("Activity doesn't exists..", null);
            return;
        }

        Message message = new MessageDatabaseService(currentActivity).getMessage(messageKey);

        if (message != null) {
            callback.invoke("Success", message.getAttachmentType());
        } else {
            callback.invoke("Error", "Some error occurred");
        }
    }

    @ReactMethod
    public void registerListener(final Callback callback) {
        final Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            callback.invoke("Activity doesn't exists..");
            return;
        }

        Applozic.connectPublish(currentActivity);

        listener = new ApplozicUIListener() {
            @Override
            public void onMessageSent(Message message) {
                WritableMap map = Arguments.createMap();
                map.putString("message", GsonUtils.getJsonFromObject(message, Message.class));
                sendEvent("onMessageSent", map);
            }

            @Override
            public void onMessageReceived(Message message) {
                WritableMap map = Arguments.createMap();
                map.putString("message", GsonUtils.getJsonFromObject(message, Message.class));
                sendEvent("onMessageReceived", map);
            }

            @Override
            public void onLoadMore(boolean loadMore) {
                WritableMap map = Arguments.createMap();
                map.putBoolean("loadMore", loadMore);
                sendEvent("onLoadMore", map);
            }

            @Override
            public void onMessageSync(Message message, String key) {
                WritableMap map = Arguments.createMap();
                map.putString("message", GsonUtils.getJsonFromObject(message, Message.class));
                sendEvent("onMessageSync", map);
            }

            @Override
            public void onMessageDeleted(String messageKey, String userId) {
                WritableMap map = Arguments.createMap();
                map.putString("messageKey", messageKey);
                map.putString("userId", userId);
                sendEvent("onMessageDeleted", map);
            }

            @Override
            public void onMessageDelivered(Message message, String userId) {
                WritableMap map = Arguments.createMap();
                map.putString("message", GsonUtils.getJsonFromObject(message, Message.class));
                map.putString("userId", userId);
                sendEvent("onMessageDelivered", map);
            }

            @Override
            public void onAllMessagesDelivered(String userId) {
                WritableMap map = Arguments.createMap();
                map.putString("userId", userId);
                sendEvent("onAllMessagesDelivered", map);
            }

            @Override
            public void onAllMessagesRead(String userId) {
                WritableMap map = Arguments.createMap();
                map.putString("userId", userId);
                sendEvent("onAllMessagesRead", map);
            }

            @Override
            public void onConversationDeleted(String userId, Integer channelKey, String response) {
                WritableMap map = Arguments.createMap();
                if (userId != null) {
                    map.putString("userId", userId);
                }
                if (channelKey != null && channelKey != 0) {
                    map.putInt("channelKey", channelKey);
                }
                map.putString("reponse", response);

                sendEvent("onConversationDeleted", map);
            }

            @Override
            public void onUpdateTypingStatus(String userId, String isTyping) {
                WritableMap map = Arguments.createMap();
                map.putString("userId", userId);
                map.putBoolean("isTyping", "true".equals(isTyping));
                sendEvent("onUpdateTypingStatus", map);
            }

            @Override
            public void onUpdateLastSeen(String userId) {
                WritableMap map = Arguments.createMap();
                map.putString("userId", userId);
                sendEvent("onUpdateLastSeen", map);
            }

            @Override
            public void onMqttDisconnected() {
                WritableMap map = Arguments.createMap();
                map.putString("status", "disconnected");
                sendEvent("onMqttDisconnected", map);
            }

            @Override
            public void onChannelUpdated() {
                WritableMap map = Arguments.createMap();
                map.putString("status", "updated");
                sendEvent("onChannelUpdated", map);
            }

            @Override
            public void onConversationRead(String userId, boolean isGroup) {
                WritableMap map = Arguments.createMap();
                map.putString("userId", userId);
                map.putBoolean("isGroup", isGroup);
                sendEvent("onConversationRead", map);
            }

            @Override
            public void onUserDetailUpdated(String userId) {
                WritableMap map = Arguments.createMap();
                map.putString("userId", userId);
                sendEvent("onUserDetailUpdated", map);
            }

            @Override
            public void onMessageMetadataUpdated(String keyString) {
                WritableMap map = Arguments.createMap();
                map.putString("messageKey", keyString);
                sendEvent("onMessageMetadataUpdated", map);
            }
        };

        Applozic.getInstance(currentActivity).registerUIListener(listener);
    }

    @ReactMethod
    public void unregisterListener(final Callback callback) {
        final Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            callback.invoke("Activity doesn't exists..");
            return;
        }

        Applozic.disconnectPublish(currentActivity);

        if (listener != null) {
            Applozic.getInstance(currentActivity).unregisterUIListener();
        }
    }

    @ReactMethod
    public void publishTypingStatus(final ReadableMap args, final Callback callback) {
        final Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            callback.invoke("Activity doesn't exists..");
            return;
        }

        if (args.hasKey("channelKey") && args.getInt("channelKey") != 0) {
            AlGroupInformationAsyncTask.GroupMemberListener listener = new AlGroupInformationAsyncTask.GroupMemberListener() {
                @Override
                public void onSuccess(Channel channel, Context context) {
                    Contact contact = null;
                    if (args.hasKey("userId") && args.getString("userId") != null) {
                        contact = new AppContactService(currentActivity).getContactById(args.getString("userId"));
                    }

                    Applozic.publishTypingStatus(context, channel, contact, args.getBoolean("isTyping"));
                    callback.invoke("Successfully published typing status");
                }

                @Override
                public void onFailure(Channel channel, Exception e, Context context) {
                    callback.invoke("Failed to publish Typing status");
                }
            };
        } else {
            if (args.hasKey("userId") && args.getString("userId") != null) {
                Applozic.publishTypingStatus(currentActivity, null, new AppContactService(currentActivity).getContactById(args.getString("userId")), args.getBoolean("isTyping"));
                callback.invoke("Successfully published typing status");
            }
        }
    }

    @ReactMethod
    public void unPublishTypingStatus(final ReadableMap args, final Callback callback) {
        final Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            callback.invoke("Activity doesn't exists..");
            return;
        }

        if (args.hasKey("channelKey") && args.getInt("channelKey") != 0) {
            AlGroupInformationAsyncTask.GroupMemberListener listener = new AlGroupInformationAsyncTask.GroupMemberListener() {
                @Override
                public void onSuccess(Channel channel, Context context) {
                    Contact contact = null;
                    if (args.hasKey("userId") && args.getString("userId") != null) {
                        contact = new AppContactService(currentActivity).getContactById(args.getString("userId"));
                    }

                    Applozic.unSubscribeToTyping(context, channel, contact);
                    callback.invoke("Successfully un-published Typing status");
                }

                @Override
                public void onFailure(Channel channel, Exception e, Context context) {
                    callback.invoke("Failed to un-publish Typing status");
                }
            };
        } else {
            if (args.hasKey("userId") && args.getString("userId") != null) {
                Applozic.publishTypingStatus(currentActivity, null, new AppContactService(currentActivity).getContactById(args.getString("userId")), args.getBoolean("isTyping"));
                callback.invoke("Successfully un-published Typing status");
            }
        }
    }

    private void sendEvent(String eventName, WritableMap data) {
        this.getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("Applozic-" + eventName, data);
    }

}