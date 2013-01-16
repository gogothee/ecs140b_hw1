(car (quote (a b)))
(cdr (quote (a b)))

(car (quote (a)))
(cdr (quote (a)))

(car ())
(cdr ())

(cons (quote z) (quote (a b)))
(cons (quote (y z)) (quote (a b)))
(cons 1 (quote (2)))
(cons () ())
(cons 3 ())

; this should fail (assuming you're not implementing dotted pairs)
; in LISP, it gives ( 1 . 2 )
(cons 1 2)
