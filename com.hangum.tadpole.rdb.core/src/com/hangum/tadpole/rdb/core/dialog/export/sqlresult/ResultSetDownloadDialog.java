/*******************************************************************************
 * Copyright (c) 2016 hangum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     hangum - initial API and implementation
 ******************************************************************************/
package com.hangum.tadpole.rdb.core.dialog.export.sqlresult;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.hangum.tadpole.commons.dialogs.message.dao.RequestResultDAO;
import com.hangum.tadpole.commons.libs.core.dao.LicenseDAO;
import com.hangum.tadpole.commons.libs.core.define.PublicTadpoleDefine;
import com.hangum.tadpole.commons.libs.core.message.CommonMessages;
import com.hangum.tadpole.commons.libs.core.utils.LicenseValidator;
import com.hangum.tadpole.commons.util.GlobalImageUtils;
import com.hangum.tadpole.commons.util.TadpoleWidgetUtils;
import com.hangum.tadpole.commons.util.download.DownloadServiceHandler;
import com.hangum.tadpole.commons.util.download.DownloadUtils;
import com.hangum.tadpole.engine.query.sql.TadpoleSystem_ExecutedSQL;
import com.hangum.tadpole.engine.sql.util.SQLConvertCharUtil;
import com.hangum.tadpole.engine.sql.util.export.AllDataExporter;
import com.hangum.tadpole.engine.sql.util.export.CSVExpoter;
import com.hangum.tadpole.engine.sql.util.export.HTMLExporter;
import com.hangum.tadpole.engine.sql.util.export.JsonExpoter;
import com.hangum.tadpole.engine.sql.util.export.SQLExporter;
import com.hangum.tadpole.engine.sql.util.export.XMLExporter;
import com.hangum.tadpole.engine.sql.util.resultset.QueryExecuteResultDTO;
import com.hangum.tadpole.engine.utils.RequestQuery;
import com.hangum.tadpole.preference.define.GetAdminPreference;
import com.hangum.tadpole.rdb.core.Activator;
import com.hangum.tadpole.rdb.core.Messages;
import com.hangum.tadpole.rdb.core.dialog.export.sqlresult.composite.AbstractExportComposite;
import com.hangum.tadpole.rdb.core.dialog.export.sqlresult.composite.ExportExcelComposite;
import com.hangum.tadpole.rdb.core.dialog.export.sqlresult.composite.ExportHTMLComposite;
import com.hangum.tadpole.rdb.core.dialog.export.sqlresult.composite.ExportJSONComposite;
import com.hangum.tadpole.rdb.core.dialog.export.sqlresult.composite.ExportSQLComposite;
import com.hangum.tadpole.rdb.core.dialog.export.sqlresult.composite.ExportTextComposite;
import com.hangum.tadpole.rdb.core.dialog.export.sqlresult.composite.ExportXMLComposite;
import com.hangum.tadpole.rdb.core.dialog.export.sqlresult.dao.AbstractExportDAO;
import com.hangum.tadpole.rdb.core.dialog.export.sqlresult.dao.ExportExcelDAO;
import com.hangum.tadpole.rdb.core.dialog.export.sqlresult.dao.ExportHtmlDAO;
import com.hangum.tadpole.rdb.core.dialog.export.sqlresult.dao.ExportJsonDAO;
import com.hangum.tadpole.rdb.core.dialog.export.sqlresult.dao.ExportSqlDAO;
import com.hangum.tadpole.rdb.core.dialog.export.sqlresult.dao.ExportTextDAO;
import com.hangum.tadpole.rdb.core.dialog.export.sqlresult.dao.ExportXmlDAO;
import com.hangum.tadpole.rdb.core.util.FindEditorAndWriteQueryUtil;

/**
 * Resultset to download
 * 
 * @author hangum
 */
public class ResultSetDownloadDialog extends Dialog {
	private static final Logger logger = Logger.getLogger(ResultSetDownloadDialog.class);
	
