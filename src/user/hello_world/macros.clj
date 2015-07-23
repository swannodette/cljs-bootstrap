(ns hello-world.macros)

(defmacro multi [a b]
  `(* ~a ~b))