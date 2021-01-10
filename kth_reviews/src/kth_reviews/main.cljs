(ns ^:figwheel-hooks kth_reviews.main
  (:require [reagent.dom :as reagent-dom]
            [kth_reviews.dom :as dom]
            [kth_reviews.core :as core]
            [kth_reviews.view.app :refer [app-component]]))

(defonce state-atom (atom nil))

(defn handle-event
  [{name :name data :data}]
  ; (println "Event: " name data)
  (cond (= name :tick)
        (swap! state-atom update :game-states
               (fn [game-states]
                 (conj game-states (core/tick (first game-states)))))

        (= name :toggle)
        (let [cell data]
          (swap! state-atom update :game-states
                 (fn [game-states]
                   (conj game-states (core/toggle (first game-states) cell)))))

        (= name :undo)
        (let [game-states (:game-states (deref state-atom))]
          (when (>= (count game-states) 2)
            (swap! state-atom update :game-states
                   (fn [game-states]
                     (drop 1 game-states)))))

        :else
        (println "Nothing here")))

(defn render! [state]
  (reagent-dom/render [app-component {:state         state
                                      :trigger-event handle-event}]
                      (dom/get-app-element)))


(when-not (deref state-atom)

  (add-watch state-atom
             :change
             (fn [_ _ old-state new-state]
               (when (not= old-state new-state)
                 (render! new-state))))

  (js/addEventListener "resize"
                       (fn []
                         (swap! state-atom
                                assoc
                                :screen
                                (dom/get-screen-size))))

  (reset! state-atom {:game-states (list (core/create-state ["### ###"
                                                             "  ## # "
                                                             "## ### "
                                                             "  #####"]))
                      :screen      (dom/get-screen-size)}))

(defn on-js-reload
  {:after-load true}
  []
  (render! (deref state-atom)))
