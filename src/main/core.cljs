(ns core (:require
          [reagent.core :as r]
          [reagent.dom :as rdom]
          [goog.string :as gstring]
          ["read-excel-file" :as xl]))
;; map of column names in the excel to keys
(def qk {:q-text "QuestionText*"
         :a "AnswerOption1*"
         :b "AnswerOption2*"
         :c  "AnswerOption3"
         :d "AnswerOption4"
         :ans "CorrectAnswer1*"
         })

(def opt-labels [:a :b :c :d])

(enable-console-print!)
(defonce q-hash-map (r/atom {}))

(defonce app-state (r/atom {:preview? true :shuffled? false}))

(defn handle-quesiton-bank! [[col-names & rows]]
  (zipmap (range)
          (map-indexed #(assoc (zipmap col-names %2) :idx %1)
                       rows)))

(defn handle-input-change [file]
  (-> (xl file)
      (.then
       #(->> %
             handle-quesiton-bank!
             (reset! q-hash-map)))))

(defn update-quesiton-and-ans! [state q i key-name]
  (swap! state assoc-in [i (get qk key-name)] q))

(defn handle-edit-question
  [[q idx]]
  (update-quesiton-and-ans! q-hash-map q idx :q-text)
  nil)

(defn handle-edit-answer
  [{:keys [text opt-idx q-idx correct-ans]}]
  (update-quesiton-and-ans! q-hash-map text q-idx (get opt-labels opt-idx))
  (when correct-ans
    (update-quesiton-and-ans! q-hash-map text q-idx (get qk :ans))))

(defn option
  [text q-idx opt-idx correct-ans]
  [:li>p
   {:content-editable (if (:preview? @app-state) "true" "false")
    :on-blur #(handle-edit-answer  {:text (-> % .-target .-innerText) :opt-idx opt-idx :q-idx q-idx :correct-ans? (= text correct-ans)})
    :suppress-content-editable-warning true
    :class (str "py-0.5 " (when (:preview? @app-state) "bg-gray-100 hover:bg-gray-200 p-2"))}
   (str text (when
              (and
               (= text correct-ans)
               (:preview? @app-state)) " *"))])


(defn options
  "render the options"
  [{:keys [idx opts ans]}]
  (js/console.log opts)
  (into  [:ol.mt-2.ml-4.break-inside-avoid.break-before-all {:style {:list-style-type "lower-alpha"}}
          (doall
           (for [opt-idx (range (count opts))]
             ^{:key opt-idx} [option (get opts opt-idx) idx opt-idx ans]))]))

(defn question
  "render a single quesiton with options"
  [{q  (:q-text qk)
    a (:a qk)
    b (:b qk)
    c (:c qk)
    d (:d qk)
    idx :idx
    ans (:ans qk) :as question-map}]
  [:li.mb-8.whitespace-break-spaces.break-inside-avoid.break-before-all.leading-tight
   [:div.relative
    (when (:preview? @app-state)
      [:span.print:hidden.absolute.right-1.top-1.rotate-90
       (gstring/unescapeEntities "&#9998;")])
    [:p {:content-editable (if (:preview? @app-state) "true" "false")
         :on-blur #(handle-edit-question  [(-> % .-target .-innerText) idx])
         :suppress-content-editable-warning true
         :class (str "px-2" (when (:preview? @app-state) "bg-gray-100 hover:bg-gray-200 rounded p-2"))}
     q]]
   [options {:opts [a b c d] :idx idx :ans ans}]])

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
           :placeholder "Upload xls/xlsx file"
           :on-change #(handle-input-change (-> % .-target .-files first))
           :class "text-sm text-gray-900 cursor-pointer focus:outline-none"}])

(defn action-btn
  []
  [:button  {:on-click #(swap! app-state assoc :preview? (not (:preview? @app-state)))
             :class "bg-blue-500 hover:bg-blue-700 text-white py-1 px-4 rounded text-sm"}
   (if (:preview? @app-state) "Shuffle" "Re-Order")])

(defn action-toolbar
  "the top toolbar placeholder for file upload and other actions"
  []
  [:div.my-2.flex.gap-1.5.justify-between.items-center [file-uploader] [action-btn]])

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
  (js/console.log @q-hash-map)
  (:preview? @app-state)
  (print @app-state)

;
  )


