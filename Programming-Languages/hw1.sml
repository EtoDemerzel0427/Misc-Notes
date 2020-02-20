fun is_older (x: int*int*int, y: int*int*int) =
    if (#1 x) < (#1 y) then
	true else
    if (#1 x) > (#1 y) then
	false else
    if (#2 x) < (#2 y) then
	true else
    if (#2 x) > (#2 y) then
	false else
    if (#3 x) < (#3 y) then
	true else false

fun number_in_month (dates: (int*int*int) list, month: int) = 
    if null dates then 0
    else if (#2 (hd dates)) = month then
    1 + number_in_month (tl dates, month)
    else number_in_month (tl dates, month)

fun number_in_months (dates: (int*int*int) list, months: int list) = 
    if null months then 0
    else number_in_month (dates, hd months) + number_in_months (dates, tl months)

fun dates_in_month (dates: (int*int*int) list, month: int) = 
    if null dates then []
    else if (#2 (hd dates)) = month then
    (hd dates)::(dates_in_month (tl dates, month))
    else dates_in_month(tl dates, month)

fun dates_in_months (dates: (int*int*int) list, months: int list) = 
    if null months then []
    else dates_in_month(dates, hd months) @ dates_in_months(dates, tl months)

fun get_nth (s: string list, n: int) = 
    if n = 1 then hd s
    else get_nth (tl s, n-1)  

fun date_to_string (date: int*int*int) =
    let val months = ["January", "February", "March", "April", "May", "June", 
                      "July", "August", "September", "October", "November", "December"]
    in
        get_nth(months, (#2 date)) ^ " " ^ Int.toString(#3 date) ^ ", " ^ Int.toString(#1 date)
    end

fun number_before_reaching_sum (sum: int, numbers: int list) = 
    if sum <= hd numbers then
    0 else
    1 + number_before_reaching_sum (sum - (hd numbers), tl numbers)
    
fun what_month (day_num: int) = 
    let val months_day = [31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31]
    in
        number_before_reaching_sum (day_num, months_day) + 1
    end

fun month_range (day1: int, day2: int) = 
    if day1 > day2 then
    []
    else what_month (day1) :: month_range (day1 + 1, day2)

fun oldest (dates: (int*int*int) list) = 
    if null dates then NONE
    else if null (tl dates)
    then SOME(hd dates)
    else let val latter_max = oldest(tl dates)
    in
        if is_older(hd dates, valOf latter_max) then SOME(hd dates)
        else latter_max
    end

(* Challenge problems *)

(* it is a O(N^2) method *)
fun remove_duplicates [] = []
  | remove_duplicates (x::xs) = x::remove_duplicates(List.filter (fn y => y <> x) xs)

fun number_in_months_challenge (dates: (int*int*int) list, months: int list) = 
    number_in_months(remove_duplicates dates, months)

fun dates_in_months_challenge (dates: (int*int*int) list, months: int list) = 
    dates_in_months(remove_duplicates dates, months)

fun reasonable_date (date: int*int*int) = 
    let val year  = #1 date
        val month = #2 date
        val day   = #3 date
        val is_leap = year mod 400 = 0 orelse (year mod 4 = 0 andalso year mod 100 <> 0)
        val feb_len = if is_leap then 29 else 28
        val month_days = [31, feb_len, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31]
    in
        year > 0 andalso month >= 1 andalso month <= 12
        andalso day >= 1 andalso day <= List.nth(month_days, month - 1)
    end



        
        