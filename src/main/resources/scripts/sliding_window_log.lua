local key = KEYS[1]
local time = redis.call('TIME')
local now_ms = (tonumber(time[1]) * 1000) + math.floor(tonumber(time[2]) / 1000)
local capacity = tonumber(ARGV[1])
local unique_id = time[1] .. ":" .. time[2]

-- remove timestamps older than window
redis.call('ZREMRANGEBYSCORE', key, '-inf', now_ms - 60000)

-- count in-window timestamps
local count = redis.call('ZCARD', key)  

if count < capacity then
    -- add current timestamp
    redis.call('ZADD', key, now_ms, unique_id)
    redis.call('EXPIRE', key, 3600) -- expire key after 1 hour of inactivity
    return 1
else
    return 0
end