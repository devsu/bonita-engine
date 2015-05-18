/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine;

import com.bonitasoft.engine.connector.ConnectorExecutionTimeOutTest;
import com.bonitasoft.engine.connector.ConnectorImplementationLocalSPTest;
import com.bonitasoft.engine.form.LiveUpdateIT;
import com.bonitasoft.engine.tenant.TenantLocalIT;
import com.bonitasoft.engine.tenant.TenantMaintenanceLocalIT;
import com.bonitasoft.engine.theme.ThemeTest;
import org.bonitasoft.engine.BPMRemoteTestsLocal;
import org.bonitasoft.engine.test.BPMLocalSuiteTests;
import org.bonitasoft.engine.test.runner.BonitaSuiteRunner;
import org.bonitasoft.engine.test.runner.BonitaSuiteRunner.Initializer;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(BonitaSuiteRunner.class)
@SuiteClasses({BPMSPTests.class,
        BPMLocalSuiteTests.class,
        BPMRemoteTestsLocal.class,
        BPMRemoteSPTests.class,
        LocalLogTest.class,
        APIMethodSPIT.class,
        ConnectorExecutionTimeOutTest.class,
        ConnectorImplementationLocalSPTest.class,
        ThemeTest.class,
        TenantMaintenanceLocalIT.class,
        LiveUpdateIT.class,
        PageAPILocalIT.class,
        TenantLocalIT.class,
        BDRepositoryLocalIT.class,
        AllBPMSPTests.class
})
@Initializer(LocalServerTestsInitializerSP.class)
public class LocalIntegrationTestsSP {

}
