/*
 * Copyright 2002-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dts.client.annotation;

import org.dts.client.interceptor.DtsTransactionAttribute;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.TransactionAnnotationParser;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.NoRollbackRuleAttribute;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttribute;

import com.quancheng.dts.common.DtsTranModel;

import java.io.Serializable;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;

/**
 * Strategy implementation for parsing Spring's {@link Transactional} annotation.
 *
 * @author Juergen Hoeller
 * @since 2.5
 */
@SuppressWarnings("serial")
public class SpringDtsTransactionAnnotationParser implements TransactionAnnotationParser, Serializable {

	@Override
	public DtsTransactionAttribute parseTransactionAnnotation(AnnotatedElement ae) {
		AnnotationAttributes attributes = AnnotatedElementUtils.getMergedAnnotationAttributes(ae, DtsTransactional.class);
		if (attributes != null) {
			return parseTransactionAnnotation(attributes);
		}
		else {
			return null;
		}
	}

	public DtsTransactionAttribute parseTransactionAnnotation(DtsTransactional ann) {
		return parseTransactionAnnotation(AnnotationUtils.getAnnotationAttributes(ann, false, false));
	}

	protected DtsTransactionAttribute parseTransactionAnnotation(AnnotationAttributes attributes) {
		DtsTransactionAttribute rbta = new DtsTransactionAttribute();

		rbta.setQualifier(attributes.getString("value"));
		rbta.setTimeout(attributes.getNumber("timeout").intValue());
		rbta.setEffectiveTime(attributes.getNumber("effectiveTime").intValue());
		DtsTranModel tranModel = attributes.getEnum("tranModel");
		rbta.setTranModel(tranModel.name());
//		Propagation propagation = attributes.getEnum("propagation");
//		rbta.setPropagationBehavior(propagation.value());
//		Isolation isolation = attributes.getEnum("isolation");
//		rbta.setIsolationLevel(isolation.value());
//		rbta.setTimeout(attributes.getNumber("timeout").intValue());
//		rbta.setReadOnly(attributes.getBoolean("readOnly"));
//		ArrayList<RollbackRuleAttribute> rollBackRules = new ArrayList<RollbackRuleAttribute>();
//		Class<?>[] rbf = attributes.getClassArray("rollbackFor");
//		for (Class<?> rbRule : rbf) {
//			RollbackRuleAttribute rule = new RollbackRuleAttribute(rbRule);
//			rollBackRules.add(rule);
//		}
//		String[] rbfc = attributes.getStringArray("rollbackForClassName");
//		for (String rbRule : rbfc) {
//			RollbackRuleAttribute rule = new RollbackRuleAttribute(rbRule);
//			rollBackRules.add(rule);
//		}
//		Class<?>[] nrbf = attributes.getClassArray("noRollbackFor");
//		for (Class<?> rbRule : nrbf) {
//			NoRollbackRuleAttribute rule = new NoRollbackRuleAttribute(rbRule);
//			rollBackRules.add(rule);
//		}
//		String[] nrbfc = attributes.getStringArray("noRollbackForClassName");
//		for (String rbRule : nrbfc) {
//			NoRollbackRuleAttribute rule = new NoRollbackRuleAttribute(rbRule);
//			rollBackRules.add(rule);
//		}
//		rbta.getRollbackRules().addAll(rollBackRules);
		return rbta;
	}

	@Override
	public boolean equals(Object other) {
		return (this == other || other instanceof SpringDtsTransactionAnnotationParser);
	}

	@Override
	public int hashCode() {
		return SpringDtsTransactionAnnotationParser.class.hashCode();
	}

}
