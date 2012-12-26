PostMan (rings twice) Lib
=========

_'With my brains and your looks, we could go places' - Frank Chambers, The postman rings always twice_


LibPostman (Rings Twice) for Android is a library intended to make the asynchronous interaction with a remote server easier, without having to deal with all the well known problems related to asynctasks bound to activities.  

It uses scribe java library under the hood for basic http calls and for oauth 1.0 and 2.0 authentication.



Architecture
-------
PostmanLib's basic architecture is barely inspired by Virgil Dobjanschi's talk at Google IO 2010, and it was initially developed after the experience I had interacting with a rest service during the development of MyDearSanta app.
After having it _in progress_ for more than a year I decided (XMas 2012) to make it working.

----

![alt text](https://raw.github.com/fedepaol/PostmanLib--Rings-Twice--Android/master/images/postman.png "Logo Title Text 1")

---

##ServerInteractionHelper
It's a singleton class to be used to send asynchronous commands. Allows also to register listener to be notified of execution results / failures. 
Any sent command is associated to a requestid which can be used to check if the given request is still pending and is passed back in result notifications. 

###ServerCommand
A server command is an implementation of the command pattern. The execute() method will be 
executed asynchronously and it is guaranteed to be executed in a background thread. 

###RestServerCommand
RestServerCommand is a specialization of ServerCommand. It's intended to be used to perform rest operations against a given url. It uses Java Scribe library under the hood, and it can be associated to an authenticated OAuthService to perform oauth authorized requests. 

##OAuth Authentication

Another feature provided by ServerInteractionHelper is to asynchronously authorize a Scribe OAuth service, displaying a webview to the user for authorization. Once the OAuthService is registered, it can be used to sign RestServer commands. 


####TO BE CONTINUED....


