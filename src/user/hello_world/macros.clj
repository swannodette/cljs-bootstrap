(ns hello-world.macros)

(defmacro mult [a b]
  `(* ~a ~b))