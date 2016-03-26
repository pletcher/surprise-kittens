(ns surprise-kittens.test-runner
  (:require
   [doo.runner :refer-macros [doo-tests]]
   [surprise-kittens.core-test]))

(enable-console-print!)

(doo-tests 'surprise-kittens.core-test)
