(ns junebot.client-spec
  (:use
    [speclj.core]
    [junebot.client]))

(describe "update-world"

  (with world-state {:walls []  :players []  :shots []})

  (it "updates walls on :new-world"
    (should= {:walls [1 2 3], :players [], :shots []}
             (update-world @world-state [:new-world [1 2 3]])))

  (it "updates players on :update-players"
    (should= {:walls [], :players [1 2], :shots []}
             (update-world @world-state [:update-players [1 2]])))

  (it "updates players on :update-shots"
    (should= {:walls [], :players [], :shots [1 2]}
             (update-world @world-state [:update-shots [1 2]])))
  )

(run-specs)