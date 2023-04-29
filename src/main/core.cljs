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
              (reset! test-q)))))

(defn options
  "render the options"
  [a1 a2 a3 a4]
  [:ol.mt-2.leading-tight.break-inside-avoid.break-before-all {:style {:list-style-type "lower-alpha"}}
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
  [:li.mb-8
   [:p.whitespace-break-spaces.leading-snug.break-inside-avoid.break-before-all q]
   [options a1 a2 a3 a4]])


(defn questions [q-list]
  (into [:ol.columns-2.gap-8.list-decimal]
        (for [i (range (count q-list))]
          ^{:key i} [question (nth q-list i) i])))

(defn file-uploader
  "file upload"
  []
  [:label.print:hidden "Upload Excel file"]
  [:input.print:hidden {:type      "file"
                   :id        "file-input"
                   :accept    ".xls, .xlsx"
                   :on-change #(handle-input-change (first (.-files (.-target %))))}])

(defn paper-title []
  [:h3.text-center.text-lg.font-bold (get-in @test-q [1 "Metadata*"])])

(defn home []
  [:div.container.max-w-screen-xl.mx-auto.p-4.text-base
   [file-uploader]
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
;
  )
