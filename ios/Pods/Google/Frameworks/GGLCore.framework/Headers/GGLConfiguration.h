/*
 * Copyright 2022 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#import <Foundation/Foundation.h>

/**
 * This class provides configuration fields of Google APIs.
 */
@interface GGLConfiguration : NSObject

/**
 * The OAuth2 client ID for the iOS application, used to authenticate Google users. For example
 * @"12345.apps.googleusercontent.com".
 */
@property(nonatomic, readonly, copy) NSString *clientID;

/**
 * The tracking ID for Google Analytics, e.g. @"UA-12345678-1", used to configure Google Analytics.
 */
@property(nonatomic, readonly, copy) NSString *trackingID;

/**
 * The Google App ID that is used to uniquely identify an instance of an app.
 */
@property(nonatomic, readonly, copy) NSString *googleAppID;

/**
 * Whether or not Analytics was enabled in the developer console.
 */
@property(nonatomic, readonly) BOOL isAnalyticsEnabled;

/**
 * Whether or not Measurement was enabled. Measurement is enabled unless explicitly disabled in
 * GoogleService-Info.plist.
 */
@property(nonatomic, readonly) BOOL isMeasurementEnabled;

/**
 * Whether or not SignIn was enabled in the developer console.
 */
@property(nonatomic, readonly) BOOL isSignInEnabled;

/**
 * The version ID of the client library, e.g. @"1100000".
 */
@property(nonatomic, readonly, copy) NSString *libraryVersionID;

@end
