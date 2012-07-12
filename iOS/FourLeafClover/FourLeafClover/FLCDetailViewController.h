//
//  FLCDetailViewController.h
//  FourLeafClover
//
//  Created by Daniel Lau on 7/11/12.
//  Copyright (c) 2012 BazaarVoice. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ImgurUploader.h"
#import "MBProgressHUD.h"

@class FLCQuestion;

@interface FLCDetailViewController : UIViewController<UINavigationControllerDelegate, UIActionSheetDelegate, UIImagePickerControllerDelegate, UITextViewDelegate, ImgurUploaderDelegate, MBProgressHUDDelegate>
{
    ImgurUploader *uploader;
    MBProgressHUD *HUD;
}

@property (nonatomic, strong) FLCQuestion *question;
@property (nonatomic, strong) UIImagePickerController *pickerController;

@property IBOutlet UILabel *questionLabel;
@property IBOutlet UISegmentedControl *answerControl;
@property IBOutlet UIButton *submitButton;
@property IBOutlet UITextView *answerField;

- (IBAction)answerControlChanged:(id)sender;
- (IBAction)submitButtonPressed:(id)sender;
- (void)popHome;



@end
