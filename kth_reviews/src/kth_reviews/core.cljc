(ns kth_reviews.core
  (:require [ysera.test :refer [deftest is is-not is=]]))

(defn create-state
  {:test (fn []
           (is= (create-state ["##  #"
                               "  ####"
                               "##  "])
                {:cells #{[0 0] [1 0] [4 0]
                          [2 1] [3 1] [4 1] [5 1]
                          [0 2] [1 2]}}))}
  [strings]
  {:cells (->> strings
               (map-indexed
                 (fn [y rows]
                   (map-indexed (fn [x character]
                                  (when (= character \#)
                                    [x y]))
                                rows)))
               (apply concat)
               (remove nil?)
               (set))})

(defn abs
  {:test (fn []
           (is= (abs 2) 2)
           (is= (abs 0) 0)
           (is= (abs -4) 4))}
  [x]
  (if (pos? x) x (- x)))


(defn distance
  {:test (fn []
           (is= (distance [4 4] [4 4]) 0)
           (is= (distance [0 0] [1 0]) 1)
           (is= (distance [1 1] [3 3]) 2)
           (is= (distance [-1 0] [1 1]) 2)
           (is= (distance [4 4] [5 -4]) 8))}
  [cell-1 cell-2]
  (->> (map - cell-1 cell-2)
       (map abs)
       (apply max)))


(defn neighbors
  {:test (fn []
           (is (neighbors [4 4] [5 4]))
           (is (neighbors [0 0] [1 0]))
           (is-not (neighbors [4 4] [4 4]))
           (is-not (neighbors [1 1] [3 3]))
           (is-not (neighbors [-1 0] [1 1])))}
  [cell-1 cell-2]
  (= (distance cell-1 cell-2) 1))

(defn alive?
  {:test (fn []
           (is (-> (create-state ["# "])
                   (alive? [0 0])))
           (is-not (-> (create-state ["# "])
                       (alive? [1 0]))))}
  [state cell]
  (contains? (:cells state) cell))

(defn get-neighbors
  {:test (fn []
           (is= (get-neighbors [2 0])
                #{[2 -1] [1 0] [1 1] [3 0] [3 -1] [1 -1] [3 1] [2 1]}))}
  [cell]
  (let [directions (for [x (range -1 2)
                         y (range -1 2)
                         :when (not= [x y] [0 0])]
                     [x y])]
    (->> directions
         (map (fn [d] (map + d cell)))
         (set))))

(defn get-live-neighbors
  {:test (fn []
           (is= (-> (create-state ["###"
                                   "  #"
                                   "# #"])
                    (get-live-neighbors [2 0]))
                #{[1 0] [2 1]}))}
  [state cell]
  (->> (get-neighbors cell)
       (filter (fn [c] (alive? state c)))
       (set)))

(defn alive-in-the-next-generation?
  {:test (fn []
           ; Any dead cell with exactly three live neighbours becomes a live cell, as if by reproduction.
           (is (-> (create-state ["###"])
                   (alive-in-the-next-generation? [1 1])))
           ; Any live cell with two or three live neighbours lives on to the next generation.
           (is (-> (create-state ["###"])
                   (alive-in-the-next-generation? [1 0])))
           (is (-> (create-state ["###"
                                  " #"])
                   (alive-in-the-next-generation? [1 0])))
           ; Any live cell with more than three live neighbours dies, as if by overpopulation.
           (is-not (-> (create-state ["###"
                                      "##"])
                       (alive-in-the-next-generation? [1 1])))
           ; Any live cell with fewer than two live neighbours dies, as if by underpopulation.
           (is-not (-> (create-state ["###"])
                       (alive-in-the-next-generation? [0 0]))))}
  [state cell]
  (let [live-neighbors (get-live-neighbors state cell)
        number-of-live-neighbors (count live-neighbors)]
    (or (and (alive? state cell)
             (<= 2 number-of-live-neighbors 3))
        (and (not (alive? state cell))
             (= number-of-live-neighbors 3)))))


(defn tick
  {:test (fn []
           (is= (-> (create-state ["###"])
                    (tick)
                    (:cells))
                #{[1 -1] [1 0] [1 1]}))}
  [state]
  (let [interesting-cells (->> (clojure.set/union (:cells state)
                                                  (->> (:cells state)
                                                       (map get-neighbors)
                                                       (apply clojure.set/union))))]
    (assoc state :cells (->> interesting-cells
                             (filter (fn [c] (alive-in-the-next-generation? state c)))
                             (set)))))

(defn toggle
  {:test (fn []
           (is= (-> (create-state ["#"])
                    (toggle [1 0])
                    (:cells))
                #{[0 0] [1 0]})
           (is= (-> (create-state ["##"])
                    (toggle [1 0])
                    (:cells))
                #{[0 0]}))}
  [state cell]
  (if (alive? state cell)
    (update state :cells (fn [cells]
                           (->> cells
                                (remove (fn [c] (= c cell)))
                                (set))))
    (update state :cells conj cell)))


