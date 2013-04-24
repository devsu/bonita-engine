/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.reporting;

import org.bonitasoft.engine.exception.ObjectAlreadyExistsException;
import org.bonitasoft.engine.reporting.Report;

/**
 * @author Matthieu Chaffotte
 */
public class ReportAlreadyExistsException extends ObjectAlreadyExistsException {

    private static final long serialVersionUID = -3352888386131568679L;

    public ReportAlreadyExistsException(final Throwable cause) {
        super(cause, Report.class);
    }

}
