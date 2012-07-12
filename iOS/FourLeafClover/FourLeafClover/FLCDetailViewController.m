//
//  FLCDetailViewController.m
//  FourLeafClover
//
//  Created by Daniel Lau on 7/11/12.
//  Copyright (c) 2012 BazaarVoice. All rights reserved.
//

#import "FLCDetailViewController.h"
#import "FLCQuestion.h"
#import "AFNetworking.h"

#define CONTROL_CAMERA 0
#define CONTROL_WRITE 1
#define CONTROL_SPEAK 2

#define BUTTON_CAMERA 0
#define BUTTON_CAMERA_ROLL 1

@interface FLCDetailViewController ()

- (void)resetSelection;
- (void)submitAnswerWithQuestion:(FLCQuestion *)theQuestion withAnswerString:(NSString *)theAnswer;
@end

const unsigned char SpeechKitApplicationKey[] = {0xdd, 0x32, 0xe8, 0x98, 0xde, 0xdf, 0xbe, 0xc2, 0x6c, 0x09, 0xa0, 0xa6, 0x11, 0x05, 0x30, 0x5a, 0x67, 0xf2, 0xe1, 0x8b, 0x10, 0xba, 0x81, 0xce, 0x56, 0x15, 0xec, 0x63, 0x4b, 0xd0, 0x32, 0x28, 0x02, 0x1a, 0xc0, 0xe0, 0xfa, 0x7f, 0xa2, 0x66, 0x88, 0x43, 0x0c, 0x99, 0x48, 0x3d, 0xf3, 0xaf, 0x2f, 0xd5, 0x3a, 0x3f, 0x8f, 0xbc, 0x0d, 0xe6, 0xd1, 0xf9, 0x49, 0x1c, 0xc9, 0x7c, 0x44, 0xcb};

@implementation FLCDetailViewController

@synthesize question;

@synthesize voiceSearch;
@synthesize pickerController;
@synthesize questionLabel;
@synthesize answerControl;
@synthesize answerField;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}


- (NSString *)urlencode:(NSString *)unencodedString
{
    NSString * encodedString = (__bridge NSString *)CFURLCreateStringByAddingPercentEscapes(
                                                                                   NULL,
                                                                                   (CFStringRef)unencodedString,
                                                                                   NULL,
                                                                                   (CFStringRef)@"!*'();:@&=+$,/?%#[]",
                                                                                   kCFStringEncodingUTF8 );
    return encodedString;
}

- (void)popHome
{
    UINavigationController *navController = self.navigationController;
    [navController popViewControllerAnimated:YES];
}

- (void)submitAnswerWithQuestion:(FLCQuestion *)theQuestion withAnswerString:(NSString *)theAnswer
{
    HUD = [[MBProgressHUD alloc] initWithView:self.navigationController.view];
	[self.navigationController.view addSubview:HUD];
    HUD.delegate = self;
	HUD.mode = MBProgressHUDModeIndeterminate;
	HUD.labelText = @"Instant Karma!";
    [HUD show:YES];
    
    NSString *endPoint = @"heman:10090/answer/for/";
    NSString *escapedEndPoint = [self urlencode:endPoint];
    NSString *escapedAnswer = [self urlencode:theAnswer];
    NSString *location = @"San Francisco, CA";
    NSString *escapedLocation = [self urlencode:location];
    
    NSString *urlStringStub = @"http://uatservices.powerreviews.com/APOSupercharge.dox?endPoint=%@&questionId=%@&profileId=%@&text=%@&location=%@";
    
    NSString *urlString = [NSString stringWithFormat:urlStringStub, escapedEndPoint, theQuestion.questionId, @"123456mobile", escapedAnswer, escapedLocation];
    NSURL *url = [NSURL URLWithString:urlString];
    NSURLRequest *request = [NSURLRequest requestWithURL:url];
    
    AFJSONRequestOperation *operation = [AFJSONRequestOperation JSONRequestOperationWithRequest:request success:^(NSURLRequest *request, NSHTTPURLResponse *response, id JSON) {
        NSLog(@"Content post success!");
        NSLog(@"Status Code %u", [response statusCode]);
        HUD.customView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"37x-Checkmark.png"]];
        HUD.mode = MBProgressHUDModeCustomView;
        HUD.labelText = @"See you next time!";
        [HUD hide:YES afterDelay:2];
        [self performSelector:@selector(popHome) withObject:self afterDelay:2.1];
    } failure:nil];
    
    [operation start];
}


-(void)dismissKeyboard {
    [answerField resignFirstResponder];
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    self.pickerController = [[UIImagePickerController alloc] init];
    self.pickerController.delegate = self;
        
    uploader = [[ImgurUploader alloc] init];
	uploader.delegate = self;
    
	// Do any additional setup after loading the view.
    UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc]
                                   initWithTarget:self
                                   action:@selector(dismissKeyboard)];
    [self.view addGestureRecognizer:tap];

    questionLabel.text = question.question;
    
    [SpeechKit setupWithID:@"NMDPTRIAL_daniel_lau20120709170122"
                      host:@"sandbox.nmdp.nuancemobility.net"
                      port:443
                    useSSL:NO
                  delegate:self];
    
}

- (void)viewWillDisappear:(BOOL)animated
{
    [voiceSearch cancel];
    voiceSearch = nil;
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}

- (void)recordAudio
{
    transactionState = TS_INITIAL;

    voiceSearch = [[SKRecognizer alloc] initWithType:SKDictationRecognizerType
                                           detection:SKLongEndOfSpeechDetection
                                            language:@"en_US"
                                            delegate:self];
}

