package com.keybox.manage.model;

import java.util.List;

/**
 * allows for paged results on the display screens
 */
public class SortedSet {
    String orderByField = null;
    String orderByDirection= "asc";
    List itemList;


    public String getOrderByField() {
        return orderByField;
    }

    public void setOrderByField(String orderByField) {
        this.orderByField = orderByField;
    }


    public String getOrderByDirection() {
        return orderByDirection;
    }

    public void setOrderByDirection(String orderByDirection) {
        this.orderByDirection = orderByDirection;
    }

    public List getItemList() {
        return itemList;
    }

    public void setItemList(List itemList) {
        this.itemList = itemList;
    }
}
