(ns junebot.client-spec
  (:use
    [speclj.core]
    [junebot.client]))

(describe "Truth"

  (it "is true"
    (should true))

  (it "is not false"
    (should-not false)))

(run-specs)