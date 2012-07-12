//
//  FLCDetailViewController.m
//  FourLeafClover
//
//  Created by Daniel Lau on 7/11/12.
//  Copyright (c) 2012 BazaarVoice. All rights reserved.
//

#import "FLCDetailViewController.h"
#import "FLCQuestion.h"

#define CONTROL_CAMERA 0
#define CONTROL_WRITE 1
#define CONTROL_SPEAK 2

@interface FLCDetailViewController ()

@end

@implementation FLCDetailViewController

@synthesize question;

@synthesize askerLabel;
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

-(void)dismissKeyboard {
    [answerField resignFirstResponder];
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view.
    UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc]
                                   initWithTarget:self
                                   action:@selector(dismissKeyboard)];
    
    [self.view addGestureRecognizer:tap];
    
    questionLabel.text = question.question;
    askerLabel.text = question.asker;
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
	NSLog(@"selected %u", [segmentedControl selectedSegmentIndex]);
}

- (IBAction)submitButtonPressed:(id)sender
{
    [self dismissKeyboard];
    NSLog(@"submit button pressed");
}

@end
