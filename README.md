##_DISCLAIMER_
#### This library is still under construction. Beta testers are more than welcome. 


PostMan (rings twice) Lib
=========

_'With my brains and your looks, we could go places' - Frank Chambers, The postman rings always twice_


LibPostman (Rings Twice) for Android is a library intended to make the asynchronous interaction with a remote server easier, without having to deal with all the well known problems related to asynctasks bound to activities.

It uses [scribe java library][scribe] under the hood for basic http calls and for oauth 1.0 and 2.0 authentication. The minSdkVersion declared in the lib's manifest is 8 since scribe itself uses HttpUrlConnection which received a significant amount of bugfixes in ApiLevel 8, 



Architecture
-------
PostmanLib's basic architecture is barely inspired by Virgil Dobjanschi's talk at Google IO 2010, and it was initially developed after the experience I had interacting with a rest service during the development of MyDearSanta app.
After having it _in progress_ for more than a year I now decided (XMas 2012) to make it working.

----

![alt text](https://raw.github.com/fedepaol/PostmanLib--Rings-Twice--Android/master/images/postman.png "Logo Title Text 1")

---

###Setup

The service used by the library must be declared into the application's manifest:

    <service
            android:enabled="true"
            android:name="com.whiterabbit.postman.InteractionService" />  


##ServerInteractionHelper
It's a singleton class to be used to send asynchronous commands. Allows also to register listener to be notified of execution results / failures.
Any sent command is associated to a requestid which can be used to check if the given request is still pending and is passed back in result notifications.

###ServerCommand
A server command is an implementation of the command pattern. The execute() method will be
executed asynchronously and it is guaranteed to be executed in a background thread.

###RestServerCommand
RestServerCommand is a specialization of ServerCommand. It's intended to be used to perform rest operations against a given url.
It uses [Java Scribe library][scribe] under the hood, and it can be associated to an authenticated OAuthService to perform oauth authorized requests.
To perform its actions must be initialized with one or more RestStrategy objects that represent the actions to be performed in background

####Executing a RestServerRequest
To make a request you need to implement a RestServerRequestObject.
The most obvious method to implement are _getUrl_ and _getVerb_ .

Since a RestServerRequest executes a _scribe_ oauth request under the hood (even if not authenticated), you need to implement _addParamsToRequest_ in order to enrich the request.
For example, in order to add a "status" parameter in a call:

    request.addBodyParameter("status", "this is sparta! *");

or

    request.addQuerystringParameter("status", "this is sparta!");

in case of a query string parameter.

####Sending a server command:
A RestServerRequest might be used to initialize a RestServerCommand to be sent. There is also an helper method to make the things easier:

    ServerInteractionHelper.getInstance().sendRestAction(this,"MyRequestId", request1, request2);

####Handling a response
The

    void processHttpResult(Response result, Context context);

of all the _RestServerAction_s you passed will be called with the result of the call.

The _Response_ parameter is a scribe Response object. You can get the stringified result using

    result.getBody()

in case of a text result, such as json.

Otherwise, a stream can be fetched using:

    result.getStream()




##OAuth Authentication

Another feature provided by ServerInteractionHelper is to asynchronously authorize a [Scribe][scribe] OAuth service, displaying a webview to the user for authorization. Once the OAuthService is registered, it can be used to sign RestServer commands.

![alt text](https://raw.github.com/fedepaol/PostmanLib--Rings-Twice--Android/master/images/postman_oauth.png "Logo Title Text 1")

---

Requesting an authorization is quite simple.
A storable builder, which is a simple wrapper to _scribe_'s ServiceBuilder, must be created and registered against the _OAuthHelper_.

    StorableServiceBuilder builder = new StorableServiceBuilder("Twitter")
                .provider(TwitterApi.class)
                .apiKey("YOURAPIKEY")
                .apiSecret("YOURAPISECRET");

    OAuthHelper o = OAuthHelper.getInstance();
    o.registerOAuthService(builder, this);

In case the service is not yet authorized, the authorization process must be started:

    if(!o.isAlreadyAuthenticated("Twitter", this)){
        o.authenticate(this, "Twitter");
    }


PostManLib library will provide a dialog to the user containing the webview to authorize the application.

Once the service is registered, all it takes to sign a RestServerRequest is to return the name of the signer in _getOauthSigner_

    public String getOAuthSigner() {
        return "Twitter";
    }

Using the same name used to register the service.


# NOTES

* In order to make the authentication possible you need to have a callback url. In twitter you need to explicitly set up a callback url into the api dashboard. If it's not the case, a redirect will not occour and OAuth Fragment will not be able to intercept the verifer token from the redirect.

* Adding call parameters is straight. You however need to rely on [Scribe][scribe] documentation in order to check how to add them

* The common pattern I suggest to use with postman lib is not to return all the data to the activity, but update your data model <b>inside the processHttpResult</b> method and only update the ui when the activity gets notified of the end of the request

* _ServerCommand_ implement Parcelable interface. This is because the command must be passed to the intent service through an intent. In addition to the _Parcelable_ methods, every command implementation must have a static CREATOR field, such as 
    
    public static final Parcelable.Creator<SampleRestCommand> CREATOR = ...

* In case of multiple rest requests, a failure will be notified whenever one of the requests fails, even if the previous ones are executed successfully.

License
-------

  Copyright (C) 2013 Federico Paolinelli

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

	     http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.


## TO BE ADDED / ROADMAP

* Plain authentication handling
* Localization of error messages 
* callback url personalization. Not sure yet that __oauth__verifer__  is a standard
* Recycling commands using a pool
* Having more than one intentservice to be round robin served for parallel requests




[scribe]: https://github.com/fernandezpablo85/scribe-java



