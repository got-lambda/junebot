(ns junebot.server)
(use 'lamina.core 'aleph.object 'gloss.core)

(def world (atom {}))

(def player-serial (atom 0))

(defn number-of-players []
  (count (keys @world)))

(defn new-player-serial []
  (swap! player-serial inc))

(def directions
  {"N" [0 -1] "S" [0 1] "E" [1 0] "W" [-1 0]})

(defn positions-taken [state]
  (let [positions (set (map :coord (vals state)))]
    (prn (str "Positions taken: " positions))
    positions))

(defn free-position? [state pos]
  (not (get (positions-taken state) pos)))

(defn move [state id movement]
  (let [new-pos (mapv + (get-in state [id :coord]) movement)]
    (if (free-position? state new-pos)
      (assoc-in state [id :coord] new-pos)
      state)))

(defn process-message [id message]
  (let [movement (get directions message)]
    (prn message)
    (swap! world move id movement)))

(def broadcast-channel (channel))

(defn new-client [ch message]
  (let [id (new-player-serial)]
    (prn message)
    (swap! world assoc id {:name (str "player " id) :coord [1 1]})
    (siphon (map* #(process-message id %) ch) broadcast-channel)
    (siphon broadcast-channel ch)))

(defn junehandler [ch info]
  (receive ch #(new-client ch %)))

(defn -main []
  (start-object-server junehandler {:port 5000}))
