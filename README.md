# web-quickeditor

A sample text editor for the web browsers illustrating how to open and save files with the Google Drive API

## Overview

This is a simple text editor built with [AngularJS](https://www.angularjs.org),
and the [Google Drive API](https://developers.google.com/drive/web).

You can try out the app on its [live instance](http://googledrive.github.io/web-quickeditor).

## Installation and Configuration

The project can run on any static web server.

## Building from source

If you don't already have [Gulp](http://gulpjs.com/) and [Bower](http://bower.io/) you can install with:

    npm install -g gulp bower

Fetch the source for the app:

    git clone https://github.com/googledrive/web-quickeditor.git
    cd web-quickeditor
    npm install & bower install

### Create a Google APIs project and Activate the Drive API

First, you need to activate the Drive API for your app. You can do it by configuring your API project in the
[Google Developers Console](https://console.developers.google.com/).


- Use [this wizard](https://console.developers.google.com/start/api?id=drive) to create or select a project in the Google Developers Console and automatically enable the API.
- In the sidebar on the left, select **Consent screen**. Select an **EMAIL ADDRESS** and enter a **PRODUCT NAME** if not already set and click the Save button.
- In the sidebar on the left, select **Credentials** and click **Create new Client ID**.
- Select the application type *Web application** and click the **Create Client ID** button.
- List your hostname in the **Authorized JavaScript Origins** field.

To enable integration with the Drive UI, including the sharing dialog, perform the following steps.

- Select the tab **APIs & Auth** > **APIs** and click the **OFF** button next to **Drive SDK** to enable it.
- Click the gear icon next to **Drive SDK**
- Click the link to return to the original console.
- Fill out the **Application Name** and upload at least one **Application Icon**.
- Set the *Open URL** to `http://YOURHOST/#/edit/{ids}/?user={userId}`
- Check the *Create With** option and set the **New URL** to `http://YOURHOST/#/edit?user={userId}`

Adjust the above URLs as needed for the correct hostname or path. Localhost is currently not allowed.

### Setup your App information

Update the `clientId` and `applicationId` constants in `index.js` with your app's identity.

### Deploy & run

You can run a local server with gulp:

    gulp serve
