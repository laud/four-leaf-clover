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

@implementation FLCDetailViewController

@synthesize question;

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
        [self resetSelection];
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




@end
