(ns core (:require
          [reagent.core :as r]
          [reagent.dom :as rdom]
          ["read-excel-file" :as xl]))

(defonce test-q (r/atom {}))
(defn handle-data [[col-names & rows]]
  (map #(zipmap col-names %) rows))

(defn handle-input-change [file]
  (-> (xl file)
      (.then
       #(->> %
             handle-data
             (into [])
             (reset! test-q)))))

(defn options
  "render the options"
  [a1 a2 a3 a4]
  [:ol.mt-2.ml-4.break-inside-avoid.break-before-all {:style {:list-style-type "lower-alpha"}}
   [:li a1]
   [:li a2]
   [:li a3]
   [:li a4]])

(defn question
  "render a single quesiton with options"
  [{q   "QuestionText*"
    a1  "AnswerOption1*"
    a2  "AnswerOption2*"
    a3  "AnswerOption3"
    a4  "AnswerOption4"
    ans "CorrectAnswer1*" :as question-map}
   index]
  [:li.mb-8.whitespace-break-spaces.break-inside-avoid.break-before-all.leading-tight
   [:p q]
   [options a1 a2 a3 a4]])

(defn questions [q-list]
  (into [:ol.columns-2.gap-8.list-decimal.hyphens-auto]
        (for [i (range (count q-list))]
          ^{:key i} [question (nth q-list i) i])))

(defn file-uploader
  "file upload"
  []
  [:label "Upload Excel file"]
  [:input {:type      "file"
           :id        "file-input"
           :accept    ".xls, .xlsx"
           :on-change #(handle-input-change (first (.-files (.-target %))))}])

(defn paper-title []
  [:h3
   {:class "text-center mb-4 w-11/12 text-lg font-bold"}
   (get-in @test-q [1 "Metadata*"])])

(defn home []
  [:div.container.max-w-screen-xl.mx-auto.px-8.py-4.text-base
   [:div.print:hidden [file-uploader]]
   [paper-title]
   [questions @test-q]])

(defn main []
  (let [app-node (.getElementById js/document "app")]
    (rdom/render [home] app-node)))

(main)

(comment
  (js/alert "hi")
  (.clear js/console)
  (print (nth @test-q 5))
  (js/console.log "hi there!")
  (print (get (nth @test-q 1) "Metadata*"))
  (print (get-in @test-q [1 "Metadata*"]))
;
  )
