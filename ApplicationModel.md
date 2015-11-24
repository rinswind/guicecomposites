# Introduction #

Both Guice Composites extensions try to support a particular OO application model. I feel this model is quite common in OO and reflects the structure of most monolithic pieces of an app. E.g. even if an app is distributed as plugins in a framework each plugin is likely to be structured as follows:

# Details #

Every OO app consists of graphs of collaborating objects. There is usually an initial graph of the longest-lived objects. I call these the _initial set of singletons_. For example when a server app starts it will create an object that drives the accept loop and say a pair of stateless RequestReader and ResponseWriter services. As these singletons work they will spawn graphs of shorter lifespan. When such a graph is created the older objects that created it can choose to retain some reference to the new objects. I call these _forward references_. The objects of the new graph on the other hand can require references to their parents in setters or constructors. I call these _back references_. For example the accept loop object will create a Connection-InputStream-OutputStream trio to handle each incoming connection. It may _forward reference_ these Connections in a list so it can close them all in case of a shutdown. The Connection object on the other hand wants a _back reference_ to the common RequestReader service in it's constructor. This continues with even shorter-lived graphs. The Connection uses the Parser service to deserialize incoming messages and spawns a Request-Response pair of objects to handle each one. Say the Request references the Response and vice versa. The request also _back references_ the InputStream of the _current_ connection while the response _back references_ the OutputStream of the _current_ connection. The back references can jump to older generations. Say the Response needs a back reference to the initial singleton ResponseWriter service.

So the vocabulary of the model is:
  * scope:
  * scope instance: A set of objects with a common lifecycle. Created by a scope intance factory.
  * scope instance factory: Dynamically generated factory called by the App to spawn new scope instances.
  * internal reference: Between objects of the same scope instance. Usually 1-to-1. Resolved by Guice.
  * forward reference: From objects of a parent scope instance to objects of child scope instance. Must be 1-to-n. Resolved by the App.
  * back reference: From objects of a child scope instance to objects of a parent scope instance. Must be n-to-1. Resolved by Guice.
  * scope instance parameter: An object passed to a method of a scope instance factory. Obtained by the App without DI. These objects become equal members of the new scope instance.