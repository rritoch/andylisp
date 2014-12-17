(ns andylisp.regex)

(defn re-exec
  "Returns a vector of start and end vectors that match the re for the string s starting at offset"
  ([re s offset]
    (let [m (.matcher re (subs s offset))]
      (loop [a []]
        (if (not (.find m))
            (if (> (count a) 0)
                a)
            (recur (conj a [(.start m) (.end m)]))))))
  ([re s]
    (re-exec re s 0)))

(defn re-ungreedy
  "Make regular expression non-greedy"
  [re]
  (loop [s (.toString re) lc nil lq nil bc 0 cc 0 q  nil r ""]
    (if (empty? s)
        (re-pattern r)
        (recur (rest s)
               (first s)
               q
               (cond (and (not q)
                          (= \[ (first s)))
                     (inc bc)
                     (and (not q)
                          (= \] (first s)))
                     (dec bc)
                     :else
                     bc)
               (cond (and (not q)
                          (= \{ (first s)))
                     (inc cc)
                     (and (not q)
                          (= \} (first s)))
                     (dec cc)
                     :else
                     cc)
               (cond (and (not q)
                          (= \\ (first s)))
                     \\
                     (and (= q \\)
                          (= \Q (first s)))
                     \Q
                     (and (= q \Q)
                          (= lc \\)
                          (= \E (first s)))
                     nil
                     (= q \Q)
                     \Q
                     :else
                     nil)
               (cond (and (or (= \* lc)
                              (= \+ lc))
                          (not lq)
                          (not= \? (first s))
                          (not q)
                          (< bc 1)
                          (< cc 1))
                     (str r "?" (first s))
                     :else
                     (str r (first s)))))))

(defn re-arr-match-first
  "If there is a match, returns a vector of the expression index and the string
  start and end index of the first match, otherwise returns nil."
  ([rx-list s offset]
     (let [r (reduce (fn [& args]
                       (if (> (count args)
                              1)
                           (cond (not (second (second args)))
                                 (first args)
                                 (not (second (first args)))
                                 (second args)
                                 :else
                                 (if (< (first (first (second (second args))))
                                        (first (first (second (first args)))))
                                     (second args)
                                     (first args)))))
                     (map-indexed (partial (fn [s offset idx re]
                                               [idx (re-exec re s offset)])
                                  s
                                  offset)
                                  rx-list))]
          (if (first (second r))
              [(first r) 
               (+ offset (first (first (second r))))
               (+ offset (second (first (second r))))])))
  ([rx-list s]
    (re-arr-match-first rx-list s 0)))


(defn re-assoc
  "Associate pattern matches to tokens"
  ([s pat-arr tok-arr def]
    (let [len (count s)
          rxarr (mapv re-ungreedy
                     (if (> (count pat-arr)
                            (count tok-arr))
                         (subvec (vec pat-arr)
                                 0
                                 (count tok-arr))
                         pat-arr))]
        (loop [sm [] 
               tm [] 
               lm (re-arr-match-first rxarr s 0) 
               offset 0]
          (if (>= offset len)
              [sm tm]
              (recur (cond (not lm)
                           (conj sm (subs s offset))
                           (> (second lm)
                              offset)
                           (conj (conj sm (subs s offset (second lm)))
                                 (subs s (second lm) (last lm)))
                           :else
                           (conj sm (subs s (second lm) (last lm))))
                     (cond (not lm)
                           (conj tm def)
                           (> (second lm)
                              offset)
                           (conj (conj tm def)
                                 (nth tok-arr (first lm)))
                           :else
                           (conj tm
                             (if lm
                                 (nth tok-arr (first lm))
                                 def)))
                     (re-arr-match-first rxarr 
                                         s 
                                         (if lm
                                             (last lm)
                                             (inc offset)))
                     (if lm
                         (last lm)
                         len))))))
  ([s pat-arr tok-arr]
    (re-assoc s pat-arr tok-arr nil)))