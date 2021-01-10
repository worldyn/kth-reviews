(ns kth_reviews.dom)

(defn get-app-element
  []
  (js/document.getElementById "app"))

(defn get-screen-size
  []
  (let [bounding-client-rect (js/document.body.getBoundingClientRect)]
    {:height (.-height bounding-client-rect)
     :width  (.-width bounding-client-rect)}))

