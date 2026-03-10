local tokens = redis.call('HGET', KEYS[1], 'tokens')
local last_refill = redis.call('HGET', KEYS[1], 'last_refill')

local capacity = tonumber(ARGV[1])
local refill_rate = tonumber(ARGV[2])
local now = tonumber(ARGV[3])

if not tokens then
    tokens = capacity
    last_refill = now
else
    tokens = tonumber(tokens)
    last_refill = tonumber(last_refill)
end

local elapsed = now - last_refill
local refill = math.floor(elapsed / refill_rate)

if refill > 0 then
    tokens = math.min(capacity, tokens + refill)
    last_refill = last_refill + refill * refill_rate
end

local allowed = 0

if tokens > 0 then
    tokens = tokens - 1
    allowed = 1
end

-- update the key
redis.call('HSET', KEYS[1], 
    'tokens', tokens, 
    'last_refill', last_refill
)

-- auto-expire after 1 hour of inactivity
redis.call('EXPIRE', KEYS[1], 3600)

return allowed