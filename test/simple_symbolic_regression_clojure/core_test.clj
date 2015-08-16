(ns simple-symbolic-regression-clojure.core-test
  (:use midje.sweet)
  (:use [simple-symbolic-regression-clojure.core])
  )

(fact "it gets a empty program and an empty binding it returns nil with an empty stack"
      (interpret [] {}) => {:result nil, :stack []})

(fact "the interpreter gets script [7] and no binding it returns 7 with stack [7]"
      (interpret [7] {}) => {:result 7, :stack [7]})

(fact "process-token puts the token onto the stack if its a literal"
      (process-token [] 8) => (just [8])
      (peek (process-token [1 2 3] 4)) => 4
      (process-token [1 2 3] 4) => (just [1 2 3 4]))

(fact "run-script should process a list of literals by pushing them into the stack"
      (run-script [] {}) => []
      (run-script [1 2 3] {}) => [1 2 3])

(facts "about process-token with +"
       (fact "process-token with + and an empty stack"
             (process-token [] +) => [])
       (fact "process-token with + and only one number"
             (process-token [7] +) => [7])
       (fact "process-token with + and two numbers"
             (process-token [3 5] +) => [8]
             (process-token [1.2 -3.4] +) => [-2.2]
             (process-token [2/3 5/6] +) => [3/2]
             (process-token [0 0] +) => [0]
             )
       )

(facts "about process-token with -"
       (fact "process-token with - and an empty stack"
             (process-token [] -) => [])
       (fact "process-token with - and only one number"
             (process-token [7] -) => [7])
       (fact "process-token with - and two numbers"
             (process-token [3 5] -) => [-2]
             (process-token [1.2 -3.4] -) => [4.6]
             (process-token [2/3 5/6] -) => [-1/6]
             (process-token [0 0] -) => [0]
             )
       )

(facts "about process-token with /"
       (fact "process-token with / and an empty stack"
             (process-token [] /) => [])
       (fact "process-token with / and only one number"
             (process-token [7] /) => [7])
       (fact "process-token with / and two numbers"
             (process-token [3 5] /) => [3/5]
             (process-token [1.5 -0.5] /) => [-3.0]
             (process-token [2/3 5/6] /) => [4/5]
             )
       (fact "process-token with / and 0 as second argument"
             (process-token [3 0] /) => [3 0]
             (process-token [3 0.0] /) => [3 0.0]
             (process-token [1/2 0/5] /) => [1/2 0]
             (process-token [0.0 0.0] /) => [0.0 0.0]
             )
       )

(facts "about process-token with *"
       (fact "process-token with * and an empty stack"
             (process-token [] *) => [])
       (fact "process-token with * and only one number"
             (process-token [7] *) => [7])
       (fact "process-token with * and two numbers"
             (process-token [3 5] *) => [15]
             (process-token [1.2 -3.4] *) => [-4.08]
             (process-token [2/3 5/6] *) => [5/9]
             (process-token [0 0] *) => [0]
             )
       (fact "process-token with * and really large arguments"
             (process-token [1111111111111 22222222222222222222222222222] *)
               => [24691358024688888888888888888641975308642N]
             (process-token [111111 22222222222222222] *)
               => [2469133333333333308642N]
             )
       )

(facts "about process-token with a variable"
       (fact "process-token with an undefined variable"
             (process-token [3 4 7] :x) => [3 4 7])
       (fact "process-token with a defined variable"
             (process-token {:x 33, :y 47} [3 4 7] :x) => [3 4 7 33])
       )

;; interpreter

(fact "the interpreter does addition"
      (interpret [1 2 +] {}) => {:result 3, :stack [3]}
      (interpret [1 +] {}) => {:result 1, :stack [1]}
      (interpret [+] {}) => {:result nil, :stack []}
      (interpret [1 2 3 4 +] {}) => {:result 7, :stack [1 2 7]}
      (interpret [1 2 3 + 4 5 7 +] {}) => {:result 12, :stack [1 5 4 12]}
      (interpret [1 2 3 4 5 + + +] {}) => {:result 14, :stack [1 14]}
      )

(fact "the interpreter does subtraction"
      (interpret [1 2 -] {}) => {:result -1, :stack [-1]}
      (interpret [1 -] {}) => {:result 1, :stack [1]}
      (interpret [-] {}) => {:result nil, :stack []}
      (interpret [1 2 3 4 -] {}) => {:result -1, :stack [1 2 -1]}
      (interpret [1 2 3 - 4 5 7 -] {}) => {:result -2, :stack [1 -1 4 -2]}
      (interpret [1 2 3 4 5 - - -] {}) => {:result -2, :stack [1 -2]}
      )

(fact "the interpreter does division"
      (interpret [1 2 /] {}) => {:result 1/2, :stack [1/2]}
      (interpret [6.5 0.125 /] {}) => {:result 52.0, :stack [52.0]}
      (interpret [1 /] {}) => {:result 1, :stack [1]}
      (interpret [/] {}) => {:result nil, :stack []}
      (interpret [1 2 3 4 /] {}) => {:result 3/4, :stack [1 2 3/4]}
      (interpret [1 2 3 / 4 5 7 /] {}) => {:result 5/7, :stack [1 2/3 4 5/7]}
      (interpret [1 2 3 4 5 / / /] {}) => {:result (/ 2 (/ 3 (/ 4 5))),
                                           :stack [1 (/ 2 (/ 3 (/ 4 5)))]}
      (interpret [1.0 2.0 3.0 4.0 5.0 / / /] {}) => {:result (/ 2.0 (/ 3.0 (/ 4.0 5.0))),
                                                     :stack [1.0 (/ 2.0 (/ 3.0 (/ 4.0 5.0)))]}
      )

(fact "the interpreter does multiplication"
      (interpret [3 2 *] {}) => {:result 6, :stack [6]}
      (interpret [3 *] {}) => {:result 3, :stack [3]}
      (interpret [*] {}) => {:result nil, :stack []}
      (interpret [1 2 3 4 *] {}) => {:result 12, :stack [1 2 12]}
      (interpret [1 2 3 * 4 5 7 *] {}) => {:result 35, :stack [1 6 4 35]}
      (interpret [1 2 3 4 5 * * *] {}) => {:result 120, :stack [1 120]}
      )

(fact "the interpreter does mixed operation arithmetic"
      (interpret [1 2 3 4 5 * + / -] {}) => {:result 21/23, :stack [21/23]}
      (interpret [1 2 * 3 + 4 / 5 -] {}) => {:result -15/4, :stack [-15/4]}
      )

(fact "the interpreter handles bound variables"
      (interpret [1 :x +] {:x 7, :y 9}) => {:result 8, :stack [8]}
      (interpret [:y :y / 111 :x + *] {:x 7/4, :y 9.5}) => {:result 112.75, :stack [112.75]}
      )
;;; rubrics

(fact "we can construct rubrics and access their components"
      (let [rubric (->Rubric 1 2)]
        (:input rubric) => 1
        (:output rubric) => 2
        ))

(fact "we can compute the error of a script on a rubric"
      (let [script [2 4 +]
            rubric (->Rubric 3 5)]
        (error-on script rubric) => 1)
      (let [script [4 20 -]
            rubric (->Rubric 3 5)]
        (error-on script rubric) => 21)
      (let [script [1 2 3 + +]
            rubric (->Rubric 0 6)]
        (error-on script rubric) => 0)
      )
