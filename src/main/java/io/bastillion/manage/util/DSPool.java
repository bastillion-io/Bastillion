/**
 *    Copyright (C) 2013 Loophole, LLC
 *
 *    Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.bastillion.common.util.AppConfig;
import org.apache.commons.dbcp2.BasicDataSource;

/**
 * Class to create a pooling data source object using commons DBCP
 *
 */
public class DSPool {

    private static Logger log = LoggerFactory.getLogger(DSPool.class);

    private static BasicDataSource dsPool =  null;

    private static final String BASE_DIR = AppConfig.CONFIG_DIR;
    private static final String DB_DRIVER = AppConfig.getProperty("dbDriver");
    private static final int MAX_ACTIVE = Integer.parseInt(AppConfig.getProperty("maxActive"));
    private static final boolean TEST_ON_BORROW = Boolean.valueOf(AppConfig.getProperty("testOnBorrow"));
    private static final int MIN_IDLE = Integer.parseInt(AppConfig.getProperty("minIdle"));
    private static final int MAX_WAIT = Integer.parseInt(AppConfig.getProperty("maxWait"));

    private DSPool() {
    }


    /**
     * fetches the data source for H2 db
     *
     * @return data source pool
     */

    public static BasicDataSource getDataSource() {
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

    private static BasicDataSource registerDataSource() {
        System.setProperty("h2.baseDir", BASE_DIR);

        // create a database connection
        String user = AppConfig.getProperty("dbUser");
        String password = AppConfig.decryptProperty("dbPassword");
        String connectionURL = AppConfig.getProperty("dbConnectionURL");

        if(connectionURL != null && connectionURL.contains("CIPHER=")) {
           password = "filepwd " + password;
        }

        String validationQuery = "select 1";

        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(DB_DRIVER);
        dataSource.setMaxTotal(MAX_ACTIVE);
        dataSource.setTestOnBorrow(TEST_ON_BORROW);
        dataSource.setMinIdle(MIN_IDLE);
        dataSource.setMaxWaitMillis(MAX_WAIT);
        dataSource.setValidationQuery(validationQuery);
        dataSource.setUsername(user);
        dataSource.setPassword(password);
        dataSource.setUrl(connectionURL);

        return dataSource;

    }

}

