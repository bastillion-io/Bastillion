/**
 * Copyright 2013 Sean Kavanagh - sean.p.kavanagh6@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.keybox.manage.model;

import java.util.HashMap;
import java.util.List;

/**
 * allows for paged results on the display screens
 */
public class SortedSet {
    private String orderByField = null;
    private String orderByDirection = "asc";
    private List itemList;
    private HashMap<String, String> filterMap = new HashMap<>();

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

    public HashMap<String, String> getFilterMap() {
        return filterMap;
    }

    public void setFilterMap(HashMap<String, String> filterMap) {
        this.filterMap = filterMap;
    }
}
