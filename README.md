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

Checkout my fork of [tools.reader](https://github.com/swannodette/tools.reader) 
from master and switch to the `cljs-bootstrap` branch. Install it into your 
local Maven:

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

## Hacking

Progress is constantly being made against master. If you are feeling
adventurous the following will let you see the current state of things. In
a *Clojure* REPL copy the `cljs/core.cljc` macros file and the `cljs/core.cljs` 
standard library file into `resources` (make sure you've created the 
`resources/cljs` parent directory).

```clj
(require '[clojure.java.io :as io])
(spit "resources/cljs/core.cljc" (slurp (io/resource "cljs/core.cljc")))
(spit "resources/cljs/core.cljs" (slurp (io/resource "cljs/core.cljs")))
(spit "resources/cljs/core.cljs.cache.aot.edn" 
  (slurp (io/resource "cljs/core.cljs.cache.aot.edn")))
```

Start a *ClojureScript* REPL. First you must load the macros file at the REPL:

```clj
(load-file "/full/path/to/resources/cljs/core.cljc")
```

Then load this project's namespace:

```clj
(require 'cljs-bootstrap.core)
```

Switch into this namespace and you should be able to eval the comment snippets
in the `src/cljs_bootstrap/core.cljs` source file.

## License

Copyright © 2015 David Nolen, Rich Hickey & Contributors

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
