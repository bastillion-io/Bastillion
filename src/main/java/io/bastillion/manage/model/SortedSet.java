/**
 *    Copyright (C) 2013 Loophole, LLC
 *
 *    Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * allows for paged results on the display screens
 */
public class SortedSet {
    private String orderByField = null;
    private String orderByDirection = "asc";
    private List itemList;
    private Map<String, String> filterMap = new HashMap<>();

    public SortedSet() {
        
    }
    
    public SortedSet(String orderByField){
        this.orderByField =  orderByField;
    }
    

    public String getOrderByField() {

        if (orderByField != null) {
            return orderByField.replaceAll("[^0-9,a-z,A-Z,\\_,\\.]", "");
        }
        return null;

    }

    public void setOrderByField(String orderByField) {
        this.orderByField = orderByField;
    }


    public String getOrderByDirection() {
        if ("asc".equalsIgnoreCase(orderByDirection)) {
            return "asc";
        } else {
            return "desc";
        }
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

    public Map<String, String> getFilterMap() {
        return filterMap;
    }

    public void setFilterMap(Map<String, String> filterMap) {
        this.filterMap = filterMap;
    }
}
