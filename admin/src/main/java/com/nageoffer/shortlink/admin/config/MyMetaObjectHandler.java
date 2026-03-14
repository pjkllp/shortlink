package com.nageoffer.shortlink.admin.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;

@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject,"createTime", Date.class,Date.from(Instant.now()));
        this.strictInsertFill(metaObject,"updateTime",Date.class,Date.from(Instant.now()));
        this.strictInsertFill(metaObject,"delFlag",Integer.class,0);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject,"updateTime",Date.class,Date.from(Instant.now()));
    }
}
