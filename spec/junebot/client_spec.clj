(ns junebot.client-spec
  (:use [speclj.core]
        [lamina.core]
        [junebot.client]))

(describe "get-color-from-name"

  (it "returns a color in RGB based on the given name"
    (should= [170 170 170]
             (get-color-from-name "HaX0R")))

  )

(describe "send-to-server"

  (with test-channel (channel))
  (with client (create-client test-channel))

  (it "queues the message to the client's channel"
    (send-to-server @client "message")
    (should= "message"
             @(read-channel @test-channel)))
  )

(describe "fire"
  (with test-channel (channel))
  (with client (create-client test-channel))

  (it "queues a :fire message to the client"
    (fire @client)
    (should= [:fire]
             @(read-channel @test-channel))))

(describe "move"
  (with test-channel (channel))
  (with client (create-client test-channel))

  (it "queues a :fire message to the client"
    (move @client "N")
    (should= [:move "N"]
             @(read-channel @test-channel))))

(describe "create-client"

  (with client (create-client "channel"))

  (it "sets channel properly"
    (should= "channel"
             (:channel @client)))

  (it "sets channel properly"
    (should= {:walls [], :players [], :shots []}
             @(:world-state @client)))

  )

(describe "update-world"

  (with world-state {:walls [], :players [], :shots []})

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

(describe "process-message"
  (with client (create-client "channel"))

  (it "updates the world based on the given message"
    (process-message @client [:new-world [1 2 3]])
    (should= {:walls [1 2 3], :players [], :shots []}
             @(:world-state @client)))

  )

(run-specs)