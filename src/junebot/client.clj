(ns junebot.client
  (:use quil.core aleph.object lamina.core aleph.tcp gloss.core)
  (:import [java.util.zip CRC32]
           [java.awt.event KeyEvent]
           [javax.swing JOptionPane])
  (:gen-class))

(defrecord Client [world-state channel])

(defn send-to-server [client msg]
  (enqueue (:channel client) msg))

(defn setup []
  (text-align :center)
  (frame-rate 30))

(def waiting-message "waiting..")

(defn get-color-from-name [name]
  (let [string (.getBytes (or name waiting-message))
        n (.getValue (doto (new CRC32) (.update string)))
        r (mod (* 97 n) 255)
        g (mod (* 133 n) 255)
        b (mod (* 451 n) 255)]
    [r g b]))

(defn draw [client]
  (background 220 230 240)
  (let [size 20
        {:keys [walls players shots]} (:world-state client)]
    (doseq [object (concat walls players)]
      (let [[r g b] (get-color-from-name (:name object))
            [x y] (:coord object)
            screen-x (* size x)
            screen-y (* size y)]
        (if (= :wall (:type object))
          (no-stroke)
          (stroke 1))
        (fill r g b)
        (rect screen-x screen-y size size)
        (fill 0 0 0)
        (when (= :client (:type object))
          (text (or (:name object) waiting-message) (+ screen-x (/ size 2)) (- screen-y 5)))))
    (doseq [{[x y] :position} shots]
      (fill 255 0 0)
      (rect (+ (* size x) (/ size 4)) (+ (* size y) (/ size 4)) (/ size 2) (/ size 2)))))

(defn fire [client]
  (send-to-server client [:fire]))

(defn move [client dir]
  (send-to-server client [:move dir]))

(defn key-pressed [client]
  (condp = (key-code)
    java.awt.event.KeyEvent/VK_RIGHT (move client "E")
    java.awt.event.KeyEvent/VK_LEFT  (move client "W")
    java.awt.event.KeyEvent/VK_UP    (move client "N")
    java.awt.event.KeyEvent/VK_DOWN  (move client "S")
    java.awt.event.KeyEvent/VK_SPACE (fire client)))

(defn create-client [channel]
  (map->Client {:channel channel
                :world-state (atom {:players [], :shots [], :walls []})}))

(def worlds-changes {:new-world       :walls
                      :update-players :players
                      :update-shots   :shots})

(defn update-world [world-state [message value]]
  (assoc world-state (worlds-changes message) value))

(defn process-message [client server-msg]
  (swap! (:world-state client) update-world server-msg))

(defn open-socket [ip port]
  (if-let [socket (.success-value (object-client {:host ip, :port port}) nil)]
    socket
    (do
      (Thread/sleep 1000)
      (println "Failed to connect, retry...")
      (recur ip port))))

(defn -main
  [& [ip, player-name]]
  (let [client (create-client (open-socket (or ip "localhost") 5000))]
    (defsketch junebot
      :title "Junebot"
      :setup setup
      :draw (partial draw client)
      :key-pressed (partial key-pressed client)
      :size [1000 600])
    (send-to-server client {:name (or player-name (JOptionPane/showInputDialog "Please enter yourname"))})
    (map* (partial process-message client) (:channel client))))