	/** null 기본값 */
	private String strDefaultNullValue = "";
	
	/** 다운로드 실행시에 사용할 쿼리 지정 */
	private String exeSQL = "";

	/** define max download limit */
	final int intMaxDownloadCnt = Integer.parseInt(GetAdminPreference.getQueryResultDownloadLimit());

	/** button status */
	public enum BTN_STATUS {PREVIEW, SENDEDITOR, DOWNLOAD};
	public BTN_STATUS btnStatus = BTN_STATUS.PREVIEW;
	
	/** 배열이 0부터 시작하므로 실제로는 5건. */ 
	private final int PREVIEW_COUNT = 4;
	private final int PREVIEW_ID = IDialogConstants.CLIENT_ID + 1;
	private final int SENDEDITOR_ID = IDialogConstants.CLIENT_ID + 2;
	
	private String defaultTargetName;
	private QueryExecuteResultDTO queryExecuteResultDTO;
	
	private CTabFolder tabFolder;
	private AbstractExportComposite compositeText;
	private AbstractExportComposite compositeExcel;
	private AbstractExportComposite compositeHTML;
	private AbstractExportComposite compositeJSON;
	private AbstractExportComposite compositeXML;
	private AbstractExportComposite compositeSQL;
	
	// preview 
	private Text textPreview;
	protected DownloadServiceHandler downloadServiceHandler;
	
	/**
	 * Create the dialog.
	 * @param parentShell
	 * @param requestQuery
	 * @param queryExecuteResultDTO 
	 * @param strDefTableName 
	 */
	public ResultSetDownloadDialog(Shell parentShell, RequestQuery requestQuery, String strDefTableName, QueryExecuteResultDTO queryExecuteResultDTO) {
		super(parentShell);
		setShellStyle(SWT.MAX | SWT.RESIZE | SWT.TITLE);
		
		this.defaultTargetName = strDefTableName;
		this.queryExecuteResultDTO = queryExecuteResultDTO;
		
		if(requestQuery.getSqlStatementType() == PublicTadpoleDefine.SQL_STATEMENT_TYPE.PREPARED_STATEMENT) {
			exeSQL = requestQuery.getSqlAddParameter();
		} else {
			exeSQL = requestQuery.getSql();
		}
		
		exeSQL = SQLConvertCharUtil.toServer(queryExecuteResultDTO.getUserDB(), exeSQL);
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		
		newShell.setText(Messages.get().ExportData);
		newShell.setImage(GlobalImageUtils.getTadpoleIcon());
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = (GridLayout) container.getLayout();
		gridLayout.verticalSpacing = 5;
		gridLayout.horizontalSpacing = 5;
		gridLayout.marginHeight = 5;
		gridLayout.marginWidth = 5;
		
		SashForm sashForm = new SashForm(container, SWT.VERTICAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		tabFolder = new CTabFolder(sashForm, SWT.NONE);
		tabFolder.setBorderVisible(false);
		tabFolder.setSelectionBackground(TadpoleWidgetUtils.getTabFolderBackgroundColor(), TadpoleWidgetUtils.getTabFolderPercents());
		
		compositeText = new ExportTextComposite(tabFolder, SWT.NONE, defaultTargetName);
		compositeText.setLayout(new GridLayout(1, false));
		
		compositeExcel = new ExportExcelComposite(tabFolder, SWT.NONE, defaultTargetName);
		compositeExcel.setLayout(new GridLayout(1, false));
		
		compositeHTML = new ExportHTMLComposite(tabFolder, SWT.NONE, defaultTargetName);
		compositeHTML.setLayout(new GridLayout(1, false));
		
		compositeJSON = new ExportJSONComposite(tabFolder, SWT.NONE, defaultTargetName);
		compositeJSON.setLayout(new GridLayout(1, false));
		
		compositeXML = new ExportXMLComposite(tabFolder, SWT.NONE, defaultTargetName);
		compositeXML.setLayout(new GridLayout(1, false));
		
		compositeSQL = new ExportSQLComposite(tabFolder, SWT.NONE, defaultTargetName, queryExecuteResultDTO.getColumnLabelName());
		compositeSQL.setLayout(new GridLayout(1, false));
		//--[tail]----------------------------------------------------------------------------------------
		Group groupPreview = new Group(sashForm, SWT.NONE);
		groupPreview.setText(Messages.get().PreviewMsg);
		groupPreview.setLayout(new GridLayout(1, false));
		
		textPreview = new Text(groupPreview, SWT.BORDER | SWT.MULTI);
		textPreview.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		//--[start]----------------------------------------------------------------------------------------
		sashForm.setWeights(new int[] {7,3});
		tabFolder.setSelection(0);
		//--[end]----------------------------------------------------------------------------------------
		
		registerServiceHandler();
		
		initUIData();
		
		return container;
	}
	
	private void initUIData() {
	}
	
	@Override
	protected void buttonPressed(int buttonId) {
		if(buttonId == IDialogConstants.CANCEL_ID) {
			super.buttonPressed(buttonId);			
		} else {
			if(buttonId == PREVIEW_ID) {
				btnStatus = BTN_STATUS.PREVIEW;
				this.textPreview.setText("");
				
			}else if(buttonId == SENDEDITOR_ID) {
				btnStatus = BTN_STATUS.SENDEDITOR;
			} else if(buttonId == IDialogConstants.OK_ID) {
				btnStatus = BTN_STATUS.DOWNLOAD;
			}
			
			executeButton();
		}
	}

	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, PREVIEW_ID, Messages.get().Preview, true);
		createButton(parent, SENDEDITOR_ID, Messages.get().SendEditor, false);
		createButton(parent, IDialogConstants.OK_ID, Messages.get().Download, false);
		createButton(parent, IDialogConstants.CANCEL_ID, CommonMessages.get().Close, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(500, 600);
	}
	
