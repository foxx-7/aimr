package com.aimr.notify.model.entity;


import com.aimr.notify.util.CommonUtils;
import lombok.Data;

import java.time.Instant;

@Data
public abstract class AbstractEntity {
    private Instant createdAt;
    private Instant updatedAt;

    public  void entityCreated(){
        setCreatedAt(CommonUtils.getCurrentTimeStamp());
        setUpdatedAt(CommonUtils.getCurrentTimeStamp());
    }

    public  void entityUpdated(){
        setUpdatedAt(CommonUtils.getCurrentTimeStamp());
    }
}
