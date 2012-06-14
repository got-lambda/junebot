(ns junebot.client)
(use 'lamina.core 'aleph.tcp 'gloss.core)

(defn -main []
  (tcp-client {:host "localhost",:port 5000,:frame (string :utf-8 :delimiters ["\r\n"])}))
