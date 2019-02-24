/**
 *    Copyright (C) 2013 Loophole, LLC
 *
 *    This program is free software: you can redistribute it and/or  modify
 *    it under the terms of the GNU Affero General Public License, version 3,
 *    as published by the Free Software Foundation.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Affero General Public License for more details.
 *
 *    You should have received a copy of the GNU Affero General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *    As a special exception, the copyright holders give permission to link the
 *    code of portions of this program with the OpenSSL library under certain
 *    conditions as described in each individual source file and distribute
 *    linked combinations including the program with the OpenSSL library. You
 *    must comply with the GNU Affero General Public License in all respects for
 *    all of the code used other than as permitted herein. If you modify file(s)
 *    with this exception, you may extend this exception to your version of the
 *    file(s), but you are not obligated to do so. If you do not wish to do so,
 *    delete this exception statement from your version. If you delete this
 *    exception statement from all source files in the program, then also delete
 *    it in the license file.
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

