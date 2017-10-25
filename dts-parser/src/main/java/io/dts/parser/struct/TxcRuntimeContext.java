package io.dts.parser.struct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import io.dts.common.common.TxcXID;
import io.dts.common.common.context.DtsContext;

public class TxcRuntimeContext {

	private final Logger logger = LoggerFactory.getLogger(TxcRuntimeContext.class);
	/**
	 * 主键，自增
	 */
	private long id;

	/**
	 * 全局事务ID
	 */
	private String xid;

	/**
	 * 分支事务ID
	 */
	private long branch_id;

	/**
	 * 分支日志
	 */
	private List<RollbackInfor> rollback_infor = new ArrayList<RollbackInfor>();

	/**
	 * 处理状态
	 */
	private int status;

	/**
	 * 服务所在IP
	 */
	private String server;

	/**
	 * 开始时间
	 */
	private final long start;

	/**
	 * 开始时间，每次打点时重新计时
	 */
	@JSONField(serialize = false)
	private long start0;

	public TxcRuntimeContext() {
		if (logger.isDebugEnabled()) {
			start = System.currentTimeMillis();
			start0 = System.currentTimeMillis();
		} else {
			start = 0;
			start0 = 0;
		}
	}
	
	/**
	 * 转换为String
	 * 
	 * @return
	 */
	public String encode() {
		return JSON.toJSONString(this, SerializerFeature.WriteDateUseDateFormat);
	}

	/**
	 * 转换为Object
	 * 
	 * @param jsonString
	 * @return
	 */
	public static TxcRuntimeContext decode(String jsonString) {
		return JSON.parseObject(jsonString, TxcRuntimeContext.class);
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getXid() {
		return xid;
	}

	public void setXid(String xid) {
		this.xid = xid;
	}

	public long getBranchId() {
		return branch_id;
	}

	public void setBranchId(long branch_id) {
		this.branch_id = branch_id;
	}

	public void setInfor(List<RollbackInfor> rollback_infor) {
		this.rollback_infor = rollback_infor;
	}

	public List<RollbackInfor> getInfor() {
		return rollback_infor;
	}

	public void addInfor(RollbackInfor undoLogInfor) {
		rollback_infor.add(undoLogInfor);
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public long getRT() {
		return System.currentTimeMillis() - start;
	}

	/**
	 * 每次调用重新计算打点的开始时间
	 */
	public long getRTFromLastPoint() {
		long fromLastPoint = System.currentTimeMillis() - start0;
		start0 = System.currentTimeMillis();
		return fromLastPoint;
	}

	public String txcStr() throws SQLException {
		if (DtsContext.inTxcTransaction()) {
			return TxcXID.formatXid(getXid(), getBranchId());
		}

		return " [NULL TXC] ";
	}
}
