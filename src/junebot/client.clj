(ns junebot.client
  (:use quil.core)
  (:gen-class)
  (:use aleph.object))

(use 'lamina.core 'aleph.tcp 'gloss.core)
(import java.util.zip.CRC32)
(import java.awt.event.KeyEvent)
(import 'javax.swing.JOptionPane)

;;;(defn -main []
;;;  (tcp-client {:host "localhost",:port 5000,:frame (string :utf-8 :delimiters ["\r\n"])}))

(def world (atom {}))

(defn setup []
  (frame-rate 30))

(defn rects []
  (for [x (range 0 10) y (range 0 10)]
    [x y]))

(defn get-color-from-name [name]
     (def crc (new java.util.zip.CRC32))
     (.update crc (.getBytes name))
     (let [n (.getValue crc)
	   r (mod (* 97 n) 255)
	   g (mod (* 133 n) 255)
	   b (mod (* 451 n) 255)]
       [r g b]))

(defn draw []
  (background 220 230 240)
  (let [size 20]
    (doseq [object @world]
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
        (text-align :center)
        (when (= :client (:type object))
          (text (:name object) (+ screen-x (/ size 2)) (- screen-y 5)))))))

(defn move [client dir]
  (enqueue @client dir)
  (println "moving " dir))

(defn change-world [new-world]
  (prn new-world)
  (reset! world new-world))

(defn show-name-input-box []
  (let [name (JOptionPane/showInputDialog nil "Enter your name:" "name" 1)]
    (println (str "Your name will be " name))))

(defn key-pressed [client]
  (cond (= (key-code) java.awt.event.KeyEvent/VK_RIGHT) (move client "E")
	(= (key-code) java.awt.event.KeyEvent/VK_LEFT) (move client "W")
	(= (key-code) java.awt.event.KeyEvent/VK_UP) (move client "N")
	(= (key-code) java.awt.event.KeyEvent/VK_DOWN) (move client "S")
        (= (key-code) java.awt.event.KeyEvent/VK_N) (show-name-input-box)))

(defn -main []
  (let [client (object-client {:host "localhost", :port 5000})]
    (map* change-world @client)
    (defsketch junebot
      :title "Junebot"
      :setup setup
      :draw draw
      :key-pressed (fn [] (key-pressed client))
      :size [1000 600])))
