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
package com.keybox.manage.util;

import org.apache.commons.dbcp.*;
import org.apache.commons.pool.impl.GenericObjectPool;

/**
 * Class to create a pooling data source object using commons DBCP
 *
 */
public class DSPool {

    //system path to the H2 DB
    private static String DB_PATH = DBUtils.class.getClassLoader().getResource("com/keybox/common/db").getPath();


    private static PoolingDataSource dsPool;


    /**
     * fetches the data source for H2 db
     *
     * @return data source pool
     */

    public static org.apache.commons.dbcp.PoolingDataSource getDataSource() {
        if (dsPool == null) {

            dsPool = registerDataSource();
        }
        return dsPool;

    }

    /**
     * register the data source for H2 DB
     *
     * @return pooling database object
     */

    private static PoolingDataSource registerDataSource() {


        // create a database connection
        String user = "keybox";
        String password = "filepwd 45WJLnwhpA47EepT162hrVnDn3vYRvJhpZi0sVdvN9Sdsf";
        String connectionURI = "jdbc:h2:" + DB_PATH + "/keybox;CIPHER=AES";

        String validationQuery = "select 1";

        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }


        GenericObjectPool connectionPool = new GenericObjectPool(null);

        connectionPool.setMaxActive(25);
        connectionPool.setTestOnBorrow(true);
        connectionPool.setMinIdle(2);
        connectionPool.setMaxWait(15000);
        connectionPool.setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_BLOCK);


        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(connectionURI, user, password);


        new PoolableConnectionFactory(connectionFactory, connectionPool, null, validationQuery, false, true);

        return new PoolingDataSource(connectionPool);

    }


}

