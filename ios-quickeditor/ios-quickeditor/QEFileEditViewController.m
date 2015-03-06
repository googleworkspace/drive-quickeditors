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
#import "QEFileEditViewController.h"
#import "QEUtilities.h"

@implementation QEFileEditViewController
@synthesize driveService = _driveService;
@synthesize driveFile = _driveFile;
@synthesize delegate = _delegate;
@synthesize saveButton = _saveButton;
@synthesize textView = _textView;
@synthesize originalContent = _originalContent;
@synthesize fileTitle = _fileTitle;
@synthesize fileIndex = _fileIndex;

#pragma mark - Managing the detail item
- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.
    self.textView.delegate = self;
    
    if (self.fileIndex == -1) {
        self.fileTitle = @"New file";
    } else {
        self.fileTitle = self.driveFile.title;
    }
    
    self.title = self.fileTitle;
    
    // In case of new file, show the title dialog.
    if (self.fileIndex == -1) {
        [self renameButtonClicked:nil];
    } else {
        [self loadFileContent];
    }
}

- (void)viewDidUnload
{
    [self setSaveButton:nil];
    [self setTextView:nil];
    [super viewDidUnload];
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    return (interfaceOrientation != UIInterfaceOrientationPortraitUpsideDown);
}

- (void)textViewDidBeginEditing:(UITextView *)textView {
    UIBarButtonItem *doneButton =
    [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemDone
                                                  target:self
                                                  action:@selector(doneEditing:)];
    self.navigationItem.leftBarButtonItem = doneButton;
}

- (void)textViewDidChange:(UITextView *)textView {
    [self toggleSaveButton];
}

- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex {
    if (buttonIndex == 1) {
        self.fileTitle = [[[alertView textFieldAtIndex:0] text] copy];
        self.title = self.fileTitle;
    }
    [self toggleSaveButton];
}

- (IBAction)doneEditing:(id)sender {
    [self.view endEditing:YES];
    self.navigationItem.leftBarButtonItem = nil;
}

- (IBAction)saveButtonClicked:(id)sender {
    [self saveFile];
}

- (IBAction)deleteButtonClicked:(id)sender {
    [self deleteFile];
}

- (IBAction)renameButtonClicked:(id)sender {
    UIAlertView * alert = [[UIAlertView alloc] initWithTitle:@"Edit title"
                                                     message:@""
                                                    delegate:self
                                           cancelButtonTitle:@"Cancel"
                                           otherButtonTitles:@"Ok", nil];
    alert.alertViewStyle = UIAlertViewStylePlainTextInput;
    UITextField * alertTextField = [alert textFieldAtIndex:0];
    alertTextField.placeholder = @"File's title";
    alertTextField.text = self.fileTitle;
    [alert show];
}

- (void)loadFileContent {
    UIAlertView *alert = [QEUtilities showLoadingMessageWithTitle:@"Loading file content"
                                                             delegate:self];
    GTMHTTPFetcher *fetcher =
    [self.driveService.fetcherService fetcherWithURLString:self.driveFile.downloadUrl];
    
    [fetcher beginFetchWithCompletionHandler:^(NSData *data, NSError *error) {
        [alert dismissWithClickedButtonIndex:0 animated:YES];
        if (error == nil) {
            NSString* fileContent = [[NSString alloc] initWithData:data
                                                          encoding:NSUTF8StringEncoding];
            self.textView.text = fileContent;
            self.originalContent = [fileContent copy];
        } else {
            NSLog(@"An error occurred: %@", error);
            [QEUtilities showErrorMessageWithTitle:@"Unable to load file"
                                               message:[error description]
                                              delegate:self];
        }
    }];
}

- (void)saveFile {
    GTLUploadParameters *uploadParameters = nil;
    
    // Only update the file content if different.
    if (![self.originalContent isEqualToString:self.textView.text]) {
        NSData *fileContent =
        [self.textView.text dataUsingEncoding:NSUTF8StringEncoding];
        uploadParameters =
        [GTLUploadParameters uploadParametersWithData:fileContent MIMEType:@"text/plain"];
    }
    
    self.driveFile.title = self.fileTitle;
    GTLQueryDrive *query = nil;
    if (self.driveFile.identifier == nil || self.driveFile.identifier.length == 0) {
        // This is a new file, instantiate an insert query.
        query = [GTLQueryDrive queryForFilesInsertWithObject:self.driveFile
                                            uploadParameters:uploadParameters];
    } else {
        // This file already exists, instantiate an update query.
        query = [GTLQueryDrive queryForFilesUpdateWithObject:self.driveFile
                                                      fileId:self.driveFile.identifier
                                            uploadParameters:uploadParameters];
    }
    UIAlertView *alert = [QEUtilities showLoadingMessageWithTitle:@"Saving file"
                                                             delegate:self];
    
    [self.driveService executeQuery:query completionHandler:^(GTLServiceTicket *ticket,
                                                              GTLDriveFile *updatedFile,
                                                              NSError *error) {
        [alert dismissWithClickedButtonIndex:0 animated:YES];
        if (error == nil) {
            self.driveFile = updatedFile;
            self.originalContent = [self.textView.text copy];
            self.fileTitle = [updatedFile.title copy];
            [self toggleSaveButton];
            [self.delegate didUpdateFileWithIndex:self.fileIndex
                                        driveFile:self.driveFile];
            [self doneEditing:nil];
        } else {
            NSLog(@"An error occurred: %@", error);
            [QEUtilities showErrorMessageWithTitle:@"Unable to save file"
                                               message:[error description]
                                              delegate:self];
        }
    }];
}

- (void)deleteFile {
    GTLQueryDrive *deleteQuery =
    [GTLQueryDrive queryForFilesDeleteWithFileId:self.driveFile.identifier];
    UIAlertView *alert = [QEUtilities showLoadingMessageWithTitle:@"Deleting file"
                                                             delegate:self];
    
    [self.driveService executeQuery:deleteQuery completionHandler:^(GTLServiceTicket *ticket,
                                                                    id object,
                                                                    NSError *error) {
        [alert dismissWithClickedButtonIndex:0 animated:YES];
        if (error == nil) {
            self.fileIndex = [self.delegate didUpdateFileWithIndex:self.fileIndex
                                                         driveFile:nil];
            [self.navigationController popViewControllerAnimated:YES];
        } else {
            NSLog(@"An error occurred: %@", error);
            [QEUtilities showErrorMessageWithTitle:@"Unable to delete file"
                                               message:[error description]
                                              delegate:self];
        }
    }];
}

- (void)toggleSaveButton {
    self.saveButton.enabled = 
    self.textView.text.length > 0 &&
    (![self.originalContent isEqualToString:self.textView.text] ||
     ![self.fileTitle isEqualToString:self.driveFile.title]);
}
@end
