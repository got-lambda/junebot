(ns junebot.server)
(use 'lamina.core 'aleph.object 'gloss.core)

(defn init-walls []
  (for [x (range 50)
        y (range 30)
        :when (or (= x 0)
                  (= x 49)
                  (= y 0)
                  (= y 29)
                  (= 0 (rand-int 5)))]
    {:type :wall, :name "wall", :coord [x y]}))

(def directions
  {"N" [0 -1], "S" [0 1], "E" [1 0], "W" [-1 0]})

(defn positions-taken [state]
  (set (map :coord state)))

(defn free-position? [state pos]
  (not (get (positions-taken state) pos)))

(defn calculate-position
  [player movement]
  (mapv + (:coord player) movement))

(defn move [{:keys [players walls] :as state} id movement]
  (let [player (players id)
        new-pos (calculate-position player movement)]
    (if (free-position? (concat (vals players) walls) new-pos)
      (assoc-in state [:players id]
                (-> player
                    (assoc :coord new-pos)
                    (assoc :direction movement)))
      state)))

(defmulti process-message
  (fn [state id [message-type]]
    message-type))

(defn add-shot
  [{:keys [walls players shots] :as state} position]
  (if (free-position? (concat walls (vals players)) position)
    (assoc state :shots (conj shots {:position position}))
    state))

(defmethod process-message :fire
  [{:keys [players] :as state} id _]
  (let [players (:players state)
        position (calculate-position (players id) (get-in players [id :direction]))
        new-state (add-shot state position)]
    {:new-state new-state
     :send-back [:update-shots (:shots new-state)]}))

(defmethod process-message :move
  [{:keys [players walls] :as state} id [_ message]]
  (let [movement (get directions message)
        new-state (move state id movement)]
    {:new-state new-state
     :send-back [:update-players (vals (:players new-state))]}))

(defrecord Server [world-state broadcast-channel])

(defn create-server []
  (map->Server
   {:broadcast-channel (channel),
    :world-state (atom {:id-counter 0
                        :walls (init-walls),
                        :players {},
                        :shots []})}))

(def ^:dynamic *send-to-client* nil)

(defn update-and-send [{:keys [broadcast-channel world-state]} id client-message]
  (binding [*send-to-client* nil]
    (swap! world-state
           (fn [state]
             (let [{:keys [send-back new-state]}
                   (process-message state id client-message)]
               (set! *send-to-client* send-back)
               new-state)))
    (enqueue broadcast-channel *send-to-client*)))

(defn new-client [{:keys [world-state broadcast-channel] :as server} ch message]
  (let [id (swap! world-state update-in [:id-counter] inc)]
    (on-closed ch (fn [] (swap! world-state update-in [:players] dissoc id)))
    (swap! (:world-state server) assoc-in [:players id]
           {:type :client, :name (message :name), :coord [1 1]})
    (map* (partial update-and-send server id) ch)
    (siphon broadcast-channel ch)
    (enqueue ch [:new-world (:walls @world-state)])))

(defn junehandler [ch info]
  (let [server (create-server)]
    (receive ch #(new-client server ch %))))

(defn -main []
  (start-object-server junehandler {:port 5000}))
