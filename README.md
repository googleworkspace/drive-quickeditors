# ios-quickeditor

A sample text editor for iOS illustrating how to open and save files with the [Google Drive API](https://developers.google.com/drive/ios)

## Overview

Quickeditor is a sample Google Drive app written in Objective-C for iOS. It is a text editor 
capable of editing files with the MIME type text/* that are stored in a user's Google Drive

## Prerequisites

To run this sample, you'll need:

* [Xcode 8.0](https://developer.apple.com/xcode/) or greater.
* [CocoaPods](http://cocoapods.org/) dependency manager.
* Access to the internet and a web browser.
* A Google account with Google Drive enabled.


## Building the sample

### Enable the Drive API and Google Sign-in

1. Use [this wizard](https://developers.google.com/mobile/add?platform=ios&cntapi=signin&cnturl=https%3A%2F%2Fconsole.developers.google.com%2Fstart%2Fapi%3Fid%3Ddrive&cntlbl=Enable%20the%20Drive%20API) to create a new app or select an existing one.
1. Enter "com.google.drive.samples.ios-quick-editor.ios-quickeditor into the field iOS Bundle ID and click the Continue button.
1. Click the Google Sign-In icon and then click the Enable Google Sign-in button. Click the Continue button.
1. Click the Download `GoogleService-Info.plist` button to download the configuration file. Take note of where you saved it. Click the Enable the Drive API button.
1. Use the dropdown menu to select the same project you use in the previous wizard and click the Continue button.
1. Close the wizard.

### Fetch and build the app

1. Clone the git repo

        git clone git@github.com:googledrive/ios-quickeditor.git
        cd ios-quickeditor
1. Open `ios-quickeditor/ios-quickeditor.xcworkspace` in Xcode
1. Repace `GoogleService-Info.plist` with the file you previously downloaded
1. Build the project and run it on the iOS simulator.

