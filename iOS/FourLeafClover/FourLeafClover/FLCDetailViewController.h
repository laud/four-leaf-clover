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
#import <SpeechKit/SpeechKit.h>

@class FLCQuestion;

@interface FLCDetailViewController : UIViewController<UINavigationControllerDelegate, UIActionSheetDelegate, UIImagePickerControllerDelegate, UITextViewDelegate, SpeechKitDelegate, SKRecognizerDelegate, ImgurUploaderDelegate, MBProgressHUDDelegate>
{
    ImgurUploader *uploader;
    MBProgressHUD *HUD;
    
    SKRecognizer* voiceSearch;
    enum {
        TS_IDLE,
        TS_INITIAL,
        TS_RECORDING,
        TS_PROCESSING,
    } transactionState;
}

@property (nonatomic, strong) FLCQuestion *question;
@property (nonatomic, strong) UIImagePickerController *pickerController;
@property (readonly) SKRecognizer* voiceSearch;


@property IBOutlet UILabel *questionLabel;
@property IBOutlet UISegmentedControl *answerControl;
@property IBOutlet UITextView *answerField;

- (IBAction)answerControlChanged:(id)sender;
- (IBAction)submitButtonPressed:(id)sender;
- (void)popHome;



@end
