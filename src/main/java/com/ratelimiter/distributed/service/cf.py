from typing import List


def minOperations(s: str) -> int:
    # check if already sorted
    n = len(s)
    
    valid = True
    for i in range(n - 1):
        if s[i] > s[i + 1] and valid:
            valid = False
            break
    
    if valid:
        return 0 # sorted already

    if n == 2:
        return -1 # impossible without sorting full string

    # left biggest
    left_big = [""] * n
    left_big[0] = s[0]
    left_sorted = [True] * n

    for i in range(1, n):
        left_big[i] = max(left_big[i - 1], s[i])
        left_sorted[i] = s[i] >= s[i - 1] and left_sorted[i - 1]

    # right smallest
    right_smallest = [""] * n
    right_smallest[n - 1] = s[n - 1]
    right_sorted = [True] * n

    for i in range(n - 2, -1, -1):
        right_smallest[i] = min(right_smallest[i + 1], s[i])
        right_sorted[i] = s[i] <= s[i + 1] and right_sorted[i + 1]
    
    for i in range(1, n):
        if not (left_sorted[i - 1] and right_sorted[i]):
            continue

        if left_big[i - 1] <= right_smallest[i]:
            return 1


    # case: 3 ops
    c1 = c2 = 1
    global_min = global_max= s[0]
    for i in range(1, n):
        ch = s[i]
        if global_min > ch:
            global_min = ch
            c1 = 0
        
        if global_min == ch:
            c1 += 1
        
        if global_max < ch:
            global_max = ch
            c2 = 0
        
        if global_max == ch:
            c2 += 1
    
    if s[0] == global_max and s[n - 1] == global_min:
        if c1 > 1 or c2 > 1:
            return 2
        
        return 3

    return 2

print(minOperations("edc"))