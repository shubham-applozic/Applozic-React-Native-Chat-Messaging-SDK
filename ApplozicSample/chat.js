//'use strict';
import React, {Component} from 'react';
import { DeviceEventEmitter } from 'react-native';
//import FCM from 'react-native-fcm';

import {
    AppRegistry,
    StyleSheet,
    Text,
    View,
    Button,
    TextInput,
    Modal,
    ScrollView,
    FlatList,
    NativeModules
} from 'react-native';

var ApplozicChat = NativeModules.ApplozicChat;

export default class AwesomeProject extends React.Component {

    state = {
        messages: []
    }

    constructor(props) {
        super(props);
        this.state = {
            userId: '',
            email: '',
            phoneNumer: '',
            pass_word: '',
            displayName: '',
            loggedIn: false,
            visible: false,
            title: 'Login/SignUp',
            mytoken: '',
            messages : []
        };
        this.isUserLogIn = this.isUserLogIn.bind(this);
        this.chatLogin = this.chatLogin.bind(this);
        this.logoutUser = this.logoutUser.bind(this);
        this.show = this.show.bind(this);
        this.openChat = this.openChat.bind(this);
        this.createGroup = this.createGroup.bind(this);
        this.addMemberToGroup = this.addMemberToGroup.bind(this);
        this.openChatWithUser = this.openChatWithUser.bind(this);
        this.getConversationList = this.getConversationList.bind(this);
        this.addEvent = this.addEvent.bind(this);

        this.getUnreadCountForUser = this.getUnreadCountForUser.bind(this);
        this.getUnreadCountForChannel = this.getUnreadCountForChannel.bind(this);
        this.totalUnreadCount = this.totalUnreadCount.bind(this);
        this.isUserLogIn = this.isUserLogIn.bind(this);
        this.sendMessage = this.sendMessage.bind(this);
        this.downloadMessage = this.downloadMessage.bind(this);
        this.registerListener = this.registerListener.bind(this);
        this.unregisterListener = this.unregisterListener.bind(this);
        this.getMessageForContact = this.getMessageForContact.bind(this);
        this.getMessageForChannel = this.getMessageForChannel.bind(this);
    }

     componentDidMount() {
       this.isUserLogIn();
       ApplozicChat.getLatestMessageList({'isScroll' : 'false'}, (error, response)=>{
           if(error){

           }else{
               const messages = JSON.parse(response);

            //    for(var i = 0; i< messagesL.length; i++){
            //        console.log("Reytum message : " + messagesL[i]);
            //    }

               this.setState({messages});
           }
       });

       console.log('Reytum component has mounted');
       this.registerListener();

       let messageSent = DeviceEventEmitter.addListener('Applozic-onMessageSent', (response) => {
        console.log('Reytum rec event message sent : ' + JSON.stringify(response));
    });

    let messageRec = DeviceEventEmitter.addListener('Applozic-onMessageReceived', (response) => {
        console.log('Reytum rec event message received : ' + JSON.stringify(response));
    });
        // FCM.getFCMToken().then(token => {
        //     this.setState({mytoken: token});
        //     console.log(token)
        // });
        //
        // this.refreshUnsubscribe = FCM.on('refreshToken', (token) => {
        //     this.refreshToken()
        //
        // });

      //  this.isUserLogIn();

    }

    componentWillUnMount()
    {
        console.log('Reytum component has un-mounted');
        this.unregisterListener();
        this.refreshUnsubscribe();
        messageSent.remove();
        messageRec.remove();

    }
    openModal() {
        this.setState({modalVisible:true});
      }

    closeModal() {
        this.setState({modalVisible:false});
    }
    openOneToOneChat(){
     alert("");

    }

    show() {
        this.setState({title: 'Loading....!'});
        this.chatLogin();
    }

    render() {
        let display = this.state.loggedIn;
        if (display) {
    //       return (
    //         <View style = {styles.container}>
    //   <Text style = { styles.titleText} >
    //       Applozic </Text>
    //         <Text style = {styles.baseText}>
    //              Demo App </Text>
    //         <Text style = {styles.btn} onPress = {this.openChat}>
    //               Open Chat List </Text>
    //   <Text style = {styles.btn} onPress = {this.sendMessage}>
    //                   Send attachment </Text>
    //   <Text style = {styles.btn} onPress = {this.downloadMessage}>
    //                       Download Attachment </Text>
    //   <Text style = {styles.btn} onPress = {this.getMessageForContact}>
    //                                  Contact Message List </Text>
    //   <Text style = {styles.btn} onPress = {this.getMessageForChannel}>
    //                           Channel Message List </Text>
    //   <Text style = {styles.btn} onPress = {this.addMemberToGroup}>
    //                                       Add Member to group </Text>
    //   <Text style = {styles.btn} onPress = {this.removeUserFromGroup}>
    //                             remove member to group </Text>
    //   <Text style = {styles.btn} onPress = {this.getConversationList}>
    //                             Get Latest Messages </Text>
    //   <Text style = {styles.btn} onPress = {this.getNextConversationList}>
    //                             Get Next Latest Messages </Text>
    //   <Text style = {styles.btn} onPress = {this.logoutUser}>
    //               LogOut </Text>
    //        </View >
    //      );
    return (
        <View style={styles.container} >
          <Text style={styles.h2text}>
            Black Order
          </Text>
            <FlatList
            data={this.state.messages}
            showsVerticalScrollIndicator={false}
            renderItem={({item}) => {
                var channel = null;
                var contact = null;
                var imageUrl = null;
            if(item.hasOwnProperty('groupId')){
               ApplozicChat.getChannelFromChannelKey(item.groupId, (args1, args2)=>{
                   channel = JSON.parse(args2).channel;
                   contact = null;
                   imageUrl = channel.imageUrl;
               });
            }else{
                 ApplozicChat.getContactById(item.to, (arg1, arg2)=>{
                     contact = JSON.parse(arg2);
                     imageUrl = contact.imageUrl;
                 });
            }
            <View style={styles1.flatview}>
             <Image
                style={{width: 50, height: 50}}
                source={{uri: imageUrl}}/>
              <Text style={styles1.name}>{item.to}</Text>
              <Text style={styles1.email}>{item.message}</Text>
            </View>
            }
        }
            keyExtractor={item => item.key}
          />
        </View>
      );
       }

      return (
        <View style ={styles.container}>
        <ScrollView>
          <Text style = {styles.titleText}>
             Applozic </Text>
                <Text style = {styles.baseText}>
             Demo App </Text>
          <TextInput style ={styles.inputText}
             keyboardType = "default"
             placeholder = "UserId"
             maxLength = {25}
             underlineColorAndroid = 'transparent'
             value = {this.state.userId}
             onChangeText={userId => this.setState({userId})}/>
          <TextInput type = "email-address"
             style = {styles.inputText}
             placeholder = "Email"
             keyboardType = "email-address"
             maxLength = {30}
             underlineColorAndroid = 'transparent'
             value = { this.state.email}
             onChangeText = {email => this.setState({email})}/>
          <TextInput style = { styles.inputText}
             placeholder = "Phone Number"
             keyboardType = "phone-pad"
             underlineColorAndroid = 'transparent'
             maxLength = {10}
             value = {this.state.phoneNumber}
             onChangeText = {phoneNumber => this.setState({phoneNumber})}/>
          <TextInput id = "password"
             type = "password"
             style = {styles.inputText}
             maxLength = {25}
             placeholder = "Password"
             keyboardType = "default"
             underlineColorAndroid = 'transparent'
             value = {this.state.pass_word}
             secureTextEntry = {true}
             password = "true"
             onChangeText = {pass_word => this.setState({pass_word})}/>
          <TextInput id = "displayName"
             style = {styles.inputText}
             placeholder = "Display Name"
             keyboardType = "default"
             underlineColorAndroid = 'transparent'
             value = {this.state.displayName}
             maxLength = {25}
             onChangeText = {displayName => this.setState({displayName})}/>
          <Button title = {this.state.title}
             onPress = {this.show}
             color = "#20B2AA"/>
          </ScrollView>
          </View>
    );
    }
    //======================== Applozic fucntions ==========================================================

        //Login chat to the users..
        chatLogin() {

            if (this.state.userId.length > 0 && this.state.pass_word.length > 0) {
              ApplozicChat.login({
                    'userId': this.state.userId,
                    'email': this.state.email,
                    'contactNumber': this.state.phoneNumber,
                    'password': this.state.pass_word,
                    'displayName': this.state.displayName
                }, (error, response) => {
                  if(error){
                      console.log("error " + error);
                  }else{
                    this.setState({loggedIn: true, title: 'Loading...'});
                    this.createGroup();
                    console.log("response::" + response);
                  }
                })
            } else {
                this.setState({title: 'Login/SignUp'});
                alert("Please Enter UserId & Password");
             };
        }

        openChat(){
          ApplozicChat.openChat();
        }
        //Launch Chat with clientGroupID : '6543274'
        openChatWithUser(){
          ApplozicChat.openChatWithUser('ak101');
        }

        //Launch Chat with clientGroupID : '6543274'
        openChatWithGroupId(groupId){
              ApplozicChat.openChatWithGroup(groupId , (error,response) =>{
                if(error){
                  //Group launch error
                  console.log(error);
                }else{
                  //group launch successfull
                  console.log(response)
                }
              });
        }

        //Launch Chat with clientGroupID
        openChatWithClientGroupId(clientGroupID){

          ApplozicChat.openChatWithClientGroupId(clientGroupID, (error,response) =>{
            if(error){
              //Group launch error
              console.log(error);
            }else{
              //group launch successfull
              console.log(response)
            }
          });

        }

      logoutUser() {
            ApplozicChat.logoutUser((error, response) => {
              if(error){
                console.log("error :#" + error);
              }else{
                this.setState({
                    userId: '',
                    email: '',
                    phoneNumber: '',
                    pass_word: '',
                    displayName: '',
                    loggedIn: false,
                    title: 'Login/SignUp'
                });
              }

            });
        }

        getUnreadCountForUser() {
            ApplozicChat.getUnreadCountForUser( 'ak102', (error, count) => {
              console.log("count for userId:" + count);
            });
        }

        getUnreadCountForChannel() {

          var requestData = {
                'groupId':7107309, //replace with your groupId
                'clientGroupId': '' //
                  // pass either channelKey or clientGroupId
            };

          ApplozicChat.getUnreadCountForChannel(requestData, (error, count) => {
            if(error){
              console.log("error ::" + error);
            }else{
              console.log("count for requestData ::" + count);
            }
          });

        }

        totalUnreadCount() {
            ApplozicChat.totalUnreadCount((error, totalUnreadCount) => {
              console.log("totalUnreadCount for logged-in user:" + totalUnreadCount);

            });

        }

        isUserLogIn() {
            ApplozicChat.isUserLogIn((response) => {
                this.setState({loggedIn: response});
            })
        }

       createGroup(){

          var groupDetails = {
                'groupName':'React Test3',
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
      }

      addMemberToGroup(){

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
     }

     removeUserFromGroup(){

       var requestData = {
             'clientGroupId':'recatNativeCGI',
             'userId': 'ak111', // Pass list of user Ids in groupMemberList
         };

          ApplozicChat.removeUserFromGroup(requestData, (error, response) => {
              if(error){
                  console.log(error)
              }else{
                console.log(response);
              }
            });
    }

    getConversationList(){
        var vary = {
          'isScroll' : false
        };
        ApplozicChat.getLatestMessageList(vary, (error, response) => {
            if(error){
                console.log("Reytum error : " + JSON.stringify(error));
            }else{
                var messageList = JSON.parse(response);
                for(var i = 0; i< messageList.length; i++){
                    var name;
                    var message;
                    if(messageList[i].hasOwnProperty('groupId')){
                        name = messageList[i].groupId;
                    }else{
                        name = messageList[i].to;
                    }
                    if(messageList[i].message.length){
                        message = messageList[i].message;
                    }else{
                        message = 'Some other type of message';
                    }
                    console.log("ListTest : "+name + " : " + message);
                    console.log("ListTest : ------------------------------------------------------------------------------------------");
                }
            }
        });
    }

    getList(isScroll){
        var vary = {
            'isScroll' : isScroll
        };

      ApplozicChat.getLatestMessageList(vary, (error, response) => {
         if(error){

         }else{
             return JSON.parse(response);
         }
      });
    }

    getNextConversationList(){
        var vary = {
          'isScroll' : true
        };
        ApplozicChat.getLatestMessageList(vary, (error, response) => {
            if(error){
                console.log("Reytum error : " + JSON.stringify(error));
            }else{
                var messageList = JSON.parse(response);
                for(var i = 0; i< messageList.length; i++){
                    var name;
                    var message;
                    if(messageList[i].hasOwnProperty('groupId')){
                        name = messageList[i].groupId;
                    }else{
                        name = messageList[i].to;
                    }
                    if(messageList[i].message.length){
                        message = messageList[i].message;
                    }else{
                        message = 'Some other type of message';
                    }
                    console.log("NextListTest : "+name + " : " + message);
                    console.log("NextListTest : ------------------------------------------------------------------------------------------");
                }
            }
        });
    }

    addEvent(){
        ApplozicChat.testListner(() => {
            ApplozicChat.addListener('progress',(data)=>{
                console.log("Reytum received : " + JSON.stringify(data));
            });
        });
    }

    sendMessage(){

        let messageTemp = {
            'message' : 'From React',
            'to' : 'reytum7',
            'contentType' : 1,
            'filePaths' : ['/storage/emulated/0/Applozic/image/JPEG_20180620_124533_.jpeg']
        };

        ApplozicChat.prepareMessage(JSON.stringify(messageTemp), (response)=>{

            console.log("Reytum received response : " + response);

            let started = DeviceEventEmitter.addListener('Applozic-UploadStarted',(data)=>{
                console.log('Reytum upload started for ' + JSON.parse(response).createdAtTime + ", " + data.createdAtTime);
             });
    
             let progress = DeviceEventEmitter.addListener('Applozic-UploadProgress',(data)=>{
                 console.log('Reytum upload progress for ' + JSON.parse(response).createdAtTime + ", " + data.createdAtTime + " : " + data.uploadProgress);
             });
    
             let cancelled = DeviceEventEmitter.addListener('Applozic-UploadCancelled',(data)=>{
                 console.log('Reytum upload cancelled for ' + JSON.parse(response).createdAtTime + ", " + data.createdAtTime);
             });
    
             let completed = DeviceEventEmitter.addListener('Applozic-UploadCompleted',(data)=>{
                 console.log('Reytum upload completed for ' + JSON.parse(response).createdAtTime + ", " + data.createdAtTime);
             });

            ApplozicChat.sendMessage(response, (arg1, arg2) => {
                if(arg1){
                    started.remove();
                    progress.remove();
                    cancelled.remove();
                    completed.remove();
                    console.log('Reytum received callback ' + response.createdAtTime + ", " + arg2.createdAtTime + " : " + arg1 + ",  " + arg2);
                }
            });
        });
       //let response = 1;
    }

    downloadMessage(){
        DeviceEventEmitter.addListener('Applozic-DownloadStarted',(data)=>{
            console.log('Reytum download started : ' + JSON.stringify(data));
         });

         DeviceEventEmitter.addListener('Applozic-DownloadProgress',(data)=>{
             console.log('Reytum download progress: ' + JSON.stringify(data));
         });

        ApplozicChat.downloadMessage('4-5319d80e-a269-4582-b86b-63f81d2145b0-1528734228860', (status, response) => {
             if(status === 'Success'){
                 var downloadedMessage = JSON.parse(response);
             }
        });
    }

    registerListener(){
        ApplozicChat.registerListener((response) => {
           console.log("Reytum register UI response : " + JSON.stringify(response));
        });
    }

    unregisterListener(){
        ApplozicChat.unregisterListener((response) => {
            console.log("Reytum un-register UI response : " + JSON.stringify(response));
        });
    }

    getMessageForChannel(){
        var t = {
            'groupId' : 8802051
        };
  
        ApplozicChat.getMessageListForChannel(t, (error, response) => {
          if(error){
              console.log("Reytum error : " + JSON.stringify(error));
          }else{
              var messageList = JSON.parse(response);
              for(var i = 0; i< messageList.length; i++){
                  var name;
                  var message;
                  if(messageList[i].hasOwnProperty('groupId')){
                      name = messageList[i].groupId;
                  }else{
                      name = messageList[i].to;
                  }
                  if(messageList[i].message.length){
                      message = messageList[i].message;
                  }else{
                      message = 'Some other type of message';
                  }
                  console.log("NextListTest : "+name + " : " + message);
                  console.log("NextListTest : ------------------------------------------------------------------------------------------");
              }
          }
        });
    }

    getMessageForContact(){
        var t = {
            'userId' : 'reytum7'
        };
  
        ApplozicChat.getMessageListForContact(t, (error, response) => {
          if(error){
              console.log("Reytum error : " + JSON.stringify(error));
          }else{
              var messageList = JSON.parse(response);
              for(var i = 0; i< messageList.length; i++){
                  var name;
                  var message;
                  if(messageList[i].hasOwnProperty('groupId')){
                      name = messageList[i].groupId;
                  }else{
                      name = messageList[i].to;
                  }
                  if(messageList[i].message.length){
                      message = messageList[i].message;
                  }else{
                      message = 'Some other type of message';
                  }
                  console.log("NextListTest : "+name + " : " + message);
                  console.log("NextListTest : ------------------------------------------------------------------------------------------");
              }
          }
        });
    }

    //======================== Applozic fucntions END===================================================

}
const styles = StyleSheet.create({
    container: {
        flex: 1,
        alignItems: 'center',
        backgroundColor: '#4D394B'
    },
    btn: {
        fontSize: 23,
        fontWeight: 'bold',
        color: 'yellow',
        marginTop: 20,
        alignSelf: 'center'
    },
    baseText: {
        fontFamily: 'Cochin',
        color: '#fff',
        marginBottom: 25,
        alignSelf: 'center'
    },
    titleText: {
        fontSize: 25,
        fontWeight: 'bold',
        color: '#fff',
        marginTop: 15,
        alignSelf: 'center'
    },
    inputText: {
        width: 330,
        height: 40,
        backgroundColor: '#fff',
        marginBottom: 6,
        padding: 10,
        fontSize: 20,
        marginLeft: 10,
        marginRight: 10
    }
});

const styles1 = StyleSheet.create({
    container: {
      flex: 1,
      marginTop: 50,
      justifyContent: 'center',
      alignItems: 'center',
      backgroundColor: '#F5FCFF',
    },
    h2text: {
      marginTop: 10,
      fontFamily: 'Helvetica',
      fontSize: 36,
      fontWeight: 'bold',
    },
    flatview: {
      justifyContent: 'center',
      paddingTop: 30,
      borderRadius: 2,
    },
    name: {
      fontFamily: 'Verdana',
      fontSize: 18
    },
    email: {
      color: 'red'
    }
    
  });
