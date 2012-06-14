(ns junebot.core)
(use 'lamina.core 'aleph.http)

(def broadcast-channel (channel))

(defn chat-handler [ch info]
  (receive ch
           (fn [name]
             (siphon (map* #(str name ": " %) ch) broadcast-channel)
             (siphon broadcast-channel ch))))

(defn -main []
  (start-http-server chat-handler {:port 5000 :websocket true}))