	@Override
	public boolean close() {
		unregisterServiceHandler();
		return super.close();
	}

	/** execute button */
	private void executeButton() {
		final String selectionTab = ""+tabFolder.getSelection().getData();
		AbstractExportDAO exportDAO = null;
		
		// validation
		if("text".equalsIgnoreCase(selectionTab)) {
			if(!compositeText.isValidate()) return;
			exportDAO = compositeText.getLastData();
		} else if("Excel".equalsIgnoreCase(selectionTab)) {
			if(!compositeText.isValidate()) return;
			exportDAO = compositeExcel.getLastData();
		}else if("html".equalsIgnoreCase(selectionTab)) {
			if(!compositeHTML.isValidate()) return;
			exportDAO = compositeHTML.getLastData();
		}else if("json".equalsIgnoreCase(selectionTab)) {			
			if(!compositeJSON.isValidate()) return;
			exportDAO = compositeJSON.getLastData();
		}else if("xml".equalsIgnoreCase(selectionTab)) {			
			if(!compositeXML.isValidate()) return;
			exportDAO = compositeXML.getLastData();
		}else if("sql".equalsIgnoreCase(selectionTab)) {			
			if(!compositeSQL.isValidate()) return;
			exportDAO = compositeSQL.getLastData();
		}else{
			if(logger.isDebugEnabled()) logger.debug("selection tab is " + selectionTab);	
			MessageDialog.openWarning(getShell(), CommonMessages.get().Warning, Messages.get().ResultSetDownloadDialog_notSelect); 
			return;
		}
		
		// job
		final String MSG_LoadingData = CommonMessages.get().LoadingData;;
		final AbstractExportDAO _dao = exportDAO;
		Job job = new Job(Messages.get().MainEditor_45) {
			@Override
			public IStatus run(IProgressMonitor monitor) {
				monitor.beginTask(MSG_LoadingData, IProgressMonitor.UNKNOWN);
				
				try {
					if("text".equalsIgnoreCase(selectionTab)) {			
						ExportTextDAO dao = (ExportTextDAO)_dao;
						exportResultCSVType(dao.isIsncludeHeader(), dao.getTargetName(), dao.getSeparatorType(), dao.getComboEncoding());
					} else if("Excel".equalsIgnoreCase(selectionTab)) {
						ExportExcelDAO dao = (ExportExcelDAO)_dao;
						exportResultExcelType(dao.getTargetName());
					}else if("html".equalsIgnoreCase(selectionTab)) {			
						ExportHtmlDAO dao = (ExportHtmlDAO)_dao;
						exportResultHtmlType(dao.getTargetName(), dao.getComboEncoding());
					}else if("json".equalsIgnoreCase(selectionTab)) {			
						ExportJsonDAO dao = (ExportJsonDAO)_dao;
						exportResultJSONType(dao.isIsncludeHeader(), dao.getTargetName(), dao.getSchemeKey(), dao.getRecordKey(), dao.getComboEncoding(), dao.isFormat());
					}else if("xml".equalsIgnoreCase(selectionTab)) {			
						ExportXmlDAO dao = (ExportXmlDAO)_dao;
						exportResultXmlType(dao.getTargetName(), dao.getComboEncoding());
					}else if("sql".equalsIgnoreCase(selectionTab)) {			
						ExportSqlDAO dao = (ExportSqlDAO)_dao;
						exportResultSqlType(dao.getTargetName(), dao.getComboEncoding(), dao.getListWhere(),  dao.getStatementType(), dao.getCommit());
					}
				} catch(Exception e) {
					logger.error(selectionTab + "type export error", e);
					return new Status(Status.WARNING, Activator.PLUGIN_ID, e.getMessage(), e);
				} finally {
					monitor.done();
				}
				
				return Status.OK_STATUS;
			}
		};
		
		// job의 event를 처리해 줍니다.
		job.addJobChangeListener(new JobChangeAdapter() {
			
			public void done(IJobChangeEvent event) {
				final IJobChangeEvent jobEvent = event; 
				
				final Display display = getShell().getDisplay();
				display.asyncExec(new Runnable() {
					public void run() {
						if(jobEvent.getResult().isOK()) {
//							MessageDialog.openInformation(getShell(), CommonMessages.get().OK, CommonMessages.get().DownloadIsComplete);
						} else {
							MessageDialog.openWarning(getShell(), CommonMessages.get().Warning, jobEvent.getResult().getMessage());
						}
					}	// end run
				});	// end display.asyncExec
				
			}	// end done
		});	// end job
		
		job.setName(Messages.get().DownloadQueryResult);
		job.setUser(true);
		job.schedule();
	}

