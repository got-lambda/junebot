(ns junebot.client
  (:use quil.core aleph.object lamina.core aleph.tcp gloss.core)
  (:import [java.util.zip CRC32]
           [java.awt.event KeyEvent]
           [javax.swing JOptionPane])
  (:gen-class))

(def world (atom []))
(def shots (atom []))

(defn setup []
  (text-align :center)
  (frame-rate 30))

(def waiting-message "waiting..")

(defn get-color-from-name [name]
  (def crc (new CRC32))
  (.update crc (.getBytes (or name waiting-message)))
  (let [n (.getValue crc)
        r (mod (* 97 n) 255)
        g (mod (* 133 n) 255)
        b (mod (* 451 n) 255)]
    [r g b]))

(defn draw []
  (background 220 230 240)
  (let [size 20]
    (doseq [object @world]
      (prn object)
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
    (doseq [{[x y] :position} @shots]
      (fill 255 0 0)
      (rect (+ (* size x) (/ size 4)) (+ (* size y) (/ size 4)) (/ size 2) (/ size 2)))))

(defn move [client dir]
  (enqueue @client [:move dir])
  (println "moving " dir))

(defmulti change-world first)

(defmethod change-world :new-world
  [[_ new-world]]
  (reset! world new-world))

(defmethod change-world :update-players
  [[_ & new-players]]
  (let [world-without-players
        (remove (fn [object] (= :client (:type object))) @world)
        player-data new-players]
    (reset! world (concat world-without-players player-data))))

(defmethod change-world :update-shots
  [[_ new-shots]]
  (reset! shots new-shots))

(defn fire
  [client]
  (enqueue @client [:fire]))

(defn key-pressed [client]
  (cond
    (= (key-code) java.awt.event.KeyEvent/VK_RIGHT) (move client "E")
    (= (key-code) java.awt.event.KeyEvent/VK_LEFT) (move client "W")
    (= (key-code) java.awt.event.KeyEvent/VK_UP) (move client "N")
    (= (key-code) java.awt.event.KeyEvent/VK_DOWN) (move client "S")
    (= (key-code) java.awt.event.KeyEvent/VK_SPACE) (fire client)))

(defn -main
  [& [ip, player-name]]
  (let [client (object-client {:host (or ip "localhost"), :port 5000})]
    (defsketch junebot
      :title "Junebot"
      :setup setup
      :draw draw
      :key-pressed (fn [] (key-pressed client))
      :size [1000 600])
    (enqueue @client {:name (or player-name (JOptionPane/showInputDialog "Please enter yourname"))})
    (map* change-world @client)))
