/* Copyright (c) 2021 OceanBase and/or its affiliates. All rights reserved.
oblogclient is licensed under Mulan PSL v2.
You can use this software according to the terms and conditions of the Mulan PSL v2.
You may obtain a copy of Mulan PSL v2 at:
         http://license.coscl.org.cn/MulanPSL2
THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
See the Mulan PSL v2 for more details. */

package com.oceanbase.clogproxy.client.config;


import com.oceanbase.clogproxy.client.util.Validator;
import com.oceanbase.clogproxy.common.config.SharedConf;
import com.oceanbase.clogproxy.common.packet.LogType;
import com.oceanbase.clogproxy.common.util.CryptoUtil;
import com.oceanbase.clogproxy.common.util.Hex;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** This is a configuration class for connection to log proxy. */
public class ObReaderConfig extends AbstractConnectionConfig {
    private static final Logger logger = LoggerFactory.getLogger(ObReaderConfig.class);

    /** Root server list. */
    private static final ConfigItem<String> RS_LIST = new ConfigItem<>("rootserver_list", "");

    /** Cluster username. */
    private static final ConfigItem<String> CLUSTER_USER = new ConfigItem<>("cluster_user", "");

    /** Cluster password. */
    private static final ConfigItem<String> CLUSTER_PASSWORD =
            new ConfigItem<>("cluster_password", "");

    /** Table whitelist. */
    private static final ConfigItem<String> TABLE_WHITE_LIST =
            new ConfigItem<>("tb_white_list", "");

    /** Start timestamp. */
    private static final ConfigItem<Long> START_TIMESTAMP =
            new ConfigItem<>("first_start_timestamp", 0L);

    /** Constructor with empty arguments. */
    public ObReaderConfig() {
        super(new HashMap<>());
    }

    /**
     * Constructor with a config map.
     *
     * @param allConfigs Config map.
     */
    public ObReaderConfig(Map<String, String> allConfigs) {
        super(allConfigs);
    }

    @Override
    public LogType getLogType() {
        return LogType.OCEANBASE;
    }

    @Override
    public boolean valid() {
        try {
            Validator.notEmpty(RS_LIST.val, "invalid rsList");
            Validator.notEmpty(CLUSTER_USER.val, "invalid clusterUser");
            Validator.notEmpty(CLUSTER_PASSWORD.val, "invalid clusterPassword");
            if (START_TIMESTAMP.val < 0L) {
                throw new IllegalArgumentException("invalid startTimestamp");
            }
            return true;
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage());
            return false;
        }
    }

    @Override
    public String generateConfigurationString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, ConfigItem<Object>> entry : configs.entrySet()) {
            String value = entry.getValue().val.toString();
            if (CLUSTER_PASSWORD.key.equals(entry.getKey()) && SharedConf.AUTH_PASSWORD_HASH) {
                value = Hex.str(CryptoUtil.sha1(value));
            }
            sb.append(entry.getKey()).append("=").append(value).append(" ");
        }

        for (Map.Entry<String, String> entry : extraConfigs.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append(" ");
        }
        return sb.toString();
    }

    @Override
    public void updateCheckpoint(String checkpoint) {
        try {
            START_TIMESTAMP.set(Long.parseLong(checkpoint));
        } catch (NumberFormatException e) {
            // do nothing
        }
    }

    @Override
    public String toString() {
        return "rootserver_list="
                + RS_LIST
                + ", cluster_user="
                + CLUSTER_USER
                + ", cluster_password=******, "
                + "tb_white_list="
                + TABLE_WHITE_LIST
                + ", start_timestamp="
                + START_TIMESTAMP;
    }

    /**
     * Set root server list.
     *
     * @param rsList Root server list.
     */
    public void setRsList(String rsList) {
        RS_LIST.set(rsList);
    }

    /**
     * Set cluster username
     *
     * @param clusterUser Cluster username.
     */
    public void setUsername(String clusterUser) {
        CLUSTER_USER.set(clusterUser);
    }

    /**
     * Set cluster password
     *
     * @param clusterPassword Cluster password.
     */
    public void setPassword(String clusterPassword) {
        CLUSTER_PASSWORD.set(clusterPassword);
    }

    /**
     * Set table whitelist. It is composed of three dimensions: tenant, library, and table. Asterisk
     * means any, such as: "A.foo.bar", "B.foo.*", "C.*.*", "*.*.*".
     *
     * @param tableWhiteList Table whitelist.
     */
    public void setTableWhiteList(String tableWhiteList) {
        TABLE_WHITE_LIST.set(tableWhiteList);
    }

    /**
     * Set start timestamp, zero means from now on.
     *
     * @param startTimestamp Start timestamp.
     */
    public void setStartTimestamp(Long startTimestamp) {
        START_TIMESTAMP.set(startTimestamp);
    }
}
