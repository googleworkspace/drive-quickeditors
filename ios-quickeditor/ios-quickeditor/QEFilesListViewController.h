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
#import "GTMOAuth2ViewControllerTouch.h"

@interface QEFilesListViewController : UITableViewController <QEFileEditDelegate>
@property (weak, nonatomic) IBOutlet UIBarButtonItem *addButton;
@property (weak, nonatomic) IBOutlet UIBarButtonItem *authButton;
@property (weak, nonatomic) IBOutlet UIBarButtonItem *refreshButton;

@property (weak, readonly) GTLServiceDrive *driveService;
@property (retain) NSMutableArray *driveFiles;
@property BOOL isAuthorized;

- (IBAction)signoutButtonClicked:(id)sender;
- (IBAction)refreshButtonClicked:(id)sender;

- (void)toggleActionButtons:(BOOL)enabled;
- (void)viewController:(GTMOAuth2ViewControllerTouch *)viewController
      finishedWithAuth:(GTMOAuth2Authentication *)auth
                 error:(NSError *)error;
- (void)isAuthorizedWithAuthentication:(GTMOAuth2Authentication *)auth;
- (void)loadDriveFiles;
@end
