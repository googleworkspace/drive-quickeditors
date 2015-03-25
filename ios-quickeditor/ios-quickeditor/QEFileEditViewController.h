/*
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
#import "GTLDrive.h"
#import "QEFileEditDelegate.h"
#import <UIKit/UIKit.h>

@interface QEFileEditViewController : UIViewController <UITextViewDelegate, UIAlertViewDelegate>

@property GTLServiceDrive *driveService;
@property GTLDriveFile *driveFile;
@property id<QEFileEditDelegate> delegate;
@property NSInteger fileIndex;

@property (weak, nonatomic) IBOutlet UIBarButtonItem *saveButton;
@property (weak, nonatomic) IBOutlet UITextView *textView;

@property (strong) NSString *originalContent;
@property (strong) NSString *fileTitle;

- (IBAction)saveButtonClicked:(id)sender;
- (IBAction)deleteButtonClicked:(id)sender;
- (IBAction)renameButtonClicked:(id)sender;

- (void)loadFileContent;
- (void)saveFile;
- (void)deleteFile;
- (void)toggleSaveButton;


@end