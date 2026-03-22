
local key = KEYS[1]
local max = tonumber(ARGV[1])
local maxtime=tonumber(ARGV[2])

local result=redis.call("exists",key)

if result == 0 then redis.call("set", key, 1,"ex",maxtime, "nx")

else redis.call("incr",key) end

if tonumber(redis.call("get",key))>max then return 0

else return 1 end