	/**
	 * export csv type
	 * 
	 * @param isAddHead
	 * @param targetName
	 * @param seprator
	 * @param encoding
	 */
	protected void exportResultCSVType(boolean isAddHead, String targetName, char seprator, String encoding) throws Exception {
		if (btnStatus == BTN_STATUS.PREVIEW) {
			previewDataLoad(targetName, CSVExpoter.makeContent(isAddHead, queryExecuteResultDTO, seprator, PREVIEW_COUNT, strDefaultNullValue), encoding);
		}else if (btnStatus == BTN_STATUS.SENDEDITOR) {
			targetEditor(CSVExpoter.makeContent(isAddHead, queryExecuteResultDTO, seprator, strDefaultNullValue));
		}else{
			String strFullPath = AllDataExporter.makeCSVAllResult(queryExecuteResultDTO.getUserDB(), exeSQL, isAddHead, targetName, seprator, encoding, strDefaultNullValue, intMaxDownloadCnt);
			downloadFile(targetName, strFullPath, encoding);
		}
	}
	
	protected void exportResultExcelType(String targetName) throws Exception {
		if (btnStatus == BTN_STATUS.PREVIEW) {
			previewDataLoad(targetName, "", "UTF-8");
		}else if (btnStatus == BTN_STATUS.SENDEDITOR) {
//			targetEditor("에디터로 데이터를 보낼수 없습니다.");
		}else{
			String strFullPath = AllDataExporter.makeExcelAllResult(queryExecuteResultDTO.getUserDB(), exeSQL, targetName, intMaxDownloadCnt);
			downloadFile(targetName, strFullPath, "UTF-8");
		}
	}
	
