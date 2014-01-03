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
package com.axelor.apps.organisation.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.organisation.db.Expense;
import com.axelor.apps.organisation.db.ExpenseLine;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;


public class ExpenseLineService {

	private static final Logger LOG = LoggerFactory.getLogger(ExpenseLineService.class);
	
	@Inject
	private CurrencyService currencyService;
	
	
	/**
	 * Calculer le montant HT d'une ligne de devis.
	 * 
	 * @param quantity
	 *          Quantité.
	 * @param price
	 *          Le prix.
	 * 
	 * @return 
	 * 			Le montant HT de la ligne.
	 */
	public static BigDecimal computeAmount(BigDecimal quantity, BigDecimal price) {

		BigDecimal amount = quantity.multiply(price).setScale(2, RoundingMode.HALF_EVEN);

		LOG.debug("Calcul du montant HT avec une quantité de {} pour {} : {}", new Object[] { quantity, price, amount });

		return amount;
	}
	
	
	public BigDecimal getUnitPrice(Expense expense, ExpenseLine expenseLine) throws AxelorException  {
		
		Product product = expenseLine.getProduct();
		
		BigDecimal unitPrice = currencyService.getAmountCurrencyConverted(
			product.getPurchaseCurrency(), expense.getCurrency(), product.getPurchasePrice(), expenseLine.getDate());  
		
		if(expenseLine.getTaxLine() != null)  {
			unitPrice = unitPrice.add(expenseLine.getTaxLine().getValue().multiply(unitPrice));
		}
		
		return unitPrice;
	}
	
	
	public BigDecimal getCompanyTotal(BigDecimal total, Expense expense) throws AxelorException  {
		
		return currencyService.getAmountCurrencyConverted(
				expense.getCurrency(), expense.getCompany().getCurrency(), total, expense.getDate());  
	}
			
		
}
