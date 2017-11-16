/*******************************************************************************
 * Copyright (c) 2013 hangum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     hangum - initial API and implementation
 ******************************************************************************/
package com.hangum.tadpole.engine.sql.util;

import java.util.Date;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.hangum.tadpole.commons.libs.core.define.PublicTadpoleDefine;
import com.hangum.tadpole.commons.libs.core.define.PublicTadpoleDefine.OBJECT_TYPE;
import com.hangum.tadpole.db.metadata.TadpoleMetaData;
import com.hangum.tadpole.engine.define.DBGroupDefine;
import com.hangum.tadpole.engine.manager.TadpoleSQLManager;
import com.hangum.tadpole.engine.query.dao.mysql.TableDAO;
import com.hangum.tadpole.engine.query.dao.system.UserDBDAO;

import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;

/**
 * <pre>
 *  java.sql.ResultSet과 ResultSetMeta를 TableViewer로 바꾸기 위해 가공하는 Util
 *  
 *  resource데이터를 저장하기 위해 data를 배열화시킨다.
 * </pre>
 * 
 * @author hangum
 *
 */
public class SQLUtil {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(SQLUtil.class);
	
	/** REGEXP pattern flag */
	private static final int PATTERN_FLAG = Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL;
	
	/**
	 * pattern statement 
	 * 
	 * <PRE>
	 * 		CHECK는 MYSQL의 CHECK TABLE VIEW_TABLE_NAME; 명령으로 VIEW의 정보를 볼수 있습니다.
	 * 		PRAGMA는 sqlite의 시스템 쿼리 얻는 거.
	 * </PRE>
	 */
	private static final String MSSQL_PATTERN_STATEMENT = "|^SP_HELP.*|^EXEC.*";
	private static final String ORACLE_PATTERN_STATEMENT = "";
	private static final String MYSQL_PATTERN_STATEMENT = "|^CALL.*";
	private static final String PGSQL_PATTERN_STATEMENT = "";
	private static final String SQLITE_PATTERN_STATEMENT = "";
	private static final String CUBRID_PATTERN_STATEMENT = "";
	
	private static final String BASE_PATTERN_STATEMENT = "^SELECT.*|^EXPLAIN.*|^SHOW.*|^DESCRIBE.*|^DESC.*|^CHECK.*|^PRAGMA.*|^WITH.*|^OPTIMIZE.*" 
							+ MSSQL_PATTERN_STATEMENT
							+ ORACLE_PATTERN_STATEMENT
							+ MYSQL_PATTERN_STATEMENT
							+ PGSQL_PATTERN_STATEMENT
							+ SQLITE_PATTERN_STATEMENT
							+ CUBRID_PATTERN_STATEMENT;
	private static final Pattern PATTERN_DML_BASIC = Pattern.compile(BASE_PATTERN_STATEMENT, PATTERN_FLAG);
	
	/** 허용되지 않는 sql 정의 */
//	private static final String[] NOT_ALLOWED_SQL = {
		/* MSSQL- USE DATABASE명 */
//		"USE"
//	};
	
