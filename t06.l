(length ())
(length (quote (a)))
(length (quote (a b)))
(length (quote (3 4 5)))
(length (quote ((3 2 1) (4 6))))
(length (quote ((3 2 1) () (4 6))))
(length (quote ((3 2 1) () (((()))) (4 6) 3)))

(length (cons (quote z) (quote (a b))))
(length (cons (quote (y z)) (quote (a b))))
(length (cons 1 (quote (2))))
(length (cons () ()))
(length (cons 3 ()))

(length (list (length ()) (length (quote (a b c))) () (quote (((()))))))

; errors
(length 4)
(length 4 5)
(length (4 5))