	/**
	 * export html type
	 * 
	 * @param targetName
	 * @param encoding
	 */
	protected void exportResultHtmlType(String targetName, String encoding) throws Exception {
		if (btnStatus == BTN_STATUS.PREVIEW) {
			previewDataLoad(targetName, HTMLExporter.makeContent(targetName, queryExecuteResultDTO, PREVIEW_COUNT, strDefaultNullValue), encoding);
		}else if (btnStatus == BTN_STATUS.SENDEDITOR) {
			targetEditor(HTMLExporter.makeContent(targetName, queryExecuteResultDTO, strDefaultNullValue));
		}else{
			String strFullPath = AllDataExporter.makeHTMLAllResult(queryExecuteResultDTO.getUserDB(), exeSQL, targetName, encoding, strDefaultNullValue, intMaxDownloadCnt);
			downloadFile(targetName, strFullPath, encoding);
		}
	}
	
	/**
	 * export json type
	 * 
	 * @param isAddHead
	 * @param targetName
	 * @param schemeKey
	 * @param recordKey
	 * @param encoding
	 * @param isFormat
	 */
	protected void exportResultJSONType(boolean isAddHead, String targetName, String schemeKey, String recordKey, String encoding, boolean isFormat)  throws Exception {
		if (isAddHead){
			if (btnStatus == BTN_STATUS.PREVIEW) {
				previewDataLoad(targetName, JsonExpoter.makeHeadContent(targetName, queryExecuteResultDTO, schemeKey, recordKey, isFormat, PREVIEW_COUNT), encoding);
			}else if (btnStatus == BTN_STATUS.SENDEDITOR) {
				targetEditor(JsonExpoter.makeHeadContent(targetName, queryExecuteResultDTO, schemeKey, recordKey, isFormat, -1));
			}else{
				String strFullPath = AllDataExporter.makeJSONHeadAllResult(queryExecuteResultDTO.getUserDB(), exeSQL, targetName, schemeKey, recordKey, isFormat, encoding, strDefaultNullValue, intMaxDownloadCnt);
				downloadFile(targetName, strFullPath, encoding);
			}
		}else{
			if (btnStatus == BTN_STATUS.PREVIEW) {
				previewDataLoad(targetName, JsonExpoter.makeContent(targetName, queryExecuteResultDTO, isFormat, PREVIEW_COUNT), encoding);
			}else if (btnStatus == BTN_STATUS.SENDEDITOR) {
				targetEditor(JsonExpoter.makeContent(targetName, queryExecuteResultDTO, isFormat, -1));
			}else{
				String strFullPath = AllDataExporter.makeJSONAllResult(queryExecuteResultDTO.getUserDB(), exeSQL, targetName, isFormat, encoding, strDefaultNullValue, intMaxDownloadCnt);
				downloadFile(targetName, strFullPath, encoding);
			}
		}
	}
	
	/**
	 * export xml type
	 * 
	 * @param targetName
	 * @param encoding
	 */
	protected void exportResultXmlType(String targetName, String encoding) throws Exception {
		if (btnStatus == BTN_STATUS.PREVIEW) {
			previewDataLoad(targetName, XMLExporter.makeContent(targetName, queryExecuteResultDTO, PREVIEW_COUNT), encoding);
		}else if (btnStatus == BTN_STATUS.SENDEDITOR) {
			targetEditor(XMLExporter.makeContent(targetName, queryExecuteResultDTO));
		}else{
			String strFullPath = AllDataExporter.makeXMLResult(queryExecuteResultDTO.getUserDB(), exeSQL, targetName, encoding, strDefaultNullValue, intMaxDownloadCnt);
			downloadFile(targetName, strFullPath, encoding);
		}
	}
	
