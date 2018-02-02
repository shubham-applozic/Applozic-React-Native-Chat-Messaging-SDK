package com.applozic;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.applozic.mobicomkit.api.account.register.RegistrationResponse;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.account.user.User;
import com.applozic.mobicomkit.api.account.user.UserClientService;
import com.applozic.mobicomkit.api.account.user.UserLoginTask;
import com.applozic.mobicomkit.api.account.user.PushNotificationTask;
import com.applozic.mobicomkit.api.conversation.database.MessageDatabaseService;
import com.applozic.mobicomkit.channel.service.ChannelService;
import com.applozic.mobicomkit.uiwidgets.conversation.ConversationUIService;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.ConversationActivity;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.people.channel.Channel;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.applozic.mobicomkit.api.account.register.RegisterUserClientService;
import com.applozic.mobicomkit.api.account.register.RegistrationResponse;

import java.util.HashMap;
import java.util.Map;


public class ApplozicChatModule extends ReactContextBaseJavaModule implements ActivityEventListener {

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
                    String json = GsonUtils.getJsonFromObject(registrationResponse,RegistrationResponse.class);
                    callback.invoke(null,json);

                    /*PushNotificationTask pushNotificationTask = null;

                    PushNotificationTask.TaskListener listener = new PushNotificationTask.TaskListener() {
                        public void onSuccess(RegistrationResponse registrationResponse) {

                        }

                        @Override
                        public void onFailure(RegistrationResponse registrationResponse, Exception exception) {
                        }
                    };
                    pushNotificationTask = new PushNotificationTask(config.getString("token"), listener, currentActivity);
                    pushNotificationTask.execute((Void) null);*/
                }else{
                    String json = GsonUtils.getJsonFromObject(registrationResponse,RegistrationResponse.class);
                    callback.invoke(json,null);

                }

            }

            @Override
            public void onFailure(RegistrationResponse registrationResponse, Exception exception) {
                //If any failure in registration the callback  will come here
                callback.invoke(exception.toString(), registrationResponse.toString());

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
           Log.i("OpenChat Error ","Activity doesn't exist");
            return;
        }

        Intent intent = new Intent(currentActivity, ConversationActivity.class);
        currentActivity.startActivity(intent);
    }

    @ReactMethod
    public void openChatWithUser( String userId ) {
        Activity currentActivity = getCurrentActivity();

        if (currentActivity == null) {
            Log.i("open ChatWithUser  ","Activity doesn't exist");
            return;
        }

        Intent intent = new Intent(currentActivity, ConversationActivity.class);

        if (userId != null ) {

            intent.putExtra(ConversationUIService.USER_ID, userId);
            intent.putExtra(ConversationUIService.TAKE_ORDER, true);

        }
        currentActivity.startActivity(intent);
    }
    @ReactMethod
    public void openChatWithUser( ReadableMap config, final Callback callback ) {

        Activity currentActivity = getCurrentActivity();
        Intent intent = new Intent(currentActivity, ConversationActivity.class);

        if (config != null && config.hasKey("clientGroupId")) {
            ChannelService channelService = ChannelService.getInstance(currentActivity);
            Channel channel = channelService.getChannelByClientGroupId(config.getString("clientGroupId"));

            if(channel==null){
                callback.invoke("Channel dose not exist", null);
                return;
            }
            intent.putExtra(ConversationUIService.GROUP_ID, channel.getKey());
            intent.putExtra(ConversationUIService.TAKE_ORDER, true);
            currentActivity.startActivity(intent);
            callback.invoke(null,"success");

        } else if(config != null && config.hasKey("channelKey")){
            Integer groupId = config.getInt("channelKey");
            intent.putExtra(ConversationUIService.GROUP_ID, groupId);
            intent.putExtra(ConversationUIService.TAKE_ORDER, true);
            currentActivity.startActivity(intent);
            callback.invoke(null,"success");

        } else {
            callback.invoke("unable to launch group chat, check your groupId/ClientGroupId","success");

        }

    }

    @ReactMethod
    public void logoutUser(ReadableMap config, final Callback callback) {

        Activity currentActivity = getCurrentActivity();

        if (currentActivity == null) {
            callback.invoke("Activity doesn't exist");
            return;
        }

        new UserClientService(currentActivity).logout();
        callback.invoke("success");
    }

    @ReactMethod
    public void getUnreadCountForUser(String userId, final Callback callback) {

        Activity currentActivity = getCurrentActivity();

        if (currentActivity == null) {
            callback.invoke("Activity doesn't exist",null);
            return;
         }
            int contactUnreadCount = new MessageDatabaseService(getCurrentActivity()).getUnreadMessageCountForContact(userId);
            callback.invoke(null,contactUnreadCount);

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

            if(channel==null){
                callback.invoke("Channel dose not exist", null);
                return;
            }

            int channelUnreadCount = new MessageDatabaseService(currentActivity).getUnreadMessageCountForChannel(channel.getKey());
            callback.invoke(null,channelUnreadCount);

        } else if(config != null && config.hasKey("channelKey")){

            int channelUnreadCount = new MessageDatabaseService(currentActivity).getUnreadMessageCountForChannel((Integer.parseInt(config.getString("channelKey"))));
            callback.invoke(null,channelUnreadCount);

        }
    }

    @ReactMethod
    public void totalUnreadCount(final Callback callback ) {
        Activity currentActivity = getCurrentActivity();

        if (currentActivity == null) {
            callback.invoke("Activity doesn't exist",null);
            return;
        }

        int totalUnreadCount = new MessageDatabaseService(currentActivity).getTotalUnreadCount();
        callback.invoke(null, totalUnreadCount);

    }

    @ReactMethod
    public void isUserLogIn( final Callback successCallback) {

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

}