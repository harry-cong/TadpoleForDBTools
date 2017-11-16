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
package com.hangum.tadpole.rdb.core.viewers.object.sub.utils;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.hangum.tadpole.commons.dialogs.message.dao.RequestResultDAO;
import com.hangum.tadpole.engine.define.DBDefine;
import com.hangum.tadpole.engine.define.DBGroupDefine;
import com.hangum.tadpole.engine.query.dao.mysql.TableColumnDAO;
import com.hangum.tadpole.engine.query.dao.mysql.TableDAO;
import com.hangum.tadpole.engine.query.dao.system.UserDBDAO;
import com.hangum.tadpole.engine.sql.util.ExecuteDDLCommand;
import com.hangum.tadpole.engine.sql.util.RDBTypeToJavaTypeUtils;
import com.hangum.tadpole.engine.sql.util.SQLUtil;
import com.hangum.tadpole.rdb.core.dialog.table.mysql.TableColumnUpdateDAO;

/**
 * Execute table column object 
 * 
 * @author hangum
 *
 */
public class TableColumnObjectQuery {
	private static Logger logger = Logger.getLogger(TableColumnObjectQuery.class);
	
	/**
	 * modify table column type 
	 * 
	 * @param userDB
	 * @param tableDAO
	 * @param metaDataDao
	 * @param strTypeName
	 * @return
	 */
	public static RequestResultDAO modifyColumnType(final UserDBDAO userDB, TableDAO tableDAO, TableColumnUpdateDAO metaDataDao) throws Exception {
		RequestResultDAO reqReResultDAO = new RequestResultDAO();
		if(DBGroupDefine.MYSQL_GROUP == userDB.getDBGroup()) {
			// ALTER TABLE 테이블명 MODIFY 컬럼이름 새컬럼타입
			String strQuery = String.format("ALTER TABLE %s CHANGE %s %s %s", 
					tableDAO.getSysName(), metaDataDao.getColumnName(), metaDataDao.getColumnName(), metaDataDao.getDataType());
			ExecuteDDLCommand.executSQL(userDB, reqReResultDAO, strQuery);
			
		} else {
			throw new Exception("Not support rename table.");
		}
		
		return reqReResultDAO;
	}
	
	/**
	 * Rename table column 
	 * 
	 * @param userDB
	 * @param tableDAO
	 * @param oldColumnName
	 * @param newColumnName
	 * @return
	 */
	public static RequestResultDAO renameColumn(final UserDBDAO userDB, TableDAO tableDAO, TableColumnUpdateDAO metaDataDao, String newColumnName) throws Exception {
		RequestResultDAO reqReResultDAO = new RequestResultDAO();
		if(DBGroupDefine.MYSQL_GROUP == userDB.getDBGroup()) {
			//ALTER TABLE `dbtype` CHANGE `tesst` `cho` INT(11)  NULL  DEFAULT NULL;
			String strQuery = String.format("ALTER TABLE %s CHANGE %s %s %s", 
												tableDAO.getSysName(), metaDataDao.getColumnName(), newColumnName, metaDataDao.getDataType()
					);
			ExecuteDDLCommand.executSQL(userDB, reqReResultDAO, strQuery);
//		} else if(userDB.getDBDefine() == DBDefine.POSTGRE_DEFAULT |
//					userDB.getDBDefine() == DBDefine.ORACLE_DEFAULT |
//					userDB.getDBDefine() == DBDefine.SQLite_DEFAULT
//		) {
//			String strQuery = String.format("ALTER TABLE %s RENAME TO %s", tableDAO.getSysName(), oldColumnName);
//			resultDao = ExecuteDDLCommand.executSQL(userDB, strQuery);
//		} else if(userDB.getDBDefine() == DBDefine.MSSQL_DEFAULT | userDB.getDBDefine() == DBDefine.MSSQL_8_LE_DEFAULT) {
//			String strQuery = String.format("sp_rename %s, %s", tableDAO.getSysName(), oldColumnName);
//			resultDao = ExecuteDDLCommand.executSQL(userDB, strQuery);
//		} else if(userDB.getDBDefine() == DBDefine.CUBRID_DEFAULT) {
//			String strQuery = String.format("RENAME TABLE %s AS %s", tableDAO.getSysName(), oldColumnName);
//			resultDao = ExecuteDDLCommand.executSQL(userDB, strQuery);
		} else {
			throw new Exception("Not support rename table.");
		}
		
		return reqReResultDAO;
	}
	
