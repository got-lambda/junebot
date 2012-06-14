(ns junebot.test.server
  (:use [junebot.server])
  (:use [clojure.test])
  (:use [aleph.tcp])
  (:use [lamina.core]))

;; adding a user to an empty world adds a player to the world
(deftest add_user_to_empty_world
  (let [before (number-of-players)]
    (new-client (channel) "hi")
    (let [after (number-of-players)]
      (is (= after (inc before))))))

;; (deftest a_user_has_coordinates)

(deftest moving_north_changes_coordinates
  (send-off world (fn [state] {42 [3 4]}))
  (process-message 42 "N")
  (. Thread sleep 10)
  (is (= [3 3] (get @world 42)))

  (send-off world (fn [state] {42 [3 4]}))
  (process-message 42 "S")
  (. Thread sleep 10)
  (is (= [3 5] (get @world 42)))

  (send-off world (fn [state] {42 [3 4]}))
  (process-message 42 "W")
  (. Thread sleep 10)
  (is (= [2 4] (get @world 42)))

  (send-off world (fn [state] {42 [3 4]}))
  (process-message 42 "E")
  (. Thread sleep 10)
  (is (= [4 4] (get @world 42)))

  (send-off world (fn [state] {42 [3 4]}))
  (is (= (process-message 42 "E")
         {42 [4 4]})))
