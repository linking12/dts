package org.dts.server.mapper;

import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;
import org.apache.ibatis.jdbc.SQL;
import org.dts.server.entity.DtsTransaction;

import java.util.Date;

/**
 * Created by Arc on 19/5/2017.
 */
public interface DtsTransactionMapper {

    @InsertProvider(type = DtsTransactionSQL.class, method = "insert")
    int insert(DtsTransaction dtsTransaction);

    @UpdateProvider(type = DtsTransactionSQL.class, method = "update")
    int update(@Param("id") Long id, @Param("status") Integer status, @Param("endAt") Date endAt);

    @SelectProvider(type = DtsTransactionSQL.class, method = "getById")
    DtsTransaction getById(@Param("id") Long id);

    class DtsTransactionSQL {

        private static final String SELECT_SQL_BASE = "trans.id as id, " +
            "trans.app_name as appName, " +
            "trans.server_group as serverGroup, " +
            "trans.app_address as appAddress, " +
            "trans.status as status, " +
            "trans.start_at as startAt, " +
            "trans.end_at as endAt ";


        private static final String FROM_SQL_BASE = "dts_transaction trans";


        public String getById(Long id) {
            return new SQL()
                .SELECT(SELECT_SQL_BASE)
                .FROM(FROM_SQL_BASE)
                .WHERE("trans.id=#{id}")
                .toString();
        }


        public String insert() {
            return new SQL()
                .INSERT_INTO("dts_transaction")
                .VALUES("id", "#{id}")
                .VALUES("app_name", "#{appName}")
                .VALUES("server_group", "#{serverGroup}")
                .VALUES("app_address ", "#{appAddress}")
                .VALUES("status", "#{status}")
                .VALUES("start_at", "#{startAt}")
                .VALUES("end_at", "#{endAt}")
                .VALUES("created_at", "now()")
                .VALUES("updated_at", "now()")
                .toString();
        }

        public String update(Long id, Integer status, Date endAt) {
            return new SQL()
                .UPDATE("dts_transaction T")
                .SET("T.status = #{status}")
                .SET("T.end_at =#{endAt}")
                .SET("T.updated_at = now()")
                .WHERE("T.trans_id = #{id}")
                .toString();
        }
    }

}
