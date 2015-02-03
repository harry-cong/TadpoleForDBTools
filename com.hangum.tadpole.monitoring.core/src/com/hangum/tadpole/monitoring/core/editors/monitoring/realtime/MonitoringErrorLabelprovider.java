package com.hangum.tadpole.monitoring.core.editors.monitoring.realtime;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.hangum.tadpole.sql.dao.system.monitoring.MonitoringResultDAO;

/**
 * monitoring label provider
 * 
 * @author hangum
 *
 */
public class MonitoringErrorLabelprovider extends LabelProvider implements ITableLabelProvider {

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		MonitoringResultDAO dao = (MonitoringResultDAO)element;
			
		switch(columnIndex) {
			case 0: return dao.getResult();
			case 1: return dao.getMonitoringIndexDAO().getTitle();
			case 2: return dao.getIndex_value();
			case 3: return dao.getMonitoringIndexDAO().getCondition_type() + " " + dao.getMonitoringIndexDAO().getCondition_value();
			case 4: return dao.getQuery_result() + dao.getQuery_result2();
			case 5: return dao.getCreate_time().toString();
		}
	
		return null;
	}


}