- (IBAction)answerControlChanged:(id)sender
{
    UISegmentedControl *segmentedControl = (UISegmentedControl *)sender;
    [self dismissKeyboard];
    NSUInteger index = [segmentedControl selectedSegmentIndex];
    
    if (index == CONTROL_CAMERA) {
        UIActionSheet *sheet = [[UIActionSheet alloc] initWithTitle:@"Select Picture Capture Type"
                                            delegate:self
                                   cancelButtonTitle:@"Cancel"
                              destructiveButtonTitle:nil
                                   otherButtonTitles:@"Take Photo", @"Select From Album", nil];
        
        // Show the sheet
        [sheet showInView:self.view];
    } else if (index == CONTROL_WRITE) {
        // DO NOTHING
        
    } else if (index == CONTROL_SPEAK) {
        // TODO: Fill this in with real code
        [self recordAudio];
    }
}

- (IBAction)submitButtonPressed:(id)sender
{
    [self dismissKeyboard];
    [self submitAnswerWithQuestion:question withAnswerString:answerField.text];
}

- (BOOL) textView:(UITextView *)textView shouldChangeTextInRange:(NSRange)range replacementText:(NSString *)text
{
    if([text isEqualToString:@"\n"]){
        [textView resignFirstResponder];
        [self submitAnswerWithQuestion:question withAnswerString:answerField.text];
        return NO;
    }else{
        return YES;
    }
}

- (void)resetSelection
{
    answerControl.selectedSegmentIndex = CONTROL_WRITE;
}

- (void)actionSheet:(UIActionSheet *)actionSheet didDismissWithButtonIndex:(NSInteger)buttonIndex
{
    if (buttonIndex == BUTTON_CAMERA) {
        self.pickerController.sourceType = UIImagePickerControllerSourceTypeCamera;
        [self presentModalViewController:self.pickerController animated:YES];
    } else if (buttonIndex == BUTTON_CAMERA_ROLL) {
        self.pickerController.sourceType = UIImagePickerControllerSourceTypePhotoLibrary;
        [self presentModalViewController:self.pickerController animated:YES];
    }
    [self resetSelection];
}

- (void)imagePickerController:(UIImagePickerController *)picker didFinishPickingMediaWithInfo:(NSDictionary *)info
{
	[self dismissModalViewControllerAnimated:YES];
    HUD = [[MBProgressHUD alloc] initWithView:self.navigationController.view];
	[self.navigationController.view addSubview:HUD];
	
	// Set determinate mode
	HUD.mode = MBProgressHUDModeAnnularDeterminate;
	
	HUD.delegate = self;
	HUD.labelText = @"Loading";
    [HUD show:YES];
	[uploader uploadImage:[info objectForKey:UIImagePickerControllerOriginalImage]];
}

-(void)imageUploadedWithURLString:(NSString*)urlString
{
    HUD.customView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"37x-Checkmark.png"]];
	HUD.mode = MBProgressHUDModeCustomView;
	HUD.labelText = @"Completed";
    [HUD hide:YES afterDelay:2];
    self.answerField.text = [NSString stringWithFormat:@"%@\n", urlString];
}

-(void)uploadFailedWithError:(NSError *)error
{
    HUD.mode = MBProgressHUDModeText;
    HUD.labelText = @"Error";
    [HUD hide:YES afterDelay:2];
}

-(void)uploadProgressedToPercentage:(CGFloat)percentage
{
    HUD.progress = percentage;
    if (HUD.progress >= 1.0) {
        HUD.mode = MBProgressHUDModeIndeterminate;
        HUD.labelText = @"Generating URL";
    }
}

- (void)textViewDidBeginEditing:(UITextView *)textView
{
    NSLog(@"here");
    NSString *text = textView.text;
    if ([text isEqualToString:@"My answer..."]) {
        textView.text = @"";
    }
}

#pragma mark -
#pragma mark SKRecognizerDelegate methods

- (void)recognizerDidBeginRecording:(SKRecognizer *)recognizer
{
    NSLog(@"Recording started.");
    
    transactionState = TS_RECORDING;
    
	HUD.mode = MBProgressHUDModeIndeterminate;
	HUD.labelText = @"Listening...";
    [HUD show:YES];

}

- (void)recognizerDidFinishRecording:(SKRecognizer *)recognizer
{
    NSLog(@"Recording finished.");

    transactionState = TS_PROCESSING;
}

- (void)recognizer:(SKRecognizer *)recognizer didFinishWithResults:(SKRecognition *)results
{
    NSLog(@"Got results.");
    NSLog(@"Session id [%@].", [SpeechKit sessionID]); // for debugging purpose: printing out the speechkit session id
    
    long numOfResults = [results.results count];
    
    transactionState = TS_IDLE;
    
    if (numOfResults > 0) {
        NSLog(@"%@", [results firstResult]);
        if ([self.answerField.text isEqualToString:@"My answer..."]) {
            self.answerField.text = [results firstResult];
        } else {
            NSString *appendedString = [NSString stringWithFormat:@"%@ %@", self.answerField.text, [results firstResult]];
            self.answerField.text = appendedString;
        }
    }
    
	if (numOfResults > 1)
		NSLog(@"%@", [[results.results subarrayWithRange:NSMakeRange(1, numOfResults-1)] componentsJoinedByString:@"\n"]);
    
    [self resetSelection];
    [HUD hide:YES];
	voiceSearch = nil;
}

- (void)recognizer:(SKRecognizer *)recognizer didFinishWithError:(NSError *)error suggestion:(NSString *)suggestion
{
    NSLog(@"Got error.");
    NSLog(@"Session id [%@].", [SpeechKit sessionID]); // for debugging purpose: printing out the speechkit session id
    
    transactionState = TS_IDLE;

    NSLog(@"ERROR");
    [self resetSelection];
    [HUD hide:YES];
	voiceSearch = nil;
}





@end
