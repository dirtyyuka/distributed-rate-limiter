def concatenatedBinary(n: int) -> int:
    ans = 1
    shift = 2
    for i in range(2, n + 1):
        ans <<= shift
        ans |= i
        if not i & (i + 1):
            shift += 1
    
    return ans % (10**9 + 7)

print(concatenatedBinary(12))