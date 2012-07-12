//
//  FLCQuestionViewController.m
//  FourLeafClover
//
//  Created by Daniel Lau on 7/11/12.
//  Copyright (c) 2012 BazaarVoice. All rights reserved.
//

#import "FLCQuestionViewController.h"
#import "FLCDetailViewController.h"
#import "FLCQuestion.h"


@interface FLCQuestionViewController ()
{
    NSMutableArray *_questions;
}

@end

@implementation FLCQuestionViewController

- (id)initWithStyle:(UITableViewStyle)style
{
    self = [super initWithStyle:style];
    if (self) {
        // Custom initialization
    
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    self.title = @"Four Leaf Clover";
    
    // Create demo data
    FLCQuestion *q1 = [[FLCQuestion alloc] initWithId:@"1" question:@"Why am i here?" asker:@"Daniel Lau"];
    FLCQuestion *q2 = [[FLCQuestion alloc] initWithId:@"2" question:@"Who am i?" asker:@"Daniel Lau"];
    FLCQuestion *q3 = [[FLCQuestion alloc] initWithId:@"3" question:@"Where am i ?" asker:@"Daniel Lau"];
    FLCQuestion *q4 = [[FLCQuestion alloc] initWithId:@"4" question:@"Why am i there?" asker:@"Daniel Lau"];
    FLCQuestion *q5 = [[FLCQuestion alloc] initWithId:@"5" question:@"Why am i bear?" asker:@"Daniel Lau"];
    FLCQuestion *q6 = [[FLCQuestion alloc] initWithId:@"6" question:@"Why am i care?" asker:@"Daniel Lau"];
    FLCQuestion *q7 = [[FLCQuestion alloc] initWithId:@"7" question:@"Why am i lair?" asker:@"Daniel Lau"];
    FLCQuestion *q8 = [[FLCQuestion alloc] initWithId:@"8" question:@"Why am i legion?" asker:@"Daniel Lau"];
    FLCQuestion *q9 = [[FLCQuestion alloc] initWithId:@"9" question:@"Why am i red?" asker:@"Daniel Lau"];
    FLCQuestion *q10 = [[FLCQuestion alloc] initWithId:@"10" question:@"Why am i bullish?" asker:@"Daniel Lau"];
    _questions = [[NSMutableArray alloc] initWithObjects:q1, q2, q3, q4, q5, q6, q7, q8, q9, q10, nil];
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

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    // Return the number of sections.
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    // Return the number of rows in the section.
    return [_questions count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *CellIdentifier = @"QuestionCell";
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    
    // Configure the cell...
    FLCQuestion *question = [_questions objectAtIndex:indexPath.row];
    cell.textLabel.text = question.question;
    cell.detailTextLabel.text = question.asker;
    
    return cell;
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if ([[segue identifier] isEqualToString:@"QuestionDetailSegue"]) {
        
        NSIndexPath *selectedRowIndex = [self.tableView indexPathForSelectedRow];
        FLCDetailViewController *detailViewController = [segue destinationViewController];
        detailViewController.question = [_questions objectAtIndex:selectedRowIndex.row];;
    }
}


@end
