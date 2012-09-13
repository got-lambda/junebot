(ns junebot.map
  (:use [junebot.client :only [world]] :reload))


(defn free-position? [level pos]
  (not (get (set level) pos)))

(defn old-random-map []
  (remove nil?
          (for [x (range 50) y (range 30)]
            (when (or (= x 0)
                      (= x 49)
                      (= y 0)
                      (= y 29)
                      (= 0 (rand-int 30)))
              [x y]))))

(defn random-map []
  (remove nil?
          (for [x (range 50) y (range 30)]
            (if (= 0 (rand-int 25)) ;(rem (+ x y) 20))
              [x y]))))

(defn add-tiles [[x1 y1] [x2 y2]]
  [(+ x1 x2) (+ y1 y2)])

(def dirs
  (for [x [-1 0 1]
        y [-1 0 1]]
        [x y]))

(defn clog-single [level tile]
    (for [dir dirs]
      (let [new-pos (add-tiles tile dir)]
        (if (free-position? level new-pos)
          new-pos))))

(defn clog [level]
  (vec (set (remove nil? 
      (mapcat (fn [tile] (clog-single level tile)) level)))))

(defn unclog-single [level tile]
    (for [dir dirs :when (not (= [0 0] dir))] 
      (let [new-pos (add-tiles tile dir)]
        (if (not (free-position? level new-pos))
          tile))))

(defn unclog [level]
  (vec (set (remove nil? (mapcat (fn [tile] (unclog-single level tile)) level)))))

(defn add-type-stuff [world]
  (map (fn [tile] {:type :wall, :name "wall", :coord tile}) world))

(defn set-world []
   (reset! world (-> (random-map)
                     clog
                     clog
                     clog
                     unclog
                     add-type-stuff))
   (println "Tile count: " (count @world)))