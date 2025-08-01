/*
 * Copyright (c) 2022 codbex or an codbex affiliate company and contributors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-FileCopyrightText: 2022 codbex or an codbex affiliate company and contributors
 * SPDX-License-Identifier: EPL-2.0
 */
const viewData = {
	id: 'airflow',
	label: 'Airflow',
	path: '/services/web/perspective-airflow/index.html',
	lazyLoad: true,
	autoFocusTab: false,
	icon: '/services/web/perspective-airflow/images/airflow.svg',
	order: 110,
};
if (typeof exports !== 'undefined') {
	exports.getPerspective = () => viewData;
}
