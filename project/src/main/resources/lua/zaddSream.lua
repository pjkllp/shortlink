---
--- Created by 27978
--- DateTime: 2026/4/14 21:40
---

--生产者生产zset元素供定时任务获取qps

local key = KEYS[1];

local t = redis.call("TIME")
-- 计算时间毫秒值
local now_ms = tonumber(t[1]) * 1000 + math.floor(tonumber(t[2]) / 1000)

local member=tostring(now_ms).."-"..tostring(t[2])

redis.call("zadd", key, now_ms, member)

redis.call("expire",key,120)