	/**
	 * export sql type
	 * @param targetName
	 * @param encoding
	 * @param listWhere
	 * @param stmtType
	 * @param commit
	 */
	protected void exportResultSqlType(String targetName, String encoding, List<String> listWhere, String stmtType, int commit) throws Exception {
			
		if ("batch".equalsIgnoreCase(stmtType)) {
			if (btnStatus == BTN_STATUS.PREVIEW) {
				previewDataLoad(targetName, SQLExporter.makeBatchInsertStatment(targetName, queryExecuteResultDTO, PREVIEW_COUNT, commit), encoding);
			}else if (btnStatus == BTN_STATUS.SENDEDITOR) {
				targetEditor(SQLExporter.makeBatchInsertStatment(targetName, queryExecuteResultDTO, -1, commit));
			}else{
				String strFullPath = AllDataExporter.makeFileBatchInsertStatment(queryExecuteResultDTO.getUserDB(), exeSQL, targetName, commit, encoding, strDefaultNullValue, intMaxDownloadCnt);
				downloadFile(targetName, strFullPath, encoding);
			}
		}else if ("insert".equalsIgnoreCase(stmtType)) {
			if (btnStatus == BTN_STATUS.PREVIEW) {
				previewDataLoad(targetName, SQLExporter.makeInsertStatment(targetName, queryExecuteResultDTO, PREVIEW_COUNT, commit), encoding);
			}else if (btnStatus == BTN_STATUS.SENDEDITOR) {
				targetEditor(SQLExporter.makeInsertStatment(targetName, queryExecuteResultDTO, -1, commit));
			}else{
				String strFullPath = AllDataExporter.makeFileInsertStatment(queryExecuteResultDTO.getUserDB(), exeSQL, targetName, commit, encoding, strDefaultNullValue, intMaxDownloadCnt);
				downloadFile(targetName, strFullPath, encoding);
			}
		}else if ("update".equalsIgnoreCase(stmtType)) {
			if (btnStatus == BTN_STATUS.PREVIEW) {
				previewDataLoad(targetName, SQLExporter.makeUpdateStatment(targetName, queryExecuteResultDTO, listWhere, PREVIEW_COUNT, commit), encoding);
			}else if (btnStatus == BTN_STATUS.SENDEDITOR) {
				targetEditor(SQLExporter.makeUpdateStatment(targetName, queryExecuteResultDTO, listWhere, -1, commit));
			}else{
				String strFullPath = AllDataExporter.makeFileUpdateStatment(queryExecuteResultDTO.getUserDB(), exeSQL, targetName, listWhere, commit, encoding, strDefaultNullValue, intMaxDownloadCnt);
				downloadFile(targetName, strFullPath, encoding);
			}
		}else if ("merge".equalsIgnoreCase(stmtType)) {
			if (btnStatus == BTN_STATUS.PREVIEW) {
				previewDataLoad(targetName, SQLExporter.makeMergeStatment(targetName, queryExecuteResultDTO, listWhere, PREVIEW_COUNT, commit), encoding);
			}else if (btnStatus == BTN_STATUS.SENDEDITOR) {
				targetEditor(SQLExporter.makeMergeStatment(targetName, queryExecuteResultDTO, listWhere, -1, commit));
			}else{
				String strFullPath = AllDataExporter.makeFileMergeStatment(queryExecuteResultDTO.getUserDB(), exeSQL, targetName, listWhere, commit, encoding, strDefaultNullValue, intMaxDownloadCnt);
				downloadFile(targetName, strFullPath, encoding);
			}
		}
	}
	
	/**
	 * 에디터 오픈
	 * 
	 * @param strContetn
	 */
	private void targetEditor(final String strContetn) {
		getShell().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				FindEditorAndWriteQueryUtil.run(queryExecuteResultDTO.getUserDB(), strContetn, PublicTadpoleDefine.OBJECT_TYPE.TABLES);
			}
		});
	}
	
	/**
	 * get request query
	 * 
	 * @return
	 */
