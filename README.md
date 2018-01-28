# Applozic-React-Native-Chat-Messaging-SDK


Applozic powers real time messaging across any device, any platform & anywhere in the world. Integrate our simple SDK to engage your users with image, file, location sharing and audio/video conversations.

Signup at https://www.applozic.com/signup.html to get the application key.

## Instalation

#### iOS CocoaPods

Setup your Podfile located at /ios/Podfile and add below pod dependency.

```
  pod 'Applozic', '~>5.0.0'
```
If you have not yet using pod dependency, check out how you can add pod in your react native project here.

#### Add bridge files to your project
Please follow below steps to add react native bridge files to your porject.

 1) copy applozic folder from here to /ios/ folder to your project. 

 2) Open project from /ios/ folder in xcode.
 
 **NOTE :** Please make sure to use .xcworkspace, not .xcproject after that.

 3) Add all .h and .m files to your project from applozic folder in step 1)
 
 ### Push Notification
 
   Open AppDelegate.m file under /ios/YOUR_PROJECT/
   
   Add code as mentioned in the following documentation:
   https://www.applozic.com/docs/ios-chat-sdk.html#step-4-push-notification-setup
 
 
 ## Integration 
 
 Define native module.
 
 ```
 var ApplozicChat = NativeModules.ApplozicChat;
```

 ### User Authentication
 
 ```
 ApplozicChat.login({
                    'userId': UNIQUE_ID, //Please Note: +,*,/ is not allowed in userIds
                    'email': EMAIL,
                    'contactNumber': PHONE NO,
                    'password': PASSWORD,
                    'displayName': DISPLAY NAME OF USER
                }, (error, response) => {
                  if(error){
                      console.log(error)
                  }else{
                    this.setState({loggedIn: true, title: 'Loading...'});
                    //this.createGroup();
                    this.addMemberToGroup();
                    console.log(response);
                  }
                })
 
 ```
 ### Chat List 
 
 ```
 ApplozicChat.openChat();
 ```
 
 ### One to one Chat 
 
 ```
 ApplozicChat.openChatWithUser(userId);
 ```
 
 ### Group Chat 
 
 1. With Applozic generated GroupId
 ```
 ApplozicChat.openChatWithGroup(groupId , (error,response) =>{
                if(error){
                  //Group launch error
                  console.log(error);
                }else{
                  //group launch successfull
                  console.log(response)
                }
              });
 ```
 **NOTE**: grpupId must be Numeric
 
 2. With your assigned groupId (Client group Id)
 
```
ApplozicChat.openChatWithClientGroupId(clientGroupId, (error,response) =>{
            if(error){
              //Group launch error
              console.log(error);
            }else{
              //group launch successfull
              console.log(response)
            }
          });
```
 **NOTE**: grpupId must be String
 
 
 ### Create Group 
 
 ```
 var groupDetails = {
                'groupName':'React Test2',
                'clientGroupId':'recatNativeCGI',
                'groupMemberList': ['ak101', 'ak102', 'ak103'], // Pass list of user Ids in groupMemberList
                'imageUrl': 'https://www.applozic.com/favicon.ico',
                'type' : 2,    //'type' : 1, //(required) 1:private, 2:public, 5:broadcast,7:GroupofTwo
                'metadata' : {
                    'key1' : 'value1',
                    'key2' : 'value2'
                }
            };
            ApplozicChat.createGroup(groupDetails, (error, response) => {
                if(error){
                    console.log(error)
                }else{
                  console.log(response);
                }
              });
 ```
 ### Add member to group 
 
 
 ```
   var requestData = {
              'clientGroupId':'recatNativeCGI',
              'userId': 'ak111', // Pass list of user Ids in groupMemberList
          };

          ApplozicChat.addMemberToGroup(requestData, (error, response) => {
               if(error){
                   console.log(error)
               }else{
                 console.log(response);
               }
             });
 ```
 ### Remove member from group 
 
 ```
 var requestData = {
             'clientGroupId':'recatNativeCGI',
             'userId': 'ak104', // Pass list of user Ids in groupMemberList
         };

          ApplozicChat.removeUserFromGroup(requestData, (error, response) => {
              if(error){
                  console.log(error)
              }else{
                console.log(response);
              }
            });
 ```
 
 ### Logout
 
 ```
   ApplozicChat.logoutUser((error, response) => {
              if(error){
                console.log("error" + error);
              }else{
                console.log(response);
              }

            });
 ```
