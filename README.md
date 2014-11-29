s3-cljs
=======

`s3-cljs` is a clojurescript core.async interface to [s3 javascript sdk](http://docs.aws.amazon.com/AWSJavaScriptSDK/latest/AWS/S3.html).

Implemented functions:
* init
* getSignedUrl
* get-object
* get-object-body
* list-objects
* files-with-prefix-and-suffix
* get-objects

Usage
-----
Add to your `project.clj`:

[![Clojars Project](http://clojars.org/viebel/s3-cljs/latest-version.svg)](http://clojars.org/viebel/s3-cljs)

and in addition you have to add the `aws-sdk` to your project.
You can download it from: `https://sdk.amazonaws.com/js/aws-sdk-2.0.29.min.js`. 
Tested with version `2.0.29`.

See [here](https://groups.google.com/forum/#!searchin/clojurescript/javascript$20extern/clojurescript/iBWLAJ3TW7I/GKhvWnzFlNEJ) why this is required for the moment.



Deployment (to [clojars](https://clojars.org/))
------------------------------------
Update the version number in `project.clj` and then execute:

```
lein deploy clojars
```
