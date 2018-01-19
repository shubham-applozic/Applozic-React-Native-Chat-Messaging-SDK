//
//  ApplozicChatManger.m
//  ApplozicSample
//
//  Created by Adarsh on 17/01/18.
//  Copyright © 2018 Facebook. All rights reserved.
//

#import "ApplozicChat.h"
#import "ALChatManager.h"
#import <Applozic/ALUser.h>
#import <Applozic/ALPushAssist.h>

@implementation ApplozicChat

// To export a module named CalendarManager
RCT_EXPORT_MODULE();


/**
 * Login method of the user...
 *
 */

RCT_EXPORT_METHOD(login:(NSDictionary *)userDetails  andCallback:(RCTResponseSenderBlock)callback )
{
  
  ALUser * aluser =  [[ALUser alloc] initWithJSONString:[self getJsonString:userDetails]];
  
  ALChatManager * chatManger = [[ALChatManager alloc] init];
  
  [chatManger registerUserWithCompletion:aluser withHandler:^(ALRegistrationResponse *rResponse, NSError *error) {
    
    if(error){
      
      NSString* errorResponse = error.description;
      if(rResponse){
        
        errorResponse = [self getJsonString:[rResponse dictionary]];
      }
      return callback(@[errorResponse, [NSNull null]]);

    }else if ( rResponse.isRegisteredSuccessfully ){
      
      return callback(@[[NSNull null],[self getJsonString:[rResponse dictionary]]]);
      
    }
  }];
  
  NSLog(@"Pretending to create an event  at ");
  
}
/**
 * Open chats
 *
 **/
RCT_EXPORT_METHOD(openChat)
{
  
  ALChatManager * chatManger = [[ALChatManager alloc] init];
  ALPushAssist* pushAssistant = [[ALPushAssist alloc] init];
  dispatch_async(dispatch_get_main_queue(), ^{
    [chatManger launchChat:pushAssistant.topViewController];
    
  });

}
/**
 * Open chat with Users
 *
 **/
RCT_EXPORT_METHOD(openChatWithUser:(NSString*)userId)
{
  
  ALChatManager * chatManger = [[ALChatManager alloc] init];
  ALChatLauncher * chatLauncher = [[ALChatLauncher alloc] initWithApplicationId:chatManger.getApplicationKey];
  ALPushAssist* pushAssistant = [[ALPushAssist alloc] init];

  dispatch_async(dispatch_get_main_queue(), ^{
    
    [chatLauncher launchIndividualChat:userId withGroupId:nil andViewControllerObject:pushAssistant.topViewController andWithText:nil ];
    
  });
  
}
/**
 * Open chat with Group
 *
 **/
RCT_EXPORT_METHOD(openChatWithGroup:(NSNumber*)groupId orClientGroupId:(NSString*) clientGroupId)
{
  
  ALChatManager * chatManger = [[ALChatManager alloc] init];
  ALChatLauncher * chatLauncher = [[ALChatLauncher alloc] initWithApplicationId:chatManger.getApplicationKey];
  ALPushAssist* pushAssistant = [[ALPushAssist alloc] init];
  dispatch_async(dispatch_get_main_queue(), ^{
    
    [chatLauncher launchIndividualChat:nil withGroupId:groupId andViewControllerObject:pushAssistant.topViewController andWithText:nil ];
    
  });
}

/**
 *  Logout users
 *
 */


RCT_EXPORT_METHOD(logoutUser:(RCTResponseSenderBlock)callback )
{
  ALRegisterUserClientService * alRegisterUserClientService = [[ALRegisterUserClientService alloc]init];
  
  [alRegisterUserClientService logoutWithCompletionHandler:^(ALAPIResponse *response, NSError *error) {
    if(error){
      
      NSString* errorResponse = error.description;
      return callback(@[errorResponse, [NSNull null]]);
      
    }else if (response ){
      
      return callback(@[[NSNull null],[self getJsonString:[response dictionary]]]);
      
    }
  }];
  
}


-(NSString *)getJsonString:(id) Object{

  NSError *error;
  NSString *jsonString;
  NSData *jsonData = [NSJSONSerialization dataWithJSONObject:Object
                                                     options:NSJSONWritingPrettyPrinted // Pass 0 if you don't care about the readability of the generated string
                                                       error:&error];
  
  if (! jsonData) {
    
    NSLog(@"Got an error: %@", error);
    
  } else {
    
    jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
  }
  return jsonString;
}
                        

@end
