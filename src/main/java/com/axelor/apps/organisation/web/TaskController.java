/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the “License”); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an “AS IS”
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 */
package com.axelor.apps.organisation.web;

import java.util.HashMap;
import java.util.Map;

import com.axelor.apps.AxelorSettings;
import com.axelor.apps.organisation.db.ITask;
import com.axelor.apps.organisation.db.Task;
import com.axelor.apps.organisation.service.FinancialInformationHistoryLineService;
import com.axelor.apps.organisation.service.FinancialInformationHistoryService;
import com.axelor.apps.organisation.service.TaskService;
import com.axelor.apps.tool.net.URLService;
import com.axelor.auth.AuthUtils;
import com.axelor.exception.AxelorException;
import com.axelor.meta.db.MetaUser;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class TaskController {

	@Inject
	private Provider<TaskService> taskService;
	
	@Inject
	private Provider<FinancialInformationHistoryService> financialInformationHistoryService;
	
	@Inject
	private Provider<FinancialInformationHistoryLineService> financialInformationHistoryLineService;
	
	public void updateFinancialInformation(ActionRequest request, ActionResponse response) throws AxelorException {
		
		Task task = request.getContext().asType(Task.class);
		
		if(task.getId() != null)  {
			taskService.get().updateFinancialInformation(task);
			
			// Les montants sont figés dès le commencement de la tache
			if(task.getStatusSelect() < ITask.STATUS_STARTED && task.getRealEstimatedMethodSelect() != ITask.REAL_ESTIMATED_METHOD_NONE)  {
				response.setValue("initialEstimatedTurnover", task.getInitialEstimatedTurnover());
				response.setValue("initialEstimatedCost", task.getInitialEstimatedCost());
				response.setValue("initialEstimatedMargin", task.getInitialEstimatedMargin());
			}
			response.setValue("realEstimatedTurnover", task.getRealEstimatedTurnover());
			response.setValue("realEstimatedCost", task.getRealEstimatedCost());
			response.setValue("realEstimatedMargin", task.getRealEstimatedMargin());
			response.setValue("realInvoicedTurnover", task.getRealInvoicedTurnover());
			response.setValue("realInvoicedCost", task.getRealInvoicedCost());
			response.setValue("realInvoicedMargin", task.getRealInvoicedMargin());
		}
	}
	
	
	public void updateFinancialInformationInitialEstimated(ActionRequest request, ActionResponse response) throws AxelorException {
		
		Task task = request.getContext().asType(Task.class);
		
		if(task.getId() != null)  {
			
			financialInformationHistoryService.get().updateFinancialInformationInitialEstimatedHistory(task);
			
			taskService.get().updateInitialEstimatedAmount(task);
			
			response.setValue("initialEstimatedTurnover", task.getInitialEstimatedTurnover());
			response.setValue("initialEstimatedCost", task.getInitialEstimatedCost());
			response.setValue("initialEstimatedMargin", task.getInitialEstimatedMargin());
			response.setValue("financialInformationHistoryLineList", task.getFinancialInformationHistoryLineList());
		}
	}
	
	
	
	public void getSpentTime(ActionRequest request, ActionResponse response) throws AxelorException {
		
		Task task = request.getContext().asType(Task.class);
		
		if(task.getId() != null)  {
				response.setValue("spentTime", taskService.get().getSpentTime(task));
		}
	}
	
	
	public void getPlannedTime(ActionRequest request, ActionResponse response) throws AxelorException {
		
		Task task = request.getContext().asType(Task.class);
		
		if(task.getId() != null)  {
				response.setValue("plannedTime", taskService.get().getPlannedTime(task));
		}
	}


	/**
	 * Fonction appeler par le bouton imprimer
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	public void printTaskReport(ActionRequest request, ActionResponse response) {

		Task task = request.getContext().asType(Task.class);

		MetaUser metaUser = MetaUser.findByUser( AuthUtils.getUser());

		StringBuilder url = new StringBuilder();
		AxelorSettings axelorSettings = AxelorSettings.get();
		String language = metaUser != null? (metaUser.getLanguage() == null || metaUser.getLanguage().equals(""))? "en" : metaUser.getLanguage() : "en"; 

		url.append(axelorSettings.get("axelor.report.engine", "")+"/frameset?__report=report/Task.rptdesign&__format="+task.getExportTypeSelect()+"&TaskId="+task.getId()+"&Local="+language+"&__locale=fr_FR"+axelorSettings.get("axelor.report.engine.datasource"));


		String urlNotExist = URLService.notExist(url.toString());
		if (urlNotExist == null){


			Map<String,Object> mapView = new HashMap<String,Object>();
			mapView.put("title", "Name "+task.getName());
			mapView.put("resource", url);
			mapView.put("viewType", "html");
			response.setView(mapView);	
		}
		else {
			response.setFlash(urlNotExist);
		}
	}
	
	
}
