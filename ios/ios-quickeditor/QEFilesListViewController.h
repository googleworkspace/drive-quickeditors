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
#import <UIKit/UIKit.h>
#import "QEFileEditDelegate.h"
#import <Google/SignIn.h>
#import <GTLRDrive.h>

@interface QEFilesListViewController : UITableViewController <QEFileEditDelegate, GIDSignInDelegate, GIDSignInUIDelegate>
@property (weak, nonatomic) IBOutlet UIBarButtonItem *addButton;
@property (weak, nonatomic) IBOutlet UIBarButtonItem *authButton;
@property (weak, nonatomic) IBOutlet UIBarButtonItem *refreshButton;

@property (retain) GTLRDriveService *driveService;
@property (retain) NSMutableArray *driveFiles;

- (IBAction)signoutButtonClicked:(id)sender;
- (IBAction)refreshButtonClicked:(id)sender;

- (void)toggleActionButtons:(BOOL)enabled;
- (void)loadDriveFiles;
@end
