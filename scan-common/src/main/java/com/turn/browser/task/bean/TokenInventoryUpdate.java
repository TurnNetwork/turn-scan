package com.turn.browser.task.bean;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenInventoryUpdate {

    /**
     *Current page number
     */
    private Integer page;

    /**
     * Whether the current page number has been updated
     */
    private boolean isUpdate;

    /**
     *The number of items in the current page number
     */
    private Integer num;

    /**
     * Whether the current page number has been updated
     *
     * @param page page number
     * @param size number of items
     * @return boolean
     * @date 2021/3/22
     */
    public boolean getPageUpdate(Integer page, Integer size) {
        if (page == this.getPage()) {
            return this.isUpdate;
        } else {
            return false;
        }
    }

    public void update(Integer page, boolean isUpdate, Integer num) {
        this.page = page;
        this.isUpdate = isUpdate;
        this.num = num;
    }


}
