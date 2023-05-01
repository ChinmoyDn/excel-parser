(ns core (:require
          [reagent.core :as r]
          [reagent.dom :as rdom]
          [goog.string :as gstring]
          ["read-excel-file" :as xl]))
;; map of column names in the excel to keys
(def qk {:q-text "QuestionText*"
         :a1 "AnswerOption1*"
         :a2 "AnswerOption2*"
         :a3  "AnswerOption3"
         :a4 "AnswerOption4"
         :ans "CorrectAnswer1*"
         })

(enable-console-print!)
(defonce q-hash-map (r/atom {}))

(defonce app-state (r/atom {:preview? true :shuffled? false}))

(defn handle-data-map [[col-names & rows]]
  (zipmap (range)
          (map-indexed #(assoc (zipmap col-names %2) :idx %1)
                       rows)))

(defn handle-input-change [file]
  (-> (xl file)
      (.then
       #(->> %
             handle-data-map
             (reset! q-hash-map)))))

(defn update-question! [state q i]
  (swap! state assoc-in [i (:q-text qk)] q))

(defn handle-edit-question
  [[q idx]]
  (update-question! q-hash-map q  idx)
   nil)

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
  [{q  (:q-text qk)
    a1 (:a1 qk)
    a2 (:a2 qk)
    a3 (:a3 qk)
    a4 (:a4 qk)
    idx :idx
    ans (:ans qk) :as question-map}]
  [:li.mb-8.whitespace-break-spaces.break-inside-avoid.break-before-all.leading-tight
   [:div.relative
    (when (:preview? @app-state)
      [:span.print-hidden.absolute.right-1.top-1.rotate-90
       (gstring/unescapeEntities "&#9998;")])
    [:p {:content-editable (if (:preview? @app-state) "true" "false")
         :on-blur #(handle-edit-question  [(-> % .-target .-innerText) idx])
         :suppress-content-editable-warning true
         :class (str "p-2 " (when (:preview? @app-state) "bg-gray-100 hover:bg-gray-200 rounded"))}
     q]]
   [options a1 a2 a3 a4]])

(defn questions [q-list]
  (into [:ol.columns-2.gap-8.list-decimal.hyphens-auto]
        (doall
         (for [i (range (count q-list))]
           ^{:key i} [question (nth q-list i) i]))))

(defn file-uploader
  "file upload"
  []
  [:label "Upload Excel file"]
  [:input {:type      "file"
           :id        "file-input"
           :accept    ".xls, .xlsx"
           :on-change #(handle-input-change (first (.-files (.-target %))))}])

(defn action-btn
  []
  [:button  {:on-click #(swap! app-state assoc :preview? (not (:preview? @app-state)))
             :class "bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded"}
   (if (:preview? @app-state) "Shuffle" "Re-Order")])

(defn action-toolbar
  "the top toolbar placeholder for file upload and other actions"
  []
  [:div [file-uploader] [action-btn]])

(defn paper-title []
  [:h3
   {:class "text-center mb-4 w-11/12 text-lg font-bold"}
   (get-in @q-hash-map [1 "Metadata*"])])

(defn home []
  (let [q-list (vals @q-hash-map)]
    [:div.container.max-w-screen-xl.mx-auto.px-8.py-4.text-base
     [:div.print:hidden [action-toolbar]]
     [paper-title]
     [questions (if-not (:preview? @app-state) (shuffle q-list) q-list)]]))

(defn main []
  (let [app-node (.getElementById js/document "app")]
    (rdom/render [home] app-node)))

(main)

(comment
  (js/alert "hi")
  (.clear js/console)
  (print @q-hash-map)
  (:preview? @app-state)
  (print @app-state)
;
  )


