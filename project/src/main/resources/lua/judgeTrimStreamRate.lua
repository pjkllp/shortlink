---
--- Created by 27978
--- DateTime: 2026/4/14 20:35
---
--计算qps


local key = KEYS[1]

local t = redis.call("TIME")
-- 计算时间毫秒值
local now_ms = tonumber(t[1]) * 1000 + math.floor(tonumber(t[2]) / 1000)
local window_start = now_ms - 1000

-- 清理窗口外数据
redis.call("ZREMRANGEBYSCORE", key, 0, window_start)


-- 统计1秒内请求数
local count = redis.call("ZCARD", key)

-- 防止冷key常驻
redis.call("EXPIRE", key, 120)

return count


