(ns kth_reviews.view.app
  (:require [reagent.core]
            [kth_reviews.core :as core]))

(defn app-component
  [{state         :state
    trigger-event :trigger-event}]

  [:div
   (let [game-states (:game-states state)
         game-state (first game-states)
         size 12
         app-width (get-in state [:screen :width])
         border-width 1
         cell-width (- (/ app-width size)
                       (* 2 border-width))]
     [:div
      [:h1 "yo!"]
      [:div
       (->> (for [x (range size)
                  y (range size)]
              [y x])
            (partition size)
            (map-indexed (fn [y row]
                           [:div {:key   y
                                  :style {:display "flex"}}
                            (->> row
                                 (map (fn [cell]
                                        [:div
                                         {:key   (str cell)
                                          :on-click (fn [] (trigger-event {:name :toggle :data cell}))
                                          :style {:position         "relative"
                                                  :width            cell-width
                                                  :height           cell-width
                                                  :background-color "lightgray"
                                                  :border           (str border-width "px solid white")}}
                                         (let [alive (core/alive? game-state cell)]
                                           [:div {:style {:position         "absolute"
                                                          :width            "100%"
                                                          :height           "100%"
                                                          :background-color "rebeccapurple"
                                                          :border-radius    "50%"
                                                          :transition       "transform 500ms"
                                                          :transform        (str "scale(" (if alive 0.9 0) ")")}}])])))])))]
      [:div {:style {:margin-top "10px"}}
       [:button {:on-click (fn []
                             (trigger-event {:name :tick}))}
        "TICK"]
       [:button {:on-click (fn []
                             (trigger-event {:name :undo}))}
        "UNDO"]
       [:div (str "Number of states: " (count game-states))]]])
   ])

