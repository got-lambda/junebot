(ns junebot.server)
(use 'lamina.core 'aleph.http)

(def world (agent {}))

(def player-serial (atom 0))

(defn number-of-players []
  (count (keys @world)))

(defn new-player-serial []
  (swap! player-serial inc))

(def directions
  {"N" [0 -1] "S" [0 1] "E" [1 0] "W" [-1 0]})

(defn process-message [id message]
  (let [move (get directions message)]
    (deref (send-off world
                     (fn [state]
                       (update-in state [id] #(vec (map + % move))))))))

(def broadcast-channel (channel))

(defn new-client [ch message]
  (let [id (new-player-serial)]
    (send-off world assoc id [1 1])
    (siphon (map* #(process-message id %) ch) broadcast-channel)
    (siphon broadcast-channel ch)))

(defn junehandler [ch info]
  (receive ch #(new-client ch %)))

(defn -main []
  (start-http-server junehandler {:port 5000}))
