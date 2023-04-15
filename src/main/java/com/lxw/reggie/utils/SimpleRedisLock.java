package com.lxw.reggie.utils;

import cn.hutool.core.lang.UUID;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class SimpleRedisLock implements ILock{
    private String name;//业务前缀

    private StringRedisTemplate stringRedisTemplate;

    private static final String KEY_PREFIX = "lock:";//锁前缀
    private static final String ID_PREFIX = UUID.randomUUID().toString(true) + "-";//ID前缀
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT;//Java中封装的lua脚本对象

    public SimpleRedisLock(String name, StringRedisTemplate stringRedisTemplate) {
        this.name = name;
        this.stringRedisTemplate = stringRedisTemplate;
    }


    static {
        UNLOCK_SCRIPT = new DefaultRedisScript<>();
        //指定lua脚本的位置
        UNLOCK_SCRIPT.setLocation(new ClassPathResource("unlock.lua"));
        //指定脚本的返回值类型
        UNLOCK_SCRIPT.setResultType(Long.class);
    }


    @Override
    public boolean tryLock(long timeoutSec) {
        // 获取线程标示
        String threadId = ID_PREFIX + Thread.currentThread().getId();
        // 获取锁
        Boolean success = stringRedisTemplate.opsForValue()
                .setIfAbsent(KEY_PREFIX + name, threadId, timeoutSec, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success);
    }

    @Override
    public void unlock() {
        // 调用lua脚本
        stringRedisTemplate.execute(
                UNLOCK_SCRIPT,
                Collections.singletonList(KEY_PREFIX + name),
                ID_PREFIX + Thread.currentThread().getId());
    }

}
