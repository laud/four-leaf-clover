//
//  ImgurUploader.h
//  FourLeafClover
//
//  Created by Daniel Lau on 7/12/12.
//  Copyright (c) 2012 BazaarVoice. All rights reserved.
//
//  https://github.com/blladnar/iPhone-Imgur-Uploader

#import <Foundation/Foundation.h>

@protocol ImgurUploaderDelegate

-(void)imageUploadedWithURLString:(NSString*)urlString;
-(void)uploadProgressedToPercentage:(CGFloat)percentage;
-(void)uploadFailedWithError:(NSError*)error;

@end


@interface ImgurUploader : NSObject <NSXMLParserDelegate>
{
	id<ImgurUploaderDelegate> delegate;
	NSMutableData *receivedData;
	NSString* imageURL;
	NSString* currentNode;
	
	
}

-(void)uploadImage:(UIImage*)image;

@property (assign) id<ImgurUploaderDelegate> delegate;


@end
