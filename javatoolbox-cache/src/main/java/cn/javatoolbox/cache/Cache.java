package cn.javatoolbox.cache;

import lombok.Data;

/**
 * @author: xbronze
 * @date: 2023-04-04 17:02
 * @description: 缓存实体
 */
@Data
public class Cache implements Comparable<Cache>{

    /**
     * 键
     */
    private Object key;

    /**
     * 缓存值
     */
    private Object value;

    /**
     * 最后一次访问时间
     */
    private long accessTime;

    /**
     * 创建时间
     */
    private long writeTime;

    /**
     * 存活时间（秒）
     */
    private long expireTime;

    /**
     * 命中次数
     */
    private Integer hitCount;

    public int compareTo(Cache o) {
        return this.hitCount.compareTo(o.getHitCount());
    }
}
