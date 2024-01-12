package com.turn.browser.bean;

import com.turn.browser.dao.entity.Game;
import com.turn.browser.enums.GameTypeEnum;
import lombok.Data;

import java.util.Date;

@Data
public class GameContract extends Game {
    private GameTypeEnum typeEnum;
    private boolean dirty = false;
    public synchronized void setDirty(boolean bol){
        this.dirty=bol;
    }
    public GameContract() {
        Date date = new Date();
        setCreateTime(date);
        setUpdateTime(date);
    }
}
