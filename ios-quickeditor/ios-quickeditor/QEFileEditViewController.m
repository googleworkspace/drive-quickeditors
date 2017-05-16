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
        self.fileTitle = self.driveFile.name;
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
    GTLRQuery *query = [GTLRDriveQuery_FilesGet queryForMediaWithFileId:self.driveFile.identifier];

    [self.driveService executeQuery:query
        completionHandler:^(GTLRServiceTicket *callbackTicket,
                            GTLRDataObject *dataObject,
                            NSError *error) {
            [alert dismissWithClickedButtonIndex:0 animated:YES];
            if (error == nil) {
                NSString* fileContent = [[NSString alloc] initWithData:dataObject.data
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
    GTLRUploadParameters *uploadParameters = nil;
    
    // Only update the file content if different.
    if (![self.originalContent isEqualToString:self.textView.text]) {
        NSData *fileContent =
        [self.textView.text dataUsingEncoding:NSUTF8StringEncoding];
        uploadParameters = [GTLRUploadParameters uploadParametersWithData:fileContent MIMEType:@"text/plain"];
    }
    
    GTLRDrive_File *metadata = [[GTLRDrive_File alloc] init];
    metadata.name = self.fileTitle;
    metadata.mimeType = @"text/plain";
    
    GTLRDriveQuery *query = nil;
    if (self.driveFile.identifier == nil || self.driveFile.identifier.length == 0) {
        // This is a new file, instantiate an insert query.
        query = [GTLRDriveQuery_FilesCreate queryWithObject:metadata
                                            uploadParameters:uploadParameters];
    } else {
        // This file already exists, instantiate an update query.
        query = [GTLRDriveQuery_FilesUpdate queryWithObject:metadata
                                                     fileId:self.driveFile.identifier
                                            uploadParameters:uploadParameters];
    }
    query.fields = @"id,name,modifiedTime,mimeType";
    UIAlertView *alert = [QEUtilities showLoadingMessageWithTitle:@"Saving file"
                                                             delegate:self];
    
    [self.driveService executeQuery:query
                  completionHandler:^(GTLRServiceTicket *ticket,
                                     GTLRDrive_File *updatedFile,
                                     NSError *error) {
        [alert dismissWithClickedButtonIndex:0 animated:YES];
        if (error == nil) {
            self.driveFile = updatedFile;
            self.originalContent = [self.textView.text copy];
            self.fileTitle = [updatedFile.name copy];
            [self toggleSaveButton];
            [self.delegate didUpdateFileWithIndex:self.fileIndex
                                        driveFile:self.driveFile];
            [self doneEditing:nil];
        } else {
            NSLog(@"Error: %@", error);
            NSDictionary *errorInfo = [error userInfo];
            NSData *errData = errorInfo[kGTMSessionFetcherStatusDataKey];
            if (errData) {
                NSString *dataStr = [[NSString alloc] initWithData:errData
                                                          encoding:NSUTF8StringEncoding];
                NSLog(@"Details: %@", dataStr);
            }
            [QEUtilities showErrorMessageWithTitle:@"Unable to save file"
                                               message:[error description]
                                              delegate:self];
        }
    }];
}

- (void)deleteFile {
    GTLRDriveQuery *deleteQuery = [GTLRDriveQuery_FilesDelete queryWithFileId:self.driveFile.identifier];
    UIAlertView *alert = [QEUtilities showLoadingMessageWithTitle:@"Deleting file"
                                                             delegate:self];
    
    [self.driveService executeQuery:deleteQuery completionHandler:^(GTLRServiceTicket *ticket,
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
     ![self.fileTitle isEqualToString:self.driveFile.name]);
}
@end
