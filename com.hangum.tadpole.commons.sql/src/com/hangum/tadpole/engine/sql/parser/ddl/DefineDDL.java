/*******************************************************************************
 * Copyright (c) 2015 hangum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     hangum - initial API and implementation
 ******************************************************************************/
package com.hangum.tadpole.engine.sql.parser.ddl;

import com.hangum.tadpole.commons.libs.core.define.PublicTadpoleDefine.QUERY_DDL_STATUS;
import com.hangum.tadpole.commons.libs.core.define.PublicTadpoleDefine.QUERY_DDL_TYPE;

/**
 * define ddl
 * 
 * @author hangum
 *
 */
public enum DefineDDL {
	TABLE_CREATE("CREATE\\s+TABLE\\s+([A-Z0-9_\\.\"'`]+)", QUERY_DDL_TYPE.TABLE, QUERY_DDL_STATUS.CREATE),
	TABLE_ALTER("ALTER\\s+TABLE\\s+([A-Z0-9_\\.\"'`]+)", QUERY_DDL_TYPE.TABLE, QUERY_DDL_STATUS.ALTER),
	TABLE_DROP("DROP\\s+TABLE\\s+([A-Z0-9_\\.\"'`]+)", QUERY_DDL_TYPE.TABLE, QUERY_DDL_STATUS.DROP),
	
	INDEX_CREATE("CREATE\\s+INDEX\\s+([A-Z0-9_\\.\"'`]+)", QUERY_DDL_TYPE.INDEX, QUERY_DDL_STATUS.CREATE),
	INDEX_ALTER("ALTER\\s+INDEX\\s+([A-Z0-9_\\.\"'`]+)", QUERY_DDL_TYPE.INDEX, QUERY_DDL_STATUS.ALTER),
	INDEX_DROP("DROP\\s+INDEX\\s+([A-Z0-9_\\.\"'`]+)", QUERY_DDL_TYPE.INDEX, QUERY_DDL_STATUS.DROP),
	
	VIEW_CREATE("CREATE\\s+VIEW\\s+([A-Z0-9_\\.\"'`]+)", QUERY_DDL_TYPE.VIEW, QUERY_DDL_STATUS.CREATE),
	VIEW_CREATE_REPLACE("CREATE\\s+OR\\s+REPLACE\\s+VIEW\\s+([A-Z0-9_\\.\"'`]+)", QUERY_DDL_TYPE.VIEW, QUERY_DDL_STATUS.CREATE),
	VIEW_ALTER("ALTER\\s+VIEW\\s+([A-Z0-9_\\.\"'`]+)", QUERY_DDL_TYPE.VIEW, QUERY_DDL_STATUS.ALTER),
	VIEW_DROP("DROP\\s+VIEW\\s+([A-Z0-9_\\.\"'`]+)", QUERY_DDL_TYPE.VIEW, QUERY_DDL_STATUS.DROP),
	
	SYNONYM_CREATE("CREATE\\s+SYNONYM\\s+([A-Z0-9_\\.\"'`]+)", QUERY_DDL_TYPE.SYNONYM, QUERY_DDL_STATUS.CREATE),
	SYNONYM_CREATE_REPLACE("CREATE\\s+OR\\s+REPLACE\\s+SYNONYM\\s+([A-Z0-9_\\.\"'`]+)", QUERY_DDL_TYPE.SYNONYM, QUERY_DDL_STATUS.CREATE),
	SYNONYM_ALTER("ALTER\\s+SYNONYM\\s+([A-Z0-9_\\.\"'`]+)", QUERY_DDL_TYPE.SYNONYM, QUERY_DDL_STATUS.ALTER),
	SYNONYM_DROP("DROP\\s+SYNONYM\\s+([A-Z0-9_\\.\"'`]+)", QUERY_DDL_TYPE.SYNONYM, QUERY_DDL_STATUS.DROP),
	
	// SEQUENCE
	SEQUENCE_CREATE("CREATE\\s+SEQUENCE\\s+([A-Z0-9_\\.\"'`]+)", QUERY_DDL_TYPE.SEQUENCE, QUERY_DDL_STATUS.CREATE),
	SEQUENCE_ALTER("ALTER\\s+SEQUENCE\\s+([A-Z0-9_\\.\"'`]+)", QUERY_DDL_TYPE.SEQUENCE, QUERY_DDL_STATUS.ALTER),
	SEQUENCE_DROP("DROP\\s+SEQUENCE\\s+([A-Z0-9_\\.\"'`]+)", QUERY_DDL_TYPE.SEQUENCE, QUERY_DDL_STATUS.DROP),
	
