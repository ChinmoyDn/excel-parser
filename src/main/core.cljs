(ns core (:require
          [reagent.core :as r]
          [reagent.dom :as rdom]
          ["read-excel-file" :as xl]))

(defonce file (r/atom nil))
(defonce test-q (r/atom {}))

(defn handle-data [[col-names & rows]]
  (map #(zipmap col-names %) rows))

(defn handle-input-change [file]
  (-> (xl file)
      (.then #(reset! test-q (shuffle (handle-data %))))))


(defn question [{q "QuestionText*"
                 a1 "AnswerOption1*"
                 a2 "AnswerOption2*"
                 a3 "AnswerOption3"
                 a4 "AnswerOption4"
                 ans "CorrectAnswer1*" :as question-map} index]

  [:div.col-6 {:style {:margin-top "10px"
                       :white-space "pre-line"
                       :align-items "flex-start"
                       ;; :display "flex"
                       ;; :flex-direction "column"
                       :flex-wrap "wrap"
                       :flex "1 auto"}}
   [:div {:style {:font-size "13px"
                  :display "flex"
                  :flex "2 1 auto"
                  }}(str (inc index) ". " q)]
   [:ul {:style {:font-size "13px"
                 :list-style-type "none"
                 :padding-left "15px"
                 :line-height "1.4"}}
    [:li (str "a. " a1)]
    [:li (str "b. " a2)]
    [:li (str "c. " a3)]
    [:li (str "d. " a4)]]])

(defn home []
  [:div.container {:style {:page-break-after "always"}}
   [:div
    [:label "Upload Excel file"]
    [:input.noprint {:type "file"
                     :id "file-input"
                     :accept ".xls, .xlsx"
                     :on-change #(handle-input-change (first (.-files (.-target %))))}]

    [:div.row [:h3.col-12  {:style {:text-align "center"}} (get-in @test-q [1 "Metadata*"])]]
    (into [:div.row]
          (for [i (range (count @test-q))]
            ^{:key i} [question (nth @test-q i) i]))]])

(defn main []
  (let [app-node (.getElementById js/document "app")]
    (rdom/render [home] app-node)))


(main)



(comment
  (js/alert "hi")
  (.clear js/console)
  (+ 1 (* 2 5) 3 4)
  (print (nth @test-q 5))
  (-> (xl @file)
      (.then #(reset! test-q (handle-data %))))
  ;;

  (def a [1 2 3 4])
  (def x ["a" "b" "c" "d"])
;;
  (zipmap a x)
  (for [i a] (do (print i) i))
;
  )
