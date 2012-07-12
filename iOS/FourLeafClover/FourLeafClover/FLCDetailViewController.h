//
//  FLCDetailViewController.h
//  FourLeafClover
//
//  Created by Daniel Lau on 7/11/12.
//  Copyright (c) 2012 BazaarVoice. All rights reserved.
//

#import <UIKit/UIKit.h>

@class FLCQuestion;

@interface FLCDetailViewController : UIViewController

@property (nonatomic, strong) FLCQuestion *question;

@property IBOutlet UILabel *questionLabel;
@property IBOutlet UILabel *askerLabel;
@property IBOutlet UISegmentedControl *answerControl;
@property IBOutlet UIButton *submitButton;
@property IBOutlet UITextView *answerField;

- (IBAction)answerControlChanged:(id)sender;
- (IBAction)submitButtonPressed:(id)sender;


@end
