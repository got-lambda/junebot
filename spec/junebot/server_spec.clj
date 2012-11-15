(ns junebot.server-spec
  (:use
    [speclj.core]
    [junebot.server]))

(describe "Truth"

  (it "is true"
    (should true))

  (it "is not false"
    (should-not false)))

(run-specs)