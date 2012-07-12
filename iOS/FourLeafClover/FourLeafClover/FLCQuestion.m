//
//  FLCQuestion.m
//  FourLeafClover
//
//  Created by Daniel Lau on 7/11/12.
//  Copyright (c) 2012 BazaarVoice. All rights reserved.
//

#import "FLCQuestion.h"

@implementation FLCQuestion
{
    @private
    NSString *_questionId;
    NSString *_question;
    NSString *_asker;
}

@synthesize questionId = _questionId;
@synthesize question = _question;
@synthesize asker = _asker;

- (id)initWithAttributes:(NSDictionary *)attributes {
    self = [super init];
    if (!self) {
        return nil;
    }
    
    _questionId = [attributes valueForKey:@"question_id"];
    _question = [attributes valueForKey:@"question"];
    _asker = [attributes valueForKey:@"asker"];
    
    return self;
}

- (id)initWithId:(NSString *)questionId question:(NSString *)question asker:(NSString *)asker
{
    self = [super init];
    if (!self) {
        return nil;
    }
    
    _questionId = questionId;
    _question = question;
    _asker = asker;
    
    return self;
}

@end
