# Introduction #

Composite Scopes evolved out of Composite Injectors and is the more advanced of the two extensions. Rather than nested Injectors is uses a hierarchy of dynamically generated scopes to track object graphs.

# Details #

The advantages Composite Scopes has over Composite Injectors are:

  * Better ahead of time validation: Everything is validate at Injector creation time.
  * Better performance: There is no need to create and validate Injectors for every object graph. Now only one Injector needs to ever be created.

The disadvantages are:

  * Less flexibility: Because Composite Injectors uses one Injector per object graph the user has the flexibility to use his own scoping for each generation of objects. Also this allows the injection of Providers or the Injector in conjunction with the generated factories. Composite Scopes uses scopes to track object graphs so the user has a caching strategy chosen for him. Also because of the way Composite Scopes captures the local object context it does not tolerate the injection of other factory-like objects except it's own dynamically generated factories.