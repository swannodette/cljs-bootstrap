# cljs-bootstrap

Use ClojureScript to compile itself.

## Usage

Install [ClojureScript](http://github.com/clojure/clojurescript) from master. 
Checkout the ClojureScript repo and build and install into your local Maven:

```
cd clojurescript
./script/build
```

Note the ClojureScript version number. Modify this repo's `project.clj` file to
reflect the version number. 

Checkout [tools.reader](https://github.com/clojure/tools.reader) from master.
Install it into your local Maven:

```
cd tools.reader
lein install
```

Install the NPM dependencies to get reasonable stack traces:

```
cd cljs-bootstrap
lein npm install
```

Start the REPL:

```
lein trampoline run -m clojure.main script/repl.clj 
```

Try the following at the REPL by loading the necessary namespaces:

```clj
(require-macros '[cljs.env.macros :refer [ensure]])
(require '[cljs.analyzer :as ana] '[cljs.compiler :as c])
```

Now you can eval. Note currently only trivial expressions work. Arbitrary
source code requires macro support which has not yet landed in ClojureScript
master:

```clj
(js/eval
  (with-out-str
    (c/emit
      (ensure
        (ana/analyze-keyword
          (assoc (ana/empty-env) :context :expr)
          :foo)))))
```

## License

Copyright © 2015 David Nolen, Rich Hickey & Contributors

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