	// jobs
	JOBS_CREATE("\\s+([A-Z0-9_\\.\"'`]+)SYS.DBMS_JOB.ISUBMIT\\s+([A-Z0-9_\\.\"'`]+)", QUERY_DDL_TYPE.JOBS, QUERY_DDL_STATUS.CREATE),
	JOBS_ALTER("\\s+([A-Z0-9_\\.\"'`]+)SYS.DBMS_JOB.REMOVE\\s+([A-Z0-9_\\.\"'`]+)", QUERY_DDL_TYPE.JOBS, QUERY_DDL_STATUS.ALTER),
	JOBS_DROP("\\s+([A-Z0-9_\\.\"'`]+)SYS.DBMS_JOB.REMOVE\\s+([A-Z0-9_\\.\"'`]+)", QUERY_DDL_TYPE.JOBS, QUERY_DDL_STATUS.DROP),
	
	PROCEDURE_CREATE("CREATE\\s+PROCEDURE\\s+([A-Z0-9_\\.\"'`]+)", QUERY_DDL_TYPE.PROCEDURE, QUERY_DDL_STATUS.CREATE),
	PROCEDURE_CREATE_REPLACE("CREATE\\s+OR\\s+REPLACE\\s+PROCEDURE\\s+([A-Z0-9_\\.\"'`]+)", QUERY_DDL_TYPE.PROCEDURE, QUERY_DDL_STATUS.CREATE),
	PROCEDURE_ALTER("ALTER\\s+PROCEDURE\\s+([A-Z0-9_\\.\"'`]+)", QUERY_DDL_TYPE.PROCEDURE, QUERY_DDL_STATUS.ALTER),
	PROCEDURE_DROP("DROP\\s+PROCEDURE\\s+([A-Z0-9_\\.\"'`]+)", QUERY_DDL_TYPE.PROCEDURE, QUERY_DDL_STATUS.DROP),
	
	FUNCTION_CREATE("CREATE\\s+FUNCTION\\s+([A-Z0-9_\\.\"'`]+)", QUERY_DDL_TYPE.FUNCTION, QUERY_DDL_STATUS.CREATE),
	FUNCTION_CREATE_REPLACE("CREATE\\s+OR\\s+REPLACE\\s+FUNCTION\\s+([A-Z0-9_\\.\"'`]+)", QUERY_DDL_TYPE.FUNCTION, QUERY_DDL_STATUS.CREATE),
	FUNCTION_ALTER("ALTER\\s+FUNCTION\\s+([A-Z0-9_\\.\"'`]+)", QUERY_DDL_TYPE.FUNCTION, QUERY_DDL_STATUS.ALTER),
	FUNCTION_DROP("DROP\\s+FUNCTION\\s+([A-Z0-9_\\.\"'`]+)", QUERY_DDL_TYPE.FUNCTION, QUERY_DDL_STATUS.DROP),
	
	PACKEAGE_CREATE("CREATE\\s+PACKAGE\\s+([A-Z0-9_\\.\"'`]+)", QUERY_DDL_TYPE.PACKAGE, QUERY_DDL_STATUS.CREATE),
	PACKEAGE_CREATE_REPLACE("CREATE\\s+OR\\s+REPLACE\\s+PACKAGE\\s+([A-Z0-9_\\.\"'`]+)", QUERY_DDL_TYPE.PACKAGE, QUERY_DDL_STATUS.CREATE),
	PACKAGE_ALTER("ALTER\\s+PACKAGE\\s+([A-Z0-9_\\.\"'`]+)", QUERY_DDL_TYPE.PACKAGE, QUERY_DDL_STATUS.ALTER),
	PACKAGE_DROP("DROP\\s+PACKAGE\\s+([A-Z0-9_\\.\"'`]+)", QUERY_DDL_TYPE.PACKAGE, QUERY_DDL_STATUS.DROP),
	
