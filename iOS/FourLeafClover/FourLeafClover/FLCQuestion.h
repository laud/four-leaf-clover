//
//  FLCQuestion.h
//  FourLeafClover
//
//  Created by Daniel Lau on 7/11/12.
//  Copyright (c) 2012 BazaarVoice. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface FLCQuestion : NSObject

@property (nonatomic, readonly) NSString *questionId;
@property (nonatomic, readonly) NSString *question;
@property (nonatomic, readonly) NSString *asker;

- (id)initWithAttributes:(NSDictionary *)attributes;
- (id)initWithId:(NSString *)questionId question:(NSString *)question asker:(NSString *)asker;

@end
