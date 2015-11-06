(ns clojure-xo.core
  (:gen-class)
  (:use [seesaw core graphics color]))

(require '[seesaw.mouse :as mouse])

                                        ; Style for the ticks around the edge of the clock
(def tick-style (style :foreground java.awt.Color/DARK_GRAY
                       :stroke (stroke :width 3 :cap :round)))

(def second-style (style :foreground :blue
                         :background :red
                         :stroke (stroke :width 3 :cap :round)))

(def lbl (label))

(declare game-over?)

(defn set-gfx-vals [c g]
  (def g-ctx g)
  (def cnvs c)
  (def s-width   (.getWidth c))
  (def s-height  (.getHeight c))
  (def m       (- (min s-width s-height) 15))
  (def w-inc (quot s-width 3))
  (def h-inc (quot s-height 3)))


  (def grid {})
  (def turn 0)

(defn draw-x [x y]
  (push g-ctx
        (draw g-ctx (line (* x w-inc) (* y h-inc)
                          (* (inc x) w-inc) (* (inc y) h-inc))
              tick-style
              )
        (draw g-ctx (line (* (inc x) w-inc) (* y h-inc)
                          (* x w-inc) (* (inc y) h-inc))
              tick-style)))

(defn draw-o [x y]
  (push g-ctx
        (draw g-ctx (ellipse (* x w-inc) (* y h-inc)
                             w-inc h-inc)
              tick-style)))

(defn paint-grid [c g]
  (set-gfx-vals c g)
  (push g
        (dotimes [n 9]
          (let [x  (quot n 3) y (mod n 3)]
            (when-let [d (get grid (str x y))]
              (if (even? d)
                (draw-x x y)
                (draw-o x y)))
            (draw   g (rect (* x w-inc) (* y h-inc) 
                            w-inc h-inc) 
                    tick-style)))
        )
 (if (= turn 0)
      (config! lbl :text "Player 1's turn")
      (config! lbl :text "Player 2's turn"))

 (when-let [win (first (game-over?))]
   (config! lbl :text (str "Player " (inc win) " wins"))
  )
  )


(defn game-over? []
  (let [values (for [x (range 3) y (range 3)]
                 (get grid (str x y)))
        tuples (partition 3 values)
        check  #(if (and (apply = %) (every? identity %)) % false)]
    (some identity (concat
                    (map check tuples)
                    (map check [(map first tuples)
                                (map second tuples)
                                (map last tuples)])
                    (map check 
                         (partition 3 (map (fn [x] (nth values x))
                                           [0 4 8 2 4 6])))))
    ))

(defn update-game [x y]
  (if-not (game-over?)
    (if-not (get grid (str x y))
      (do
        (def grid (assoc grid (str x y) (mod turn 2)))
        (def turn (inc turn))
        (repaint! cnvs)))))

(defn new-game [e]
  (def grid {})
  (def turn 0)
  (repaint! cnvs))

(defn -main[]

  (def grid {})
  (def turn 0)

  (let [cvs (canvas :id :canvas :background "#BBBBBB" 
                    :paint paint-grid)]
    (native!)
    (def lbl (label :text "hi"))
    (def new-game-btn (button :text "New Game"
                              :listen [:action new-game]))
    (def content (border-panel
                  :north 
                  (border-panel
                   :west lbl
                   :east new-game-btn)
                  :center cvs
                  :vgap 5 :hgap 5 :border 5))
    (-> (frame 
         :title "XO Game" 
         :width 600 :height 600
         :content content
         :on-close :exit)
        show!)
    (listen cvs :mouse-clicked 
            #(let [[ex ey] (mouse/location %)]
               (let [width       (.getWidth cvs)
                     height  (.getHeight cvs)
                     x-cell       (quot width 3)
                     y-cell       (quot height 3)
                     x            (quot ex x-cell)
                     y            (quot ey y-cell)]
                 (update-game x y)))
            )))
