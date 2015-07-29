# ios-quickeditor

A sample text editor for iOS illustrating how to open and save files with the [Google Drive API](https://developers.google.com/drive/ios)

## Overview

Quickeditor is a sample Google Drive app written in Objective-C for iOS. It is a text editor 
capable of editing files with the MIME type text/* that are stored in a user's Google Drive

## Prerequisites

Building this sample requires:

* [Xcode 6](https://developer.apple.com/xcode/downloads/)

## Building the sample

### Create a Google APIs project and activate the Drive API

First, you need to activate the Drive API for your app. You can do it by configuring your API project in the
[Google Developers Console](https://console.developers.google.com/).


- Use [this wizard](https://console.developers.google.com/start/api?id=drive) to create or select a project in the Google Developers Console and automatically enable the API.
- In the sidebar on the left, select **Consent screen**. Select an **EMAIL ADDRESS** and enter a **PRODUCT NAME** if not already set and click the Save button.
- In the sidebar on the left, select **Credentials** and click **Create new Client ID**.
- Select the application type **Installed application**, **Other**, and click the **Create Client ID** button.
- Note the **CLIENT ID** and **CLIENT SECRET** values

### Fetch and build the app

1. Clone the git repo

        git clone git@github.com:googledrive/ios-quickeditor.git
        cd ios-quickeditor
1. Open `ios-quickeditor/ios-quickeditor.xcworkspace` in Xcode
1. Edit `QEFilesListViewController.m` and replace `<CLIENT_ID>` and `<CLIENT_SECRET>` with the values from the
[Google Developers Console](https://console.developers.google.com/apis/console/)
1. Build the project and run it on the iOS simulator.