	/**
	 * Delete table column
	 * 
	 * @param userDB
	 * @param tableColumnDao
	 * @return
	 */
	public static RequestResultDAO deleteColumn(final UserDBDAO userDB, final List<TableColumnDAO> listTableColumnDao) throws Exception {
		RequestResultDAO resultDao = null;
		if(DBGroupDefine.MYSQL_GROUP == userDB.getDBGroup() ||
				DBGroupDefine.MSSQL_GROUP == userDB.getDBGroup() ||
				DBGroupDefine.POSTGRE_GROUP == userDB.getDBGroup() ||
				DBGroupDefine.ORACLE_GROUP == userDB.getDBGroup() ||
				DBGroupDefine.CUBRID_GROUP == userDB.getDBGroup()
		) {
			for(TableColumnDAO tableColumnDao: listTableColumnDao) {
				//TODO: 테이블 컬럼명에 공백이나 특수문자가 있을경우 getSysName을 사용할 수 있도록 처리가 필요함.
				resultDao = deleteColumn(userDB, tableColumnDao.getTableDao().getFullName(), SQLUtil.makeIdentifierName(userDB, tableColumnDao.getField()));
			}
			
		} else {
			throw new Exception("Not support delete column.");
		}
		
		return resultDao;
	}
	
	/**
	 * delete column 
	 * @param userDB
	 * @param tableName
	 * @param columnName
	 * @return
	 * @throws Exception
	 */
	public static RequestResultDAO deleteColumn(final UserDBDAO userDB, String tableName, String columnName) throws Exception {
		RequestResultDAO reqReResultDAO = new RequestResultDAO();
		String strQuery = String.format("ALTER TABLE %s DROP COLUMN %s", tableName, columnName);
		ExecuteDDLCommand.executSQL(userDB, reqReResultDAO, strQuery); //$NON-NLS-1$
		
		return reqReResultDAO;
	}
	
	/**
	 * 컬럼을 추가합니다. 
	 * 
	 * @param userDB
	 * @param tableDAO
	 * @param metaDataDao
	 */
	public static RequestResultDAO addColumn(final UserDBDAO userDB, final TableDAO tableDAO, final TableColumnUpdateDAO metaDataDao) throws Exception {
		RequestResultDAO addColumnResultDAO = new RequestResultDAO();
		
		String strQuery = String.format("ALTER TABLE %s ADD COLUMN %s %s %s COMMENT %s ", 
											tableDAO.getFullName(), 
											SQLUtil.makeIdentifierName(userDB, metaDataDao.getColumnName()), 
											metaDataDao.getDataType(), 
											metaDataDao.isNotNull()?"NOT NULL":"NULL", 
											SQLUtil.makeQuote(metaDataDao.getComment())
				);
		
		if(!"".equals(metaDataDao.getCollation())) { 
			strQuery += String.format(" COLLATE %s ", metaDataDao.getCollation());
		}
		if(metaDataDao.isPrimaryKey()) {
			strQuery += " PRIMARY KEY ";
		}
		if(metaDataDao.isAutoIncrement()) {
			strQuery += " auto_increment ";
		} else {
			if(!"".equals(StringUtils.trimToEmpty(metaDataDao.getDefaultValue()))) {
				strQuery += String.format(" DEFAULT %s ", SQLUtil.makeQuote(metaDataDao.getDefaultValue()));
			}
		}
		
		ExecuteDDLCommand.executSQL(userDB, addColumnResultDAO, strQuery); //$NON-NLS-1$	
		return addColumnResultDAO;
	}
	
	/**
	 * update column
	 * 
	 * @param userDB
	 * @param tableDAO
	 * @param tableColumnDAO 
	 * @param metaDataDao
	 * @return
	 * @throws Exception
	 */
	public static RequestResultDAO updateColumn(final UserDBDAO userDB, final TableDAO tableDAO, TableColumnDAO tableColumnDAO, final TableColumnUpdateDAO metaDataDao) throws Exception {
		RequestResultDAO addColumnResultDAO = new RequestResultDAO();
		
		String strQuery = String.format("ALTER TABLE %s CHANGE COLUMN %s %s %s %s COMMENT %s ", 
											tableDAO.getFullName(), 
											SQLUtil.makeIdentifierName(userDB, tableColumnDAO.getField()),
											SQLUtil.makeIdentifierName(userDB, metaDataDao.getColumnName()), 
											metaDataDao.getDataType(), 
											metaDataDao.isNotNull()?"NOT NULL":"NULL", 
											SQLUtil.makeQuote(metaDataDao.getComment())
				);
		
		if(!"".equals(metaDataDao.getCollation())) { 
			strQuery += String.format(" COLLATE %s ", metaDataDao.getCollation());
		}
		if(metaDataDao.isPrimaryKey()) {
			strQuery += " PRIMARY KEY ";
		}
		if(metaDataDao.isAutoIncrement()) {
			strQuery += " auto_increment ";
		} else {
			if(!"".equals(StringUtils.trimToEmpty(metaDataDao.getDefaultValue()))) {
				strQuery += String.format(" DEFAULT %s ", SQLUtil.makeQuote(metaDataDao.getDefaultValue()));
			}
		}
		
		ExecuteDDLCommand.executSQL(userDB, addColumnResultDAO, strQuery);	
		return addColumnResultDAO;
	}
	
