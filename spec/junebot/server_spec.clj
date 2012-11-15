(ns junebot.server-spec
  (:use
    [speclj.core]
    [junebot.server]))

(describe "Truth"

  (it "is true"
    (should true))

  (it "is not false"
    (should-not false)))



(describe "Free-position"
          (with state
                [{:coord [1 1]}
                 {:coord [1 2]}
                 {:coord [2 1]}
                 {:coord [1 3]}
                 {:coord [3 1]}])
          (it "should return false when position is taken"
              (should-not (free-position? @state [2 1])))

          (it "should return true when position is free"
              (should (free-position? @state [2 2]))))



(describe "calculate-position"
          (it "should return the corect new possition"
              (should (= (calculate-position {:coord [0 0]} [0 1])
                         [0 1]))
              (should (= (calculate-position {:coord [3 3]} [-1 0])
                         [2 3]))
              (should (= (calculate-position {:coord [1 3]} [-1 1])
                         [0 4]))
              (should-not (= (calculate-position {:coord [2 2]} [-1 -1])
                             [3 3]))))

(describe "process-message move"

          (with world
                {:players {0 {:coord [2 2]}
                           1 {:coord [2 3]}}
                 :shots []
                 :walls [{:coord [2 1]}
                         {:coord [3 3]}]})

          (it "should move the correct player"
              (let [new-state (process-message @world 0 [:move "E"])]
                (should (= (get-in new-state [:new-state :players 0 :coord])
                           [3 2]))))

          (it "should not move to a positon taken by player"
              (let [new-state (process-message @world 0 [:move "N"])]
                (should (= @world (:new-state new-state)))))

          (it "should not move to a position taken by a wall"
              (let [new-state (process-message @world 0 [:move "S"])]
                (should (= @world (:new-state new-state))))))

(describe "process-message shot"

          (with world
                {:players {0 {:direction [0 1] :coord [3 3]}}
                 :walls   [{:coord [2 3]}]
                 :shots   []})

          (it "should add a shot when fired"
              (should (= [:update-shots [{:position  [3 4]
                                          :direction [0 1]}]]
                         (:send-back (process-message @world 0 [:fire])))))

          (it "should not fire when next to wall"
             (=
              {:new-state @world
               :send-back [:update-shots []]}
              (-> (process-message @world 0 [:move "E"])
                  :new-state
                  (process-message 0 [:move "W"])
                  :new-state
                  (process-message 0 [:fire])))))



(use 'lamina.core)


(describe "update-and-send"

          (with server
                {:broadcast-channel (channel)
                 :world-state
                 (atom {:players {0 {:coord [2 2] :direction [1 0]}
                                  1 {:coord [2 3]}}
                        :shots []
                        :walls [{:coord [2 1]}
                                {:coord [3 3]}]})})

          (it "should send back the correct players when sent move msg"

              (update-and-send @server 0 [:move "E"])
              (should (= (-> @(-> @server :broadcast-channel read-channel)
                             (get-in [1])
                             set)
                         #{{:coord [2 3]} {:coord [3 2]
                                           :direction [1 0]}})))

          (it "should send back a update shot when a shot is fired"

              (update-and-send @server 0 [:fire])

              (let [v (-> @(-> @server :broadcast-channel read-channel)
                             (get-in [1])
                             set)]
                (should (= v
                         #{{:position [3 2] :direction [1 0]}})))))





(run-specs)
