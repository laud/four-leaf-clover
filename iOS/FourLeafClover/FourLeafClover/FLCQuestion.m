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

- (id)initWithAttributes:(NSDictionary *)attributes {
    self = [super init];
    if (!self) {
        return nil;
    }
    
    _questionId = [attributes valueForKey:@"_id"];
    _question = [attributes valueForKeyPath:@"_source.text"];
    
    return self;
}


@end
