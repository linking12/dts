package io.dts.common.common;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author qinan.qn@taobao.com 2015年3月31日
 */
public class RedoContext {
  private ConcurrentHashMap<String, RedoBranch>/* dbkey,RedoBranch */ dbKeyMap =
      new ConcurrentHashMap<String, RedoBranch>();


  public RedoBranch find(String dbKey) {
    RedoBranch branch = dbKeyMap.get(dbKey);
    if (branch == null) {
      synchronized (this) {
        branch = dbKeyMap.get(dbKey);
        if (branch == null) {
          branch = new RedoBranch();
          dbKeyMap.put(dbKey, branch);
        }
      }
    }
    return branch;
  }


  public ConcurrentHashMap<String, RedoBranch> getDbKeyMap() {
    return dbKeyMap;
  }


  public final static class RedoBranch {
    private long branchId = -1;
    private ConcurrentHashMap<String, String> tableNames = new ConcurrentHashMap<String, String>();

    public void addTable(String tableName) {
      tableNames.put(tableName, tableName);
    }

    public Iterator<String> iterator() {
      return tableNames.keySet().iterator();
    }

    public void checkBranchId(BuildBranchId builder) throws SQLException {
      if (branchId < 0) {
        synchronized (this) {
          if (branchId < 0) {
            if (branchId == -2) {
              throw new SQLException("Register branch has failed!");
            } else {
              try {
                branchId = builder.build();
              } catch (Throwable e) {
                branchId = -2;
                throw new SQLException(e);
              }
            }
          }
        }
      }
    }

    public long getBranchId() {
      return branchId;
    }
  }
  public static interface BuildBranchId {
    public long build() throws Throwable;
  }
}
