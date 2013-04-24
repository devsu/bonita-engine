/*******************************************************************************
 * Copyright (C) 2009, 2012 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine;

import org.bonitasoft.engine.test.APIMethodTest;
import org.junit.Test;

import com.bonitasoft.engine.api.impl.LogAPIExt;
import com.bonitasoft.engine.api.impl.MigrationAPIImpl;
import com.bonitasoft.engine.api.impl.MonitoringAPIImpl;
import com.bonitasoft.engine.api.impl.NodeAPIImpl;
import com.bonitasoft.engine.api.impl.PlatformMonitoringAPIImpl;
import com.bonitasoft.engine.api.impl.ProcessAPIExt;
import com.bonitasoft.engine.api.impl.ReportAPIExt;

public class APIMethodSPTest extends APIMethodTest {

    @Override
    @Test
    public void checkAllMethodsOfProcessAPIThrowInvalidSessionException() {
        checkThrowsInvalidSessionException(ProcessAPIExt.class);
    }

    @Override
    @Test
    public void checkAllMethodsOfProcessAPIContainsSerializableParameters() {
        checkAllParametersAreSerializable(ProcessAPIExt.class);
    }

    @Test
    public void checkAllMethodsOfLogAPIThrowInvalidSessionException() {
        checkThrowsInvalidSessionException(LogAPIExt.class);
    }

    @Test
    public void checkAllMethodsOfLogAPIContainsSerializableParameters() {
        checkAllParametersAreSerializable(LogAPIExt.class);
    }

    @Test
    public void checkAllMethodsOfMonitoringAPIThrowInvalidSessionException() {
        checkThrowsInvalidSessionException(MonitoringAPIImpl.class);
    }

    @Test
    public void checkAllMethodsOfMonitoringAPIContainsSerializableParameters() {
        checkAllParametersAreSerializable(MonitoringAPIImpl.class);
    }

    @Test
    public void checkAllMethodsOfMigrationAPIThrowInvalidSessionException() {
        checkThrowsInvalidSessionException(MigrationAPIImpl.class);
    }

    @Test
    public void checkAllMethodsOfMigrationAPIContainsSerializableParameters() {
        checkAllParametersAreSerializable(MigrationAPIImpl.class);
    }

    // @Override
    // @Test
    // public void checkAllMethodsOfPlatformAPIThrowInvalidSessionException() {
    // checkThrowsInvalidSessionException(PlatformAPIExt.class);
    // }
    //
    // @Override
    // @Test
    // public void checkAllMethodsOfPlatformAPIContainsSerializableParameters() {
    // checkAllParametersAreSerializable(PlatformAPIExt.class);
    // }

    @Test
    public void checkAllMethodsOfPlatformMonitoringAPIThrowInvalidSessionException() {
        checkThrowsInvalidSessionException(PlatformMonitoringAPIImpl.class);
    }

    @Test
    public void checkAllMethodsOfPlatformMonitoringAPIContainsSerializableParameters() {
        checkAllParametersAreSerializable(PlatformMonitoringAPIImpl.class);
    }

    @Override
    @Test
    public void checkAllMethodsOfReportAPIThrowInvalidSessionException() {
        checkThrowsInvalidSessionException(ReportAPIExt.class);
    }

    @Override
    @Test
    public void checkAllMethodsOfReportAPIContainsSerializableParameters() {
        checkAllParametersAreSerializable(ReportAPIExt.class);
    }

    @Test
    public void checkAllMethodsOfNodeAPIContainsSerializableParameters() {
        checkAllParametersAreSerializable(NodeAPIImpl.class);
    }

}
