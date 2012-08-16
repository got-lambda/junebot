(ns junebot.client
  (:use quil.core)
  (:gen-class)
  (:use aleph.object))

(use 'lamina.core 'aleph.tcp 'gloss.core)
(import java.util.zip.CRC32)
(import java.awt.event.KeyEvent)

;;;(defn -main []
;;;  (tcp-client {:host "localhost",:port 5000,:frame (string :utf-8 :delimiters ["\r\n"])}))

(def world (atom
	    [{ :name "player1" :coord [2 5] }
	     { :name "player2" :coord [10 10] }
	     { :name "player3" :coord [30 3] }]))

(def my-name "player2")

(defn setup []
  (frame-rate 5))

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
    (doseq [player @world]
      (let [[r g b] (get-color-from-name (:name player))]
	(fill r g b))
      (let [[x y] (:coord player)]
	    (rect (* size x) (* size y) size size)))))

(defn move [client dir]
  (enqueue @client dir)
  (println "moving " dir))

(defn change-world [new-world]
  (reset! world new-world))

(defn key-pressed [client]
  (cond (= (key-code) java.awt.event.KeyEvent/VK_RIGHT) (move client "E")
	(= (key-code) java.awt.event.KeyEvent/VK_LEFT) (move client "W")
	(= (key-code) java.awt.event.KeyEvent/VK_UP) (move client "N")
	(= (key-code) java.awt.event.KeyEvent/VK_DOWN) (move client "S")))

(defn -main []
  (let [client (object-client {:host "localhost", :port 5000})]
    (defsketch junebot
      :title "Junebot"
      :setup setup
      :draw draw
      :key-pressed (fn [] (key-pressed client))
      :size [1000 600])))
