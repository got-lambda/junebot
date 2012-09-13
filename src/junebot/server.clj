(ns junebot.server)
(use 'lamina.core 'aleph.object 'gloss.core)

(def world
  (remove nil?
          (for [x (range 50) y (range 30)]
            (when (or (= x 0)
                      (= x 49)
                      (= y 0)
                      (= y 29)
                      (= 0 (rand-int 5)))
              {:type :wall, :name "wall", :coord [x y]}))))

(def players (atom {}))

(def player-serial (atom 0))

(defn number-of-players []
  (count (keys @world)))

(defn new-player-serial []
  (swap! player-serial inc))

(def directions
  {"N" [0 -1] "S" [0 1] "E" [1 0] "W" [-1 0]})

(defn positions-taken [state]
  (set (map :coord (concat world (vals state)))))

(defn free-position? [state pos]
  (not (get (positions-taken state) pos)))

(defn move [state id movement]
  (let [new-pos (mapv + (get-in state [id :coord]) movement)]
    (if (free-position? state new-pos)
      (assoc-in state [id :coord] new-pos)
      state)))

(defmulti process-message
  (fn [id [message-type]]
    (prn id message-type)
    message-type))

(defmethod process-message :name
  [id [_ player-name]]
  (swap! players assoc-in [id :player-name] player-name)
  (concat world @players))

(defmethod process-message :move
  [id [_ message]]
  (let [movement (get directions message)]
    (prn message)
    (cons :update-players (vals (swap! players move id movement)))))

(def broadcast-channel (channel))

(defn disconnect-client [id]
  (swap! players dissoc id))

(defn new-client [ch message]
  (let [id (new-player-serial)]
    (on-closed ch (fn [] (disconnect-client id)))
    (swap! players assoc id {:type :client :name (message :name) :coord [1 1]})
    (siphon (map* #(process-message id %) ch) broadcast-channel)
    (siphon broadcast-channel ch)
    (enqueue ch [:new-world world])))

(defn junehandler [ch info]
  (receive ch #(new-client ch %)))

(defn -main []
  (start-object-server junehandler {:port 5000}))