	/**
	 * update comment
	 * 
	 * @param userDB
	 * @param tableDAO
	 * @param columnDAO
	 */
	public static void updateComment(final UserDBDAO userDB, final TableDAO tableDAO, TableColumnDAO columnDAO) throws Exception {
		RequestResultDAO reqReResultDAO = new RequestResultDAO();
		StringBuffer query = new StringBuffer();

		if (DBGroupDefine.ORACLE_GROUP == userDB.getDBGroup() || DBGroupDefine.POSTGRE_GROUP == userDB.getDBGroup()) {
			String strQuery = String.format("COMMENT ON COLUMN %s.%s IS %s", tableDAO.getSysName(), columnDAO.getField(), SQLUtil.makeQuote(columnDAO.getComment()));
			
			try{
				ExecuteDDLCommand.executSQL(userDB, reqReResultDAO, strQuery);
			}catch(Exception e){
				//  org.postgresql.util.PSQLException: No results were returned by the query.
			}

		} else if (userDB.getDBDefine() == DBDefine.MSSQL_8_LE_DEFAULT) {
			query.append(" exec sp_dropextendedproperty 'MS_Description' ").append(", 'user' ,").append(userDB.getUsers());
			query.append(",'table' , '").append(tableDAO.getSysName()).append("'");
			query.append(",'column' , '").append(columnDAO.getSysName()).append("'");
			try{
				ExecuteDDLCommand.executSQL(userDB, reqReResultDAO, query.toString());
			}catch(Exception e){
				// 주석이 최초로 등록될때는 삭제될 주석이 없으므로 오류 발생함.
			}

			query = new StringBuffer();
			query.append(" exec sp_addextendedproperty 'MS_Description', '").append(columnDAO.getComment()).append("' ,'user' ,").append(userDB.getUsers());
			query.append(",'table' , '").append(tableDAO.getSysName()).append("'");
			query.append(",'column', '").append(columnDAO.getSysName()).append("'");
			ExecuteDDLCommand.executSQL(userDB, reqReResultDAO, query.toString());

		} else if (userDB.getDBDefine() == DBDefine.MSSQL_DEFAULT ) {
			query.append(" exec sp_dropextendedproperty 'MS_Description' ").append(", 'schema' , " + tableDAO.getSchema_name());
			query.append(",'table' , '").append(tableDAO.getTable_name()).append("'");
			query.append(",'column' , '").append(columnDAO.getSysName()).append("'");
			try{
				ExecuteDDLCommand.executSQL(userDB, reqReResultDAO, query.toString());
			}catch(Exception e){
				// 주석이 최초로 등록될때는 삭제될 주석이 없으므로 오류 발생함.
			}

			query = new StringBuffer();
			query.append(" exec sp_addextendedproperty 'MS_Description', '").append(columnDAO.getComment()).append("' ,'schema' , " + tableDAO.getSchema_name());
			query.append(",'table' , '").append(tableDAO.getTable_name()).append("'");
			query.append(",'column', '").append(columnDAO.getSysName()).append("'");
			ExecuteDDLCommand.executSQL(userDB, reqReResultDAO, query.toString());

		} else if (DBGroupDefine.MYSQL_GROUP == userDB.getDBGroup()) {

			String strQuery = String.format("ALTER TABLE %s CHANGE %s %s %s %s COMMENT %s", 
											tableDAO.getFullName(),
											SQLUtil.makeIdentifierName(userDB, columnDAO.getField()), 
											SQLUtil.makeIdentifierName(userDB, columnDAO.getField()), 
											columnDAO.getType(), ("NO".equals(columnDAO.getNull())?"NOT NULL":"NULL"), 
											SQLUtil.makeQuote(columnDAO.getComment()));
			ExecuteDDLCommand.executSQL(userDB, reqReResultDAO, strQuery);
			
			if (null != columnDAO.getDefault()){
				if (RDBTypeToJavaTypeUtils.isNumberType(columnDAO.getType())) {
					strQuery = String.format("ALTER TABLE %s ALTER %s SET DEFAULT %s", tableDAO.getFullName(), SQLUtil.makeIdentifierName(userDB, columnDAO.getField()), columnDAO.getDefault());
					ExecuteDDLCommand.executSQL(userDB, reqReResultDAO, strQuery);
				} else if(RDBTypeToJavaTypeUtils.isDateType(columnDAO.getType())) {
					// nothing
				}else{
					strQuery = String.format("ALTER TABLE %s ALTER %s SET DEFAULT %s", tableDAO.getFullName(), SQLUtil.makeIdentifierName(userDB, columnDAO.getField()), SQLUtil.makeQuote(columnDAO.getDefault()));
					ExecuteDDLCommand.executSQL(userDB, reqReResultDAO, strQuery);
				}
			}
		}
	}

}