//	public RequestQuery getRequestQuery() {
//		return requestQuery;
//	}
	
	/**
	 * preview data 
	 * @param fileName
	 * @param previewData
	 * @param encoding
	 * @throws Exception
	 */
	protected void previewDataLoad(final String fileName, final String previewData, final String encoding) throws Exception {
		getShell().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				textPreview.setText(previewData);		
			}
		});
	}
	
	/**
	 * download file
	 * 
	 * @param fileName
	 * @param strFileLocation
	 * @param encoding 
	 * 
	 * @throws Exception
	 */
	protected void downloadFile(final String fileName, final String strFileLocation, final String encoding) throws Exception {
		getShell().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				try {
//					String strZipFile = ZipUtils.pack(strFileLocation);
//					byte[] bytesZip = FileUtils.readFileToByteArray(new File(strZipFile));
//					if(logger.isDebugEnabled()) logger.debug("zipFile is " + strZipFile + ", file name is " + fileName +".zip");
					
					File file = new File(strFileLocation);
					String strExt = StringUtils.substringAfter(strFileLocation, ".");
					if(logger.isInfoEnabled()) {
						logger.info("#####[start]#####################[resource download]");
						logger.info("\tfile ext : " + strExt);
						logger.info("\tfile size : " + file.length());
						logger.info("#####[end]#####################[resource download]");
					}
					
					byte[] bytesZip = FileUtils.readFileToByteArray(file);
					_downloadExtFile(fileName + "." + strExt, bytesZip); //$NON-NLS-1$
					
					// 사용후 파일을 삭제한다.
					FileUtils.deleteDirectory(new File(file.getParent()));
//					FileUtils.forceDelete(new File(strZipFile));
				} catch(Exception e) {
					logger.error("download file", e);
				}
			}
		});
	}
	
//	/**
//	 * 쿼리 실행 결과를 저장한다.
//	 * 
//	 * @param reqResultDAO
//	 * @param rsDAO
//	 * @return
//	 */
//	public long saveExecutedSQLData(RequestResultDAO reqResultDAO, QueryExecuteResultDTO rsDAO) {
//		long longHistorySeq = -1;
//		
//		LicenseDAO licenseDAO = LicenseValidator.getLicense();
//		if(licenseDAO.isValidate()) {
//			try {
//				
//				String strExecuteResultData = "";
//				if(rsDAO != null) {
//					if(PublicTadpoleDefine.YES_NO.YES.name().equals(rsDAO.getUserDB().getIs_result_save())) {
//						strExecuteResultData = CSVExpoter.makeContent(true, rsDAO, ',', "UTF-8");
//					}
//				}
//				
//				longHistorySeq = TadpoleSystem_ExecutedSQL.saveExecuteSQUeryResource(getRdbResultComposite().getUserSeq(), 
//								getRdbResultComposite().getUserDB(), 
//								PublicTadpoleDefine.EXECUTE_SQL_TYPE.EDITOR, 
//								strExecuteResultData,
//								reqResultDAO);
//			
//				
//			} catch(Exception e) {
//				logger.error("save the user query", e); //$NON-NLS-1$
//			}
//		}
//	}
	
	/** registery service handler */
	protected void registerServiceHandler() {
		downloadServiceHandler = new DownloadServiceHandler();
		RWT.getServiceManager().registerServiceHandler(downloadServiceHandler.getId(), downloadServiceHandler);
	}
	
	/** download service handler call */
	protected void unregisterServiceHandler() {
		RWT.getServiceManager().unregisterServiceHandler(downloadServiceHandler.getId());
		downloadServiceHandler = null;
	}
	
	/**
	 * download external file
	 * 
	 * @param fileName
	 * @param newContents
	 */
	protected void _downloadExtFile(String fileName, byte[] newContents) {
		downloadServiceHandler.setName(fileName);
		downloadServiceHandler.setByteContent(newContents);
		
		DownloadUtils.provideDownload(getShell(), downloadServiceHandler.getId());
	}
}
