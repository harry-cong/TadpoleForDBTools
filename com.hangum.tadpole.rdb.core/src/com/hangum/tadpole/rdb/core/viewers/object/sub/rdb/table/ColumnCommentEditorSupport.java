/*******************************************************************************
 * Copyright (c) 2013 hangum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     nilriri - initial API and implementation
 ******************************************************************************/
package com.hangum.tadpole.rdb.core.viewers.object.sub.rdb.table;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;

import com.hangum.tadpole.commons.libs.core.define.PublicTadpoleDefine;
import com.hangum.tadpole.engine.define.DBGroupDefine;
import com.hangum.tadpole.engine.query.dao.mysql.TableColumnDAO;
import com.hangum.tadpole.engine.query.dao.mysql.TableDAO;
import com.hangum.tadpole.engine.query.dao.system.UserDBDAO;
import com.hangum.tadpole.rdb.core.viewers.object.sub.utils.TableColumnObjectQuery;

/**
 * column comment editor
 * 
 * @author nilriri
 *
 */
public class ColumnCommentEditorSupport extends EditingSupport {
	private static final Logger logger = Logger.getLogger(ColumnCommentEditorSupport.class);

	private final TableViewer tableviewer;
	private final TableViewer viewer;
	private UserDBDAO userDB;
	private int column;

	/**
	 * 
	 * @param viewer
	 * @param explorer
	 */
	public ColumnCommentEditorSupport(TableViewer tableviewer, TableViewer viewer, UserDBDAO userDB, int column) {
		super(viewer);
		
		this.tableviewer = tableviewer;
		this.viewer = viewer;
		this.userDB = userDB;
		this.column = column;
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		if(column == 6) return new CommentCellEditor(column, viewer);
		else return null;
	}

	@Override
	protected boolean canEdit(Object element) {
		if(column == 6) {
			if(PublicTadpoleDefine.YES_NO.YES.name().equals(userDB.getIs_readOnlyConnect())) return false;
			
			if(PublicTadpoleDefine.YES_NO.NO.name().equals(userDB.getDbAccessCtl().getDdl_lock())) {
				if(DBGroupDefine.ORACLE_GROUP == userDB.getDBGroup() ||
						DBGroupDefine.POSTGRE_GROUP == userDB.getDBGroup() ||
						DBGroupDefine.MSSQL_GROUP == userDB.getDBGroup() ||
						DBGroupDefine.MYSQL_GROUP == userDB.getDBGroup() ) {
					return true;
				}
			}
		}
		
		return false;
	}

	@Override
	protected Object getValue(Object element) {
		try {
			TableColumnDAO dao = (TableColumnDAO) element;
			String comment = dao.getComment();
			return comment == null ? "" : comment;
		} catch (Exception e) {
			logger.error("getValue error ", e);
			return "";
		}
	}

	@Override
	protected void setValue(Object element, Object value) {
		IStructuredSelection is = (IStructuredSelection) tableviewer.getSelection();
		final TableDAO tableDAO = (TableDAO)is.getFirstElement();
		
		String comment = "";
		try {
			TableColumnDAO columnDAO = (TableColumnDAO) element;
			comment = (String) (value == null ? "" : value);
			if(logger.isDebugEnabled()) logger.debug("dao column name is " + columnDAO.getField());
			
			// 기존 코멘트와 다를때만 db에 반영한다.
			if (!(comment.equals(columnDAO.getComment()))) {
				columnDAO.setComment(comment);
				TableColumnObjectQuery.updateComment(userDB, tableDAO, columnDAO);
			}

			viewer.update(element, null);
		} catch (Exception e) {
			logger.error("setValue error ", e);
		}
	}

}
