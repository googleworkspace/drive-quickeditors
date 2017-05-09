# web-quickeditor

A sample text editor for the web browsers illustrating how to open and save files with the Google Drive API

## Overview

This is a simple text editor built with [AngularJS](https://www.angularjs.org),
and the [Google Drive API](https://developers.google.com/drive/web).

You can try out the app on its [live instance](http://googledrive.github.io/web-quickeditor).

## Installation and Configuration

The project can run on any static web server.

## Building from source

Fetch the source for the app:

    git clone https://github.com/googledrive/web-quickeditor.git
    cd web-quickeditor
    npm install

### Create a Google APIs project and Activate the Drive API

First, you need to activate the Drive API for your app. Activate it by configuring your API project in the
[Google Developers Console](https://console.developers.google.com/).

- Use [this wizard](https://console.developers.google.com/start/api?id=drive) to create or select a project in the Google Developers Console and automatically enable the API.
- Open the **API Manager** on the left sidebar.
- Select **Credentials** -> **New Credentials** -> **OAuth Client ID**
- If using a new proejct, select **Configure consent screen* and fill out the form
    - Select an **EMAIL ADDRESS** and enter a **PRODUCT NAME** if not already set and click the Save button.
- Select the application type *Web application**
- List your hostname in the **Authorized JavaScript Origins** field.
- Click the **Create** button. Note the resulting client ID and secret which you'll need later on.

To enable integration with the Drive UI, including the sharing dialog, perform the following steps.

- Select **Overview** section in **API Manager**
- Search for 'Drive API' and click on 'Drive API' in the results
- Click **Enable API**
- Select the **Drive UI Integration** tab
- Fill out the **Application Name** and upload at least one **Application Icon**.
- Set the **Open URL** to `http://YOURHOST/#/edit/{ids}/?user={userId}`
- Check the **Create With** option and set the **New URL** to `http://YOURHOST/#/edit?user={userId}`
- Check the **This application works properly with files in Team Drives** option
- Click **Save Changes**

Adjust the above URLs as needed for the correct hostname or path. Localhost is currently not allowed.

### Setup your App information

Update the `clientId` and `applicationId` constants in `index.js` with your app's identity.

### Deploy & run

You can run a local server with `npm start`:
