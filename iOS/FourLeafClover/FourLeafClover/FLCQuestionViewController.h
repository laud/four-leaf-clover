//
//  FLCQuestionViewController.h
//  FourLeafClover
//
//  Created by Daniel Lau on 7/11/12.
//  Copyright (c) 2012 BazaarVoice. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "EGORefreshTableHeaderView.h"

@class FLCQuestionService;

@interface FLCQuestionViewController : UITableViewController <EGORefreshTableHeaderDelegate, UITableViewDelegate, UITableViewDataSource>
{
    EGORefreshTableHeaderView *_refreshHeaderView;
    
    BOOL _reloading;
}


@property (nonatomic, strong) FLCQuestionService *questionService;

@end