	/**
	 * tadpole 에서 사용하는 특수 컬럼여부 
	 * 0번째 컬럼 #과 {@code PublicTadpoleDefine#SPECIAL_USER_DEFINE_HIDE_COLUMN}
	 * 
	 * @param strColumnName
	 * @return
	 */
	public static boolean isTDBSpecialColumn(String strColumnName) {
		if(StringUtils.equals(strColumnName, "#") || StringUtils.startsWithIgnoreCase(strColumnName, PublicTadpoleDefine.SPECIAL_USER_DEFINE_HIDE_COLUMN)) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * remove comment
	 * 
	 * @param strSQL
	 * @return
	 */
	public static String removeComment(String strSQL) {

//		try {
//			Pattern regex = Pattern.compile("(?:/\\*[^;]*?\\*/)|(?:--[^;]*?$)", Pattern.DOTALL | Pattern.MULTILINE);
//		    Matcher regexMatcher = regex.matcher(subjectString);
//		    while (regexMatcher.find()) {
//		        // matched text: regexMatcher.group()
//		        // match start: regexMatcher.start()
//		        // match end: regexMatcher.end()
//		    } 
//		} catch (PatternSyntaxException ex) {
//		    // Syntax error in the regular expression
//		}

		if(null == strSQL) return "";
		String strCheckSQL = strSQL.replaceAll("(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?:--.*)", "");
		strCheckSQL = StringUtils.trimToEmpty(strCheckSQL);
		return strCheckSQL;
	}
	
	/**
	 * SQL의 DML, DDL 을 테스트 하기위 사용하는 
	 * @param strSQL
	 * @return
	 */
	public static String makeSQLTestString(String strSQL) {
		String strCheckSQL = removeComment(strSQL);
		strCheckSQL = StringUtils.removeStart(strCheckSQL, "(");
		strCheckSQL = StringUtils.trimToEmpty(strCheckSQL);
		return strCheckSQL;
	}
	
//	/**
//	 * 쿼리중에 허용하지 않는 쿼리 목록.
//	 * 쿼리문 위에 주석을 빼야... -- / ** * / / * * /
//	 * 
//	 * @param strSQL
//	 * @return
//	 */
//	public static boolean isNotAllowed(String strSQL) {
//		boolean isRet = false;
//		String cmpSql = removeComment(strSQL);
//		
//		for (String strNAllSQL : NOT_ALLOWED_SQL) {
//			if(StringUtils.startsWithIgnoreCase(cmpSql, strNAllSQL)) {
//				return true;
//			}
//		}
//		
//		return isRet;
//	}
	
	/**
	 * 쿼리의 패턴이 <code>PATTERN_STATEMENT</code>인지?
	 * 
	 * @param strSQL
	 * @return
	 */
	public static boolean isStatement(String strSQL) {
		strSQL = makeSQLTestString(strSQL);
		if((PATTERN_DML_BASIC.matcher(strSQL)).matches()) {
			return true;
//		} else {
//			try {
			//	// 영문일때만 검사하도록 합니다. 영문이 아닐 경우 무조건 false 입니다.
			//	// 검사를 하는 이유는 한글이 파서에 들어가면 무한루프돌면서 에디터 전체가 데드락으로 빠집니다.
//			//	//if(!isEnglish(strSQL)) return false;
//				
//				CCJSqlParserManager parserManager = new CCJSqlParserManager();
//				Statement statement = parserManager.parse(new StringReader(strSQL));
//				if(statement instanceof Select) return true;
//			} catch(Exception e) {
//				logger.error("SQL Parser Exception.\n sql is [" + strSQL + "]");
//			}
//			return false;
		}
		
		return false;
	}
	
	/**
	 * sql 관련 없는 모든 코드를 삭제한다.
	 * 
	 * @param userDB
	 * @param exeSQL
	 * @return
	 */
	public static String removeCommentAndOthers(UserDBDAO userDB, String exeSQL) {
		exeSQL = StringUtils.trimToEmpty(exeSQL);
		exeSQL = removeComment(exeSQL);
		exeSQL = StringUtils.trimToEmpty(exeSQL);
		exeSQL = StringUtils.removeEnd(exeSQL, "/");
		exeSQL = StringUtils.trimToEmpty(exeSQL);
		//TO DO 오라클 프로시저등의 오브젝트는 마지막 딜리미터(;)가 없으면 오류입니다. 하여서 이 코드는 문제입니다.
		exeSQL = StringUtils.removeEnd(exeSQL, PublicTadpoleDefine.SQL_DELIMITER);
		
		return exeSQL;
	}
	
	/**
	 * 쿼리를 jdbc에서 실행 가능한 쿼리로 보정합니다.
	 * 
	 * @param userDB
	 * @param exeSQL
	 * @return
	 */
	public static String makeExecutableSQL(UserDBDAO userDB, String exeSQL) {
		
//		tmpStrSelText = UnicodeUtils.getUnicode(tmpStrSelText);
			
//			https://github.com/hangum/TadpoleForDBTools/issues/140 오류로 불럭지정하였습니다.
//			TO DO 특정 쿼리에서는 주석이 있으면 오류인데..DB에서 쿼리를 실행받는 다양한 조건을 고려할 필요가 있습니다. 
		
		// 문장 의 // 뒤에를 주석으로 인식 쿼리열에서 제외합니다.
		/*
		 *  mysql의 경우 주석문자 즉, -- 바로 다음 문자가 --와 붙어 있으면 주석으로 인식하지 않아 오류가 발생합니다. --comment 이면 주석으로 인식하지 않습니다.(다른 디비(mssql, oralce, pgsql)은 주석으로 인식합니다)
		 *  고칠가 고민하지만, 실제 쿼리에서도 동일하게 오류로 처리할 것이기에 주석을 지우지 않고 놔둡니다. - 2013.11.11- (hangum)
		 */

		exeSQL = StringUtils.trimToEmpty(exeSQL);

		// 주석제거.
		// oracle, tibero, altibase은 힌트가 주석 문법을 쓰므로 주석을 삭제하지 않는다.
		
		if(DBGroupDefine.ORACLE_GROUP == userDB.getDBGroup()
				|| DBGroupDefine.MYSQL_GROUP == userDB.getDBGroup()
				|| DBGroupDefine.ALTIBASE_GROUP == userDB.getDBGroup()
		) {
			// ignore code
		} else {
			exeSQL = removeComment(exeSQL);
		}
		
		// 주석으로 종료되는 행이면 지우지 않도록 수정.
		exeSQL = StringUtils.trimToEmpty(exeSQL);
		if(!StringUtils.endsWith(exeSQL, "*/")) { 
			exeSQL = StringUtils.removeEnd(exeSQL, "/");
		}
		exeSQL = StringUtils.trimToEmpty(exeSQL);
		
		//TO DO 오라클 프로시저등의 오브젝트는 마지막 딜리미터(;)가 없으면 오류입니다. 하여서 이 코드는 문제입니다.
		exeSQL = StringUtils.removeEnd(exeSQL, PublicTadpoleDefine.SQL_DELIMITER);
		
		return exeSQL;
	}
	
	/**
	 * 쿼리에 사용 할 Table, column name을 만듭니다.
	 * 
	 * @param userDB
	 * @param name
	 * @return
	 */
	public static String makeIdentifierName(UserDBDAO userDB, String name) {
		boolean isChanged = false;
		name = name == null ? "" : name;
		String retStr = name;

		//
		// 오라클 평선의 파라미터 중에 리턴값의 아규먼트 명칭은 널이다.  
		//
		TadpoleMetaData tmd = TadpoleSQLManager.getDbMetadata(userDB);
		if(tmd == null) return name;

		//
		// mssql일 경우 시스템 테이블 스키서부터 "가 붙여 있는 경우 "가 있으면 []을 양쪽에 붙여 줍니다. --;;
		//
		if(DBGroupDefine.MSSQL_GROUP == userDB.getDBGroup()) {
			if(StringUtils.contains(name, "\"")) {
				return name = String.format("[%s]", name);
			}
		}
		
		// 정의 된 형태로 오브젝트 명을 변경한다.
		switch(tmd.getSTORE_TYPE()) {
		case NONE: 
			// 오브젝트명이 전부 대문자로 변경한것과도 틀리고 전부 소문자로 변경한것과도 틀린경우 대, 소문자가 혼합된 명칭으로 간주하고 구분자를 추가해 준다.
			if(!StringUtils.equals(name, StringUtils.lowerCase(name)) && !StringUtils.equals(name, StringUtils.upperCase(name)) ) {
				isChanged = true;
				retStr = makeFullyTableName(name, tmd.getIdentifierQuoteString());
			}
			break;
		case BLANK: 
			if(name.matches(".*\\s.*")) {
				isChanged = true;
				retStr = makeFullyTableName(name, tmd.getIdentifierQuoteString());
			}
			break;
		case LOWCASE_BLANK:
			if(name.matches(".*[a-z\\s].*")) {
				isChanged = true;
				retStr = makeFullyTableName(name, tmd.getIdentifierQuoteString());
			}else if(name.matches(".*[.].*")) {
				isChanged = true;
				retStr = makeFullyTableName(name, tmd.getIdentifierQuoteString());
			}
			break;
		case UPPERCASE_BLANK:
			if(name.matches(".*[A-Z\\s].*")) {
				isChanged = true;
				retStr = makeFullyTableName(name, tmd.getIdentifierQuoteString());
			}else if(name.matches(".*[.].*")) {
				isChanged = true;
				retStr = makeFullyTableName(name, tmd.getIdentifierQuoteString());
			}
			break;
		}
		
		// 키워드 인지 검사하여 오브젝트 명을 변경한다.
		if(!isChanged) {
			if(StringUtils.containsIgnoreCase(","+tmd.getKeywords()+",", ","+retStr+",")) {
				retStr = tmd.getIdentifierQuoteString() + name + tmd.getIdentifierQuoteString();
			}
		}
		
		return retStr;
	}

	/**
	 * remove identifier quote string
	 * 
	 * @param userDB
	 * @param name
	 * @return
	 */
	public static String removeIdentifierQuoteString(UserDBDAO userDB, String name) {
		TadpoleMetaData tmd = TadpoleSQLManager.getDbMetadata(userDB);
		if(tmd == null) return name;

		return StringUtils.replace(name, tmd.getIdentifierQuoteString(), "");
	}

	/**
	 * make fully table name
	 * @param tableName
	 * @param strIdentifier
	 * @return
	 */
	private static String makeFullyTableName(String tableName, String strIdentifier) {
		return strIdentifier + tableName + strIdentifier;
	}
	
	/**
	 * 에디터에서 쿼리 실행 단위 조절.
	 * 
	 * https://github.com/hangum/TadpoleForDBTools/issues/466
	 * 
	 * 오라클 디비링크 관련 스크립트는 SQL에디터를 사용하도록 OBJECT_TYPE.LINK 추가.
	 * 
	 * @param dbAction
	 * @return
	 */
	public static boolean isSELECTEditor(OBJECT_TYPE dbAction) {
		if(dbAction == OBJECT_TYPE.TABLES ||
				dbAction == OBJECT_TYPE.VIEWS ||
				dbAction == OBJECT_TYPE.SYNONYM ||
				dbAction == OBJECT_TYPE.INDEXES ||
				dbAction == OBJECT_TYPE.SEQUENCE ||
				dbAction == OBJECT_TYPE.LINK ||
				dbAction == OBJECT_TYPE.JOBS
				) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * sql of query type
	 * 
	 * @param sql
	 * @return query type
	 */
	public static PublicTadpoleDefine.QUERY_DML_TYPE sqlQueryType(String sql) {
		logger.debug("##[시작]####################################################################################");
		logger.debug(new Date());
		logger.debug(sql);
		
		PublicTadpoleDefine.QUERY_DML_TYPE queryType = PublicTadpoleDefine.QUERY_DML_TYPE.UNKNOWN;
		
		try {
			Statement statement = CCJSqlParserUtil.parse(sql);
			if(statement instanceof Select) {
				queryType = PublicTadpoleDefine.QUERY_DML_TYPE.SELECT;
			} else if(statement instanceof Insert) {
				queryType = PublicTadpoleDefine.QUERY_DML_TYPE.INSERT;
			} else if(statement instanceof Update) {
				queryType = PublicTadpoleDefine.QUERY_DML_TYPE.UPDATE;
			} else if(statement instanceof Delete) {
				queryType = PublicTadpoleDefine.QUERY_DML_TYPE.DELETE;
			} else {
				
//				queryType = PublicTadpoleDefine.QUERY_DML_TYPE.DDL;
			}
			
		} catch (Throwable e) {
			logger.error(String.format("sql parse exception. [ %s ]", sql));
			queryType = PublicTadpoleDefine.QUERY_DML_TYPE.UNKNOWN;
		}
		
		
		logger.debug(new Date());
		logger.debug("##[시작]####################################################################################");
		
		return queryType;
	}
	
	/**
	 * make quote mark
	 * 
	 * @param value
	 * @return
	 */
	public static String makeQuote(Object value) {
		if (null == value){
			return null;
		}else{
			return String.format("'%s'", StringEscapeUtils.escapeSql(value.toString()));
		}
	}

	/**
	 * Table name
	 * @param userDB 
	 * @param tableDAO
	 * @return
	 */
	public static String getTableName(UserDBDAO userDB, TableDAO tableDAO) {
		return tableDAO.getFullName();
	}
	
}
