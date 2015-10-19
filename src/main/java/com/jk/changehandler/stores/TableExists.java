package com.jk.changehandler.stores;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by jshridha on 10/11/15.
 */
public class TableExists {
    private static Set<String> tablePresenceInfo = new HashSet<>();

    public static Boolean exists(String tableName) {
        return tablePresenceInfo.contains(tableName);
    }

    public static void setExists(String tableName) {
        tablePresenceInfo.add(tableName);
    }

}