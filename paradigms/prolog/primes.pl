check(_, It) :- composite(It), !.
check(Div, It) :- prime(It), assert(min_div(It, Div)).

find_comp(Div, It, Size) :- prime(Div), It < Size, check(Div, It), assert(composite(It)), NewIt is It + Div, find_comp(Div, NewIt, Size).

iterator(Div, Size) :- prime(Div), It is Div * Div, find_comp(Div, It, Size).
iterator(Div, Size) :- NewDiv is Div + 1, (NewDiv * NewDiv - 1) < Size, iterator(NewDiv, Size).


find_prime(It, Pos, Size) :- prime(It), !, NewPos is Pos + 1, assert(nth_prime(Pos, It)), NewIt is It + 1, find_prime(NewIt, NewPos, Size).
find_prime(It, Pos, Size) :- NewIt is It + 1, NewIt < Size, !, find_prime(NewIt, Pos, Size).

init(MAX_N) :- (iterator(2, MAX_N); find_prime(2, 1, MAX_N)).

prime(N) :- N > 1, \+ composite(N).

prime_divisors(1, []) :- !.
prime_divisors(N, [H | T]) :- N > 1, number(N), min_div(N, H), N1 is div(N, H), prime_divisors(N1, T).
prime_divisors(N, [H]) :- prime(N), H = N.