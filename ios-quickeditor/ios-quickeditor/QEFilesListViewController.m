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
#import "QEFilesListViewController.h"

#import <GTLRDrive.h>
#import <Google/SignIn.h>

#import "QEFileEditViewController.h"
#import "QEUtilities.h"

@implementation QEFilesListViewController
@synthesize addButton = _addButton;
@synthesize authButton = _authButton;
@synthesize refreshButton = _refreshButton;
@synthesize driveFiles = _driveFiles;
@synthesize driveService = _driveService;


- (void)awakeFromNib
{
    [super awakeFromNib];
}

- (void)viewDidLoad
{
    // Configure Google Sign-in.
    GIDSignIn* signIn = [GIDSignIn sharedInstance];
    signIn.delegate = self;
    signIn.uiDelegate = self;
    signIn.scopes = [NSArray arrayWithObjects:kGTLRAuthScopeDrive, nil];
    [signIn signInSilently];
        
    self.driveService = [[GTLRDriveService alloc] init];
}

- (void)viewDidUnload
{
    [self setAddButton:nil];
    [self setRefreshButton:nil];
    [self setAuthButton:nil];
    [super viewDidUnload];
    // Release any retained subviews of the main view.
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    GIDGoogleUser *currentUser = [GIDSignIn sharedInstance].currentUser;
    NSLog(@"user = %@", currentUser);
    if (!currentUser) {
        [self toggleActionButtons:NO];
        [self showSignIn];
    }
}

- (void)showSignIn {
    [[GIDSignIn sharedInstance] signIn];
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    return (interfaceOrientation != UIInterfaceOrientationPortraitUpsideDown);
}

#pragma mark - Table View

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return self.driveFiles.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"Cell"];
    
    GTLRDrive_File *file = [self.driveFiles objectAtIndex:indexPath.row];
    cell.textLabel.text = file.name;
    return cell;
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    QEFileEditViewController *viewController = [segue destinationViewController];
    NSString *segueIdentifier = segue.identifier;
    viewController.driveService = [self driveService];
    viewController.delegate = self;
    
    if ([segueIdentifier isEqualToString:@"editFile"]) {
        NSIndexPath *indexPath = [self.tableView indexPathForSelectedRow];
        GTLRDrive_File *file = [self.driveFiles objectAtIndex:indexPath.row];
        viewController.driveFile = file;
        viewController.fileIndex = indexPath.row;
    } else if ([segueIdentifier isEqualToString:@"addFile"]) {
        viewController.driveFile = [GTLRDrive_File object];
        viewController.fileIndex = -1;
    }
}

- (NSInteger)didUpdateFileWithIndex:(NSInteger)index
                          driveFile:(GTLRDrive_File *)driveFile {
    if (index == -1) {
        if (driveFile != nil) {
            // New file inserted.
            [self.driveFiles insertObject:driveFile atIndex:0];
            index = 0;
        }
    } else {
        if (driveFile != nil) {
            // File has been updated.
            [self.driveFiles replaceObjectAtIndex:index withObject:driveFile];
        } else {
            // File has been deleted.
            [self.driveFiles removeObjectAtIndex:index];
            index = -1;
        }
    }
    return index;
}

- (void)signIn:(GIDSignIn *)signIn
didSignInForUser:(GIDGoogleUser *)user
     withError:(NSError *)error {
    GIDGoogleUser *currentUser = [GIDSignIn sharedInstance].currentUser;
    NSLog(@"user = %@", currentUser);

    if (currentUser) {
        self.driveService.authorizer = currentUser.authentication.fetcherAuthorizer;
        self.authButton.title = @"Sign out";
        [self toggleActionButtons:YES];
        [self loadDriveFiles];
    }
}

- (void)signIn:(GIDSignIn *)signIn
didDisconnectWithUser:(GIDGoogleUser *)user
     withError:(NSError *)error {
    NSLog(@"User signed-out");
}


- (IBAction)signoutButtonClicked:(id)sender {
    NSLog(@"Signing out user");
    [[GIDSignIn sharedInstance] signOut];
    self.driveService.authorizer = nil;
    [self toggleActionButtons:NO];
    [self.driveFiles removeAllObjects];
    [self.tableView reloadData];
    [self showSignIn];
}

- (IBAction)refreshButtonClicked:(id)sender {
    [self loadDriveFiles];
}

- (void)toggleActionButtons:(BOOL)enabled {
    self.addButton.enabled = enabled;
    self.refreshButton.enabled = enabled;
}


- (void)loadDriveFiles {
    GTLRDriveQuery_FilesList *query = [GTLRDriveQuery_FilesList query];
    query.q = @"mimeType = 'text/plain'";
    query.pageSize = 100;
    query.fields = @"files(id,name,mimeType,modifiedTime),nextPageToken";
    
    UIAlertView *alert = [QEUtilities showLoadingMessageWithTitle:@"Loading files"
                                                             delegate:self];
    [self.driveService executeQuery:query
                  completionHandler:^(GTLRServiceTicket *ticket,
                                      GTLRDrive_FileList *files,
                                      NSError *error) {
        [alert dismissWithClickedButtonIndex:0 animated:YES];
        if (error == nil) {
            if (self.driveFiles == nil) {
                self.driveFiles = [[NSMutableArray alloc] init];
            }
            [self.driveFiles removeAllObjects];
            [self.driveFiles addObjectsFromArray:files.files];
            // Sort Drive Files by modified date (descending order).
            [self.driveFiles sortUsingComparator:^NSComparisonResult(GTLRDrive_File *lhs,
                                                                     GTLRDrive_File *rhs) {
                return [rhs.modifiedTime.date compare:lhs.modifiedTime.date];
            }];
            [self.tableView reloadData];
        } else {
            NSLog(@"An error occurred: %@", error);
            [QEUtilities showErrorMessageWithTitle:@"Unable to load files"
                                               message:[error description]
                                              delegate:self];
        }
    }];
}
@end
