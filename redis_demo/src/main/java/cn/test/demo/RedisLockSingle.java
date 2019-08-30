package cn.test.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import redis.clients.jedis.*;



public class RedisLockSingle {



    @Autowired
    private JedisPoolConfig jedisPoolConfig;

    private static String pre="REDIS_LOCK:";

    @Value("${redis.ip}")
    private String host;

    @Value("${redis.post}")
    private int port;

    @Value("${redis.maxIdle}")
    private int maxIdle;

    @Value("${redis.maxActive}")
    private int maxTotal;

    @Value("${redis.maxWaitMillis}")
    private int maxWaitMillis;

    @Value("${redis.timeout}")
    private int connectionTimeout;

    /**
     * 单机版
     * @return
     */
    private JedisPool getJedisPool(){
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(maxIdle);
        jedisPoolConfig.setMaxTotal(maxTotal);
        jedisPoolConfig.setMaxWaitMillis(maxWaitMillis);
        return new JedisPool(jedisPoolConfig,host,port,connectionTimeout);
    }



    /**
     * 尝试获取分布式锁，注释的为单机版
     * @param seriNo 全局流水号
     * @param value  key的value值
     * @param expireTime 过期时间
     * @return true成功，false失败
     */
    public boolean tryGetDistributedLock(String seriNo,String value,int expireTime){
        JedisPool jedisPool = getJedisPool();
        Jedis jedis = null;
        try{
            jedis = jedisPool.getResource();
            String result = jedis.set(pre+seriNo,value,"NX","PX",expireTime);
            if("OK".equals(result)){
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }finally {
            jedis.close();
        }
        return false;
    }

    /**
     * 释放锁，注释的为单机版
     * @param seriNo 全局流水号
     * @param value  key的value
     * @return true成功，false失败
     */
    public boolean releaseDistributedLock(String seriNo,String value){
        JedisPool jedisPool = getJedisPool();
         Jedis jedis = null;
        try{
            jedis = jedisPool.getResource();
            long result = jedis.del(pre+seriNo,value);
            if(1L==result){
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }finally {
            jedis.close();
        }
        return false;
    }
}