	TRIGGER_CREATE("CREATE\\s+TRIGGER\\s+([A-Z0-9_\\.\"'`]+)", QUERY_DDL_TYPE.TRIGGER, QUERY_DDL_STATUS.CREATE),
	TRIGGER_CREATE_REPLACE("CREATE\\s+OR\\s+REPLACE\\s+TRIGGER\\s+([A-Z0-9_\\.\"'`]+)", QUERY_DDL_TYPE.TRIGGER, QUERY_DDL_STATUS.CREATE),
	TRIGGER_ALTER("ALTER\\s+TRIGGER\\s+([A-Z0-9_\\.\"'`]+)", QUERY_DDL_TYPE.TRIGGER, QUERY_DDL_STATUS.ALTER),
	TRIGGER_DROP("DROP\\s+TRIGGER\\s+([A-Z0-9_\\.\"'`]+)", QUERY_DDL_TYPE.TRIGGER, QUERY_DDL_STATUS.DROP),
	
	DBLINK_CREATE("CREATE\\s+DATABASE\\s+LINK\\s+([A-Z0-9_\\.\"'`]+)", QUERY_DDL_TYPE.LINK, QUERY_DDL_STATUS.CREATE),
	DBLINK_CREATE_SHARED("CREATE\\s+SHARED\\s+DATABASE\\s+LINK\\s+([A-Z0-9_\\.\"'`]+)", QUERY_DDL_TYPE.LINK, QUERY_DDL_STATUS.CREATE),
	DBLINK_CREATE_PUBLIC("CREATE\\s+PUBLIC\\s+DATABASE\\s+LINK\\s+([A-Z0-9_\\.\"'`]+)", QUERY_DDL_TYPE.LINK, QUERY_DDL_STATUS.CREATE),
	DBLINK_CREATE_PUBLIC_SHARED("CREATE\\s+SHARED\\s+PUBLIC\\s+DATABASE\\s+LINK\\s+([A-Z0-9_\\.\"'`]+)", QUERY_DDL_TYPE.LINK, QUERY_DDL_STATUS.CREATE),
	DBLINK_ALTER("ALTER\\s+DATABASE\\s+LINK\\s+([A-Z0-9_\\.\"'`]+)", QUERY_DDL_TYPE.LINK, QUERY_DDL_STATUS.ALTER),
	DBLINK_ALTER_SHARED("ALTER\\s+SHARED\\s+DATABASE\\s+LINK\\s+([A-Z0-9_\\.\"'`]+)", QUERY_DDL_TYPE.LINK, QUERY_DDL_STATUS.ALTER),
	DBLINK_ALTER_PUBLIC("ALTER\\s+PUBLIC\\s+DATABASE\\s+LINK\\s+([A-Z0-9_\\.\"'`]+)", QUERY_DDL_TYPE.LINK, QUERY_DDL_STATUS.ALTER),
	DBLINK_ALTER_PUBLIC_SHARED("ALTER\\s+SHARED\\s+PUBLIC\\s+DATABASE\\s+LINK\\s+([A-Z0-9_\\.\"'`]+)", QUERY_DDL_TYPE.LINK, QUERY_DDL_STATUS.ALTER),
	DBLINK_DROP("DROP\\s+DATABASE\\s+LINK\\s+([A-Z0-9_\\.\"'`]+)", QUERY_DDL_TYPE.LINK, QUERY_DDL_STATUS.DROP)
	
	// java
	
	;
	
	private String regExp;
	private QUERY_DDL_TYPE ddlType;
	private QUERY_DDL_STATUS ddlStatus;
	private DefineDDL(String regExp, QUERY_DDL_TYPE ddlType, QUERY_DDL_STATUS ddlStatus) {
		this.regExp = regExp;
		this.ddlType = ddlType;
		this.ddlStatus = ddlStatus;
	}
	/**
	 * @return the regExp
	 */
	public String getRegExp() {
		return regExp;
	}
	/**
	 * @param regExp the regExp to set
	 */
	public void setRegExp(String regExp) {
		this.regExp = regExp;
	}
	/**
	 * @return the ddlType
	 */
	public QUERY_DDL_TYPE getDdlType() {
		return ddlType;
	}
	/**
	 * @param ddlType the ddlType to set
	 */
	public void setDdlType(QUERY_DDL_TYPE ddlType) {
		this.ddlType = ddlType;
	}
	/**
	 * @return the ddlStatus
	 */
	public QUERY_DDL_STATUS getDdlStatus() {
		return ddlStatus;
	}
	/**
	 * @param ddlStatus the ddlStatus to set
	 */
	public void setDdlStatus(QUERY_DDL_STATUS ddlStatus) {
		this.ddlStatus = ddlStatus;
	}
	
}
