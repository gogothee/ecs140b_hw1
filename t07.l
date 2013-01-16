(append)
(append ())
(append () ())
(append () (quote (34 56 7)) ())
(quote (34 56 7))
(append (quote (a)) )
(quote (a))
(append (quote (a)) (quote (b)))
(append (quote (a e)) (quote (b f)) (quote (c g)))
(quote (a e))
(quote (b f))
(quote (c g))
; 5 causes error
(append 5 (quote (c)) (quote (b)) (quote 4))
; 4 causes error (in LISP OK)
(append '(3) 4)
; foo causes error (note: evaluation of foo, after error, gives (),
; which is fine within append.
(append '(3) foo '(4))
