package cn.javatoolbox.cache;

import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author: xbronze
 * @date: 2023-04-04 17:12
 * @description: 最少使用策略的缓存
 */
public class LFUCache<K, V> {

    // 缓存载体
    private ConcurrentHashMap<Object, Cache> concurrentHashMap;

    /**
     * 最大缓存
     */
    private final int size;

    public LFUCache(int size) {
        this.size = size;
        this.concurrentHashMap = new ConcurrentHashMap<Object, Cache>(size);
        new Thread(new TimeoutTimerThread()).start();
    }

    /**
     * 获取缓存
     * @param key 缓存key
     * @return 缓存内容
     */
    public Object get(K key) {
        checkNotNull(key);
        if (concurrentHashMap.isEmpty()) {
            return null;
        }
        if (concurrentHashMap.containsKey(key)) {
            Cache cache = concurrentHashMap.get(key);
            if (cache == null) {
                return null;
            }
            cache.setHitCount(cache.getHitCount() + 1);
            cache.setAccessTime(System.nanoTime());
            return cache.getValue();
        } else {
            return null;
        }
    }

    /**
     * 添加缓存
     * @param key
     * @param value
     * @param expire 存活时间（秒）
     */
    public void put(K key, V value, long expire){
        checkNotNull(key);
        checkNotNull(value);
        long nanoTime = System.nanoTime();
        // 当缓存存在时，更新缓存
        if (concurrentHashMap.containsKey(key)) {
            Cache cache = concurrentHashMap.get(key);
            // 命中次数加1
            cache.setHitCount(cache.getHitCount() + 1);
            // 更新写入时间和最后一次访问时间
            cache.setWriteTime(nanoTime);
            cache.setAccessTime(nanoTime);
            // 更新缓存的存活时间
            cache.setExpireTime(expire);
            // 更新缓存内容
            cache.setValue(value);
            return;
        }


        if (isFull()) {
            // 获取最少使用的缓存key
            Object leastFrequentlyUserKey = getLeastFrequentlyUsedKey();
            if (leastFrequentlyUserKey != null) {
                concurrentHashMap.remove(leastFrequentlyUserKey);
            }
        }

        // 当缓存不存在，并且还没达到最大缓存数
        Cache cache = new Cache();
        cache.setKey(key);
        cache.setValue(value);
        cache.setWriteTime(nanoTime);
        cache.setAccessTime(nanoTime);
        cache.setHitCount(1);
        cache.setExpireTime(expire);
        concurrentHashMap.put(key, cache);
    }

    public static <T> T checkNotNull(T reference) {
        if (reference == null) {
            throw new NullPointerException();
        }
        return reference;
    }

    /**
     * 获取最少使用的缓存key
      */
    private Object getLeastFrequentlyUsedKey() {
        Cache minCache = Collections.min(concurrentHashMap.values());
        if (minCache == null) {
            return null;
        }
        return minCache.getKey();
    }

    /**
     * 判断是否达到最大缓存数
     * @return 是否最大缓存
     */
    private boolean isFull() {
        return concurrentHashMap.size() == size;
    }


    /**
     * 定时清除过期缓存
     */
    class TimeoutTimerThread implements Runnable {
        public void run() {
            // 每60秒执行一次清除过期缓存的操作
            while (true) {
                try {
                    TimeUnit.SECONDS.sleep(60);
                    expireCache();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * 清除过期缓存
         */
        private void expireCache() {
            for (Object key : concurrentHashMap.keySet()) {
                Cache cache = concurrentHashMap.get(key);
                // 纳秒 -> 秒
                long timeoutTime = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - cache.getWriteTime());
                if (timeoutTime > cache.getExpireTime()) {
                    // 清除过期的缓存
                    concurrentHashMap.remove(key);
                }
            }
        }
    }
}
