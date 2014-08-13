/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.application.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectAlreadyExistsException;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.SUpdateEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilderFactory;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SBonitaSearchException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;
import org.bonitasoft.engine.queriablelogger.model.builder.ActionType;
import org.bonitasoft.engine.queriablelogger.model.builder.HasCRUDEAction;
import org.bonitasoft.engine.queriablelogger.model.builder.SLogBuilder;
import org.bonitasoft.engine.queriablelogger.model.builder.SPersistenceLogBuilder;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.bonitasoft.engine.services.QueriableLoggerService;

import com.bonitasoft.engine.business.application.ApplicationService;
import com.bonitasoft.engine.business.application.SApplication;
import com.bonitasoft.engine.business.application.SApplicationLogBuilder;
import com.bonitasoft.engine.business.application.SApplicationPage;
import com.bonitasoft.engine.business.application.SApplicationPageLogBuilder;
import com.bonitasoft.engine.business.application.SInvalidNameException;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class ApplicationServiceImpl implements ApplicationService {

    private final Recorder recorder;

    private final ReadPersistenceService persistenceService;

    private final QueriableLoggerService queriableLoggerService;

    public ApplicationServiceImpl(final Recorder recorder, final ReadPersistenceService persistenceService, final QueriableLoggerService queriableLoggerService) {
        this.recorder = recorder;
        this.persistenceService = persistenceService;
        this.queriableLoggerService = queriableLoggerService;
    }

    @Override
    public SApplication createApplication(final SApplication application) throws SObjectCreationException, SObjectAlreadyExistsException,
    SInvalidNameException {
        final String methodName = "createApplication";
        final SApplicationLogBuilder logBuilder = getApplicationLog(ActionType.CREATED, "Creating application named " + application.getName());
        try {
            validate(application);
            final SInsertEvent insertEvent = (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class).createInsertEvent(ApplicationService.APPLICATION)
                    .setObject(application).done();
            recorder.recordInsert(new InsertRecord(application), insertEvent);
            log(application.getId(), SQueriableLog.STATUS_OK, logBuilder, methodName);
        } catch (final SInvalidNameException e) {
            log(application.getId(), SQueriableLog.STATUS_FAIL, logBuilder, methodName);
            throw e;
        } catch (final SObjectAlreadyExistsException e) {
            log(application.getId(), SQueriableLog.STATUS_FAIL, logBuilder, methodName);
            throw e;
        } catch (final SBonitaException e) {
            handleCreationException(application, logBuilder, e, methodName);
        }
        return application;
    }

    private void validate(final SApplication application) throws SInvalidNameException, SBonitaReadException, SObjectAlreadyExistsException {
        if (!URLValidator.isValid(application.getName())) {
            throw new SInvalidNameException(
                    "Invalid application name: the name can not be null or empty and should contains only alpha numeric characters and the following special characters '-', '.', '_' or '~'");
        }
        if (hasApplicationWithName(application.getName())) {
            throw new SObjectAlreadyExistsException("An application already exists with name '" + application.getName() + "'.");
        }
    }

    private void handleCreationException(final PersistentObject persitentObject, final SPersistenceLogBuilder logBuilder, final Exception e, final String methodName)
            throws SObjectCreationException {
        log(persitentObject.getId(), SQueriableLog.STATUS_FAIL, logBuilder, methodName);
        throw new SObjectCreationException(e);
    }

    private void throwModificationException(final long persitentObjectId, final SApplicationLogBuilder logBuilder, final String methodName, final Exception e)
            throws SObjectModificationException {
        log(persitentObjectId, SQueriableLog.STATUS_FAIL, logBuilder, methodName);
        throw new SObjectModificationException(e);
    }

    public boolean hasApplicationWithName(final String name) throws SBonitaReadException {
        final SApplication application = persistenceService.selectOne(new SelectOneDescriptor<SApplication>("getApplicationByName", Collections
                .<String, Object> singletonMap("name",
                        name), SApplication.class));
        return application != null;
    }

    private <T extends SLogBuilder> void initializeLogBuilder(final T logBuilder, final String message) {
        logBuilder.actionStatus(SQueriableLog.STATUS_FAIL).severity(SQueriableLogSeverity.INTERNAL).rawMessage(message);
    }

    private <T extends HasCRUDEAction> void updateLog(final ActionType actionType, final T logBuilder) {
        logBuilder.setActionType(actionType);
    }

    private SApplicationLogBuilder getApplicationLog(final ActionType actionType, final String message) {
        final SApplicationLogBuilder logBuilder = new SApplicationLogBuilderImpl();
        initializeLogBuilder(logBuilder, message);
        updateLog(actionType, logBuilder);
        return logBuilder;
    }

    private SApplicationPageLogBuilder getApplicationPageLog(final ActionType actionType, final String message) {
        final SApplicationPageLogBuilderImpl logBuilder = new SApplicationPageLogBuilderImpl();
        initializeLogBuilder(logBuilder, message);
        updateLog(actionType, logBuilder);
        return logBuilder;
    }

    void log(final long objectId, final int sQueriableLogStatus, final SPersistenceLogBuilder logBuilder, final String methodName) {
        logBuilder.actionScope(String.valueOf(objectId));
        logBuilder.actionStatus(sQueriableLogStatus);
        logBuilder.objectId(objectId);
        final SQueriableLog log = logBuilder.done();
        if (queriableLoggerService.isLoggable(log.getActionType(), log.getSeverity())) {
            queriableLoggerService.log(this.getClass().getName(), methodName, log);
        }
    }

    @Override
    public SApplication getApplication(final long applicationId) throws SBonitaReadException, SObjectNotFoundException {
        final SApplication application = persistenceService
                .selectById(new SelectByIdDescriptor<SApplication>("getApplicationById", SApplication.class, applicationId));
        if (application == null) {
            throw new SObjectNotFoundException("No application found with id '" + applicationId + "'.");
        }
        return application;
    }

    @Override
    public void deleteApplication(final long applicationId) throws SObjectModificationException, SObjectNotFoundException {
        final String methodName = "deleteApplication";
        final SApplicationLogBuilder logBuilder = getApplicationLog(ActionType.CREATED, "Deleting application with id " + applicationId);
        try {
            final SApplication application = getApplication(applicationId);
            final SDeleteEvent event = (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent(ApplicationService.APPLICATION)
                    .setObject(application).done();
            recorder.recordDelete(new DeleteRecord(application), event);
            log(application.getId(), SQueriableLog.STATUS_OK, logBuilder, methodName);
        } catch (final SObjectNotFoundException e) {
            log(applicationId, SQueriableLog.STATUS_FAIL, logBuilder, methodName);
            throw e;
        } catch (final SBonitaException e) {
            throwModificationException(applicationId, logBuilder, methodName, e);
        }

    }

    @Override
    public SApplication updateApplication(final long applicationId, final EntityUpdateDescriptor updateDescriptor) throws SObjectModificationException {
        final String methodName = "updateApplication";
        final SApplicationLogBuilder logBuilder = getApplicationLog(ActionType.CREATED, "Updating application with id " + applicationId);
        try {
            final SApplication application = getApplication(applicationId);
            final UpdateRecord updateRecord = UpdateRecord.buildSetFields(application,
                    updateDescriptor);
            final SUpdateEvent updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(ApplicationService.APPLICATION)
                    .setObject(application).done();
            recorder.recordUpdate(updateRecord, updateEvent);
            log(applicationId, SQueriableLog.STATUS_OK, logBuilder, methodName);
            return application;
        } catch (final SBonitaException e) {
            log(applicationId, SQueriableLog.STATUS_FAIL, logBuilder, methodName);
            throw new SObjectModificationException(e);
        }
    }

    @Override
    public long getNumberOfApplications(final QueryOptions options) throws SBonitaReadException {
        return persistenceService.getNumberOfEntities(SApplication.class, options, null);
    }

    @Override
    public List<SApplication> searchApplications(final QueryOptions options) throws SBonitaSearchException {
        try {
            return persistenceService.searchEntity(SApplication.class, options, null);
        } catch (final SBonitaReadException e) {
            throw new SBonitaSearchException(e);
        }
    }

    @Override
    public SApplicationPage createApplicationPage(final SApplicationPage applicationPage) throws SObjectCreationException, SObjectAlreadyExistsException,
    SInvalidNameException {
        final String methodName = "createApplicationPage";
        final SApplicationPageLogBuilder logBuilder = getApplicationPageLog(ActionType.CREATED, "Creating application page named " + applicationPage.getName());
        final SInsertEvent insertEvent = (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class).createInsertEvent(ApplicationService.APPLICATION_PAGE)
                .setObject(applicationPage).done();
        try {
            throwExceptionIfNameIsInvalid(applicationPage);
            throwAlreadyExistsExceptionIfAlreadyExists(applicationPage);
            recorder.recordInsert(new InsertRecord(applicationPage), insertEvent);
            log(applicationPage.getId(), SQueriableLog.STATUS_OK, logBuilder, methodName);
        } catch (final SInvalidNameException e) {
            log(applicationPage.getId(), SQueriableLog.STATUS_FAIL, logBuilder, methodName);
            throw e;
        } catch (final SObjectAlreadyExistsException e) {
            log(applicationPage.getId(), SQueriableLog.STATUS_FAIL, logBuilder, methodName);
            throw e;
        } catch (final SBonitaException e) {
            handleCreationException(applicationPage, logBuilder, e, methodName);
        }
        return applicationPage;
    }

    private void throwExceptionIfNameIsInvalid(final SApplicationPage applicationPage) throws SInvalidNameException {
        if (!URLValidator.isValid(applicationPage.getName())) {
            throw new SInvalidNameException(
                    "Invalid application page name: the name can not be null or empty and should contains only alpha numeric characters and the following special characters '-', '.', '_' or '~'");
        }
    }

    private void throwAlreadyExistsExceptionIfAlreadyExists(final SApplicationPage applicationPage) throws SBonitaReadException, SObjectAlreadyExistsException {
        if (hasApplicationPage(applicationPage.getApplicationId(), applicationPage.getName())) {
            final StringBuilder stb = new StringBuilder();
            stb.append("An application page named '");
            stb.append(applicationPage.getName());
            stb.append("' already exists for the application with id '");
            stb.append(applicationPage.getApplicationId());
            stb.append("'");
            throw new SObjectAlreadyExistsException(stb.toString());
        }
    }

    private boolean hasApplicationPage(final long applicationId, final String name) throws SBonitaReadException {
        final SApplicationPage applicationPage = getApplicationPage(applicationId, name);
        return applicationPage != null;
    }

    public SApplicationPage getApplicationPage(final long applicationId, final String applicationPageName) throws SBonitaReadException {
        final Map<String, Object> inputParameters = new HashMap<String, Object>(2);
        inputParameters.put("applicationId", applicationId);
        inputParameters.put("applicationPageName", applicationPageName);
        final SApplicationPage applicationPage = persistenceService
                .selectOne(new SelectOneDescriptor<SApplicationPage>("getApplicationPageByNameAndApplicationId", inputParameters, SApplicationPage.class));
        return applicationPage;
    }

    @Override
    public SApplicationPage getApplicationPage(final String applicationName, final String applicationPageName) throws SBonitaReadException,
    SObjectNotFoundException {
        final Map<String, Object> inputParameters = new HashMap<String, Object>(2);
        inputParameters.put("applicationName", applicationName);
        inputParameters.put("applicationPageName", applicationPageName);
        final SApplicationPage applicationPage = persistenceService
                .selectOne(new SelectOneDescriptor<SApplicationPage>("getApplicationPageByNameAndApplicationName", inputParameters, SApplicationPage.class));
        if (applicationPage == null) {
            final StringBuilder stb = new StringBuilder();
            stb.append("No application page found with name '");
            stb.append(applicationPageName);
            stb.append("' and application name '");
            stb.append(applicationName);
            stb.append("'.");
            throw new SObjectNotFoundException(stb.toString());
        }
        return applicationPage;
    }

    @Override
    public SApplicationPage getApplicationPage(final long applicationPageId) throws SBonitaReadException, SObjectNotFoundException {
        final SApplicationPage applicationPage = persistenceService
                .selectById(new SelectByIdDescriptor<SApplicationPage>("getApplicationPageById", SApplicationPage.class, applicationPageId));
        if (applicationPage == null) {
            throw new SObjectNotFoundException("No application page found with id '" + applicationPageId + "'.");
        }
        return applicationPage;
    }

    @Override
    public void deleteApplicationPage(final long applicationpPageId) throws SObjectModificationException, SObjectNotFoundException {
        final String methodName = "deleteApplicationPage";
        final SApplicationLogBuilder logBuilder = getApplicationLog(ActionType.CREATED, "Deleting application page with id " + applicationpPageId);
        try {
            final SApplicationPage applicationPage = getApplicationPage(applicationpPageId);
            final SDeleteEvent event = (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent(ApplicationService.APPLICATION_PAGE)
                    .setObject(applicationPage).done();
            recorder.recordDelete(new DeleteRecord(applicationPage), event);
            log(applicationPage.getId(), SQueriableLog.STATUS_OK, logBuilder, methodName);
        } catch (final SObjectNotFoundException e) {
            log(applicationpPageId, SQueriableLog.STATUS_FAIL, logBuilder, methodName);
            throw e;
        } catch (final SBonitaException e) {
            throwModificationException(applicationpPageId, logBuilder, methodName, e);
        }

    }

    @Override
    public SApplicationPage getApplicationHomePage(final long applicationId) throws SBonitaReadException, SObjectNotFoundException {
        final Map<String, Object> inputParameters = new HashMap<String, Object>(2);
        inputParameters.put("applicationId", applicationId);
        final SApplicationPage applicationPage = persistenceService
                .selectOne(new SelectOneDescriptor<SApplicationPage>("getApplicationHomePage", inputParameters, SApplicationPage.class));
        if (applicationPage == null) {
            final StringBuilder stb = new StringBuilder();
            stb.append("No home page found for application with id '");
            stb.append(applicationId);
            stb.append("'.");
            throw new SObjectNotFoundException(stb.toString());
        }
        return applicationPage;
    }

    @Override
    public long getNumberOfApplicationPages(final QueryOptions options) throws SBonitaReadException {
        return persistenceService.getNumberOfEntities(SApplicationPage.class, options, null);
    }

    @Override
    public List<SApplicationPage> searchApplicationPages(final QueryOptions options) throws SBonitaSearchException {
        try {
            return persistenceService.searchEntity(SApplicationPage.class, options, null);
        } catch (final SBonitaReadException e) {
            throw new SBonitaSearchException(e);
        }
    }


}
