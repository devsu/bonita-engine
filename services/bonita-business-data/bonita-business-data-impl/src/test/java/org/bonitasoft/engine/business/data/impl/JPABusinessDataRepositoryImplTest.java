/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.business.data.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;

import org.bonitasoft.engine.bdm.Entity;
import org.bonitasoft.engine.business.data.BusinessDataModelRepository;
import org.bonitasoft.engine.business.data.SBusinessDataNotFoundException;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.SRetryableException;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JPABusinessDataRepositoryImplTest {

    private static final long PRIMARY_KEY_1 = 1L;

    private JPABusinessDataRepositoryImpl repository;

    @Mock
    private TransactionService transactionService;

    @Mock
    private BusinessDataModelRepository businessDataModelRepository;

    @Mock
    private Map<String, Object> configuration;

    @Mock
    private ClassLoaderService classLoaderService;

    @Mock
    EntityManager manager;

    @Mock
    private TechnicalLoggerService loggerService;

    @Before
    public void setUp() {
        repository = spy(
                new JPABusinessDataRepositoryImpl(transactionService, businessDataModelRepository, loggerService, configuration, classLoaderService, 1L));
        doReturn(manager).when(repository).getEntityManager();
        doReturn(true).when(businessDataModelRepository).isDBMDeployed();
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);
        doReturn(criteriaBuilder).when(manager).getCriteriaBuilder();
        CriteriaQuery criteriaQuery = mock(CriteriaQuery.class);
        doReturn(criteriaQuery).when(criteriaBuilder).createQuery(any(Class.class));
        doReturn(criteriaQuery).when(criteriaQuery).where(any(Expression.class));
        doReturn(criteriaQuery).when(criteriaQuery).select(any(Selection.class));
        Root root = mock(Root.class);
        doReturn(root).when(criteriaQuery).from(any(Class.class));
        doReturn(mock(Path.class)).when(root).get(anyString());
    }

    @Test
    public void should_start_add_listener_on_classloader() throws Exception {
        //given
        doReturn(true).when(businessDataModelRepository).isDBMDeployed();
        doReturn(mock(EntityManagerFactory.class)).when(repository).createEntityManagerFactory();
        //when
        repository.start();
        //then
        verify(classLoaderService).addListener(ScopeType.TENANT.name(), 1L, repository);
    }

    @Test
    public void should_stop_remove_listener_on_classloader() throws Exception {
        //given
        doReturn(true).when(businessDataModelRepository).isDBMDeployed();
        doReturn(mock(EntityManagerFactory.class)).when(repository).createEntityManagerFactory();
        //when
        repository.stop();
        //then
        verify(classLoaderService).removeListener(ScopeType.TENANT.name(), 1L, repository);
    }

    @Test
    public void should_onUpdate_recreate_the_entity_manager_factory() throws Exception {
        //given
        EntityManagerFactory entityManagerFactory = mock(EntityManagerFactory.class);
        doReturn(entityManagerFactory).when(repository).createEntityManagerFactory();
        doReturn(entityManagerFactory).when(repository).getEntityManagerFactory();
        repository.start();
        //when
        repository.onUpdate(null);
        //then
        verify(entityManagerFactory).close();
        verify(repository, times(2)).createEntityManagerFactory();
    }

    @Test
    public void findById_should_not_detach_entities() throws Exception {
        final Address address1 = new Address(PRIMARY_KEY_1);
        when(manager.find(Address.class, PRIMARY_KEY_1)).thenReturn(address1);

        final Address result = repository.findById(Address.class, PRIMARY_KEY_1);

        assertThat(result).isEqualTo(address1);
        verify(manager, never()).detach(any(Address.class));
    }

    @Test(expected = SBusinessDataNotFoundException.class)
    public void findById_should_throw_an_exception_when_not_found() throws Exception {
        when(manager.find(Address.class, PRIMARY_KEY_1)).thenReturn(null);

        repository.findById(Address.class, PRIMARY_KEY_1);
    }

    @Test(expected = SBusinessDataNotFoundException.class)
    public void findById_should_throw_an_exception_with_a_null_identifier() throws Exception {
        repository.findById(Address.class, null);
    }

    @Test(expected = SRetryableException.class)
    public void should_findById_throw_retryable_when_persistenceException() throws Exception {
        //given
        doThrow(PersistenceException.class).when(manager).find(Address.class,PRIMARY_KEY_1);
        //when
        repository.findById(Address.class,PRIMARY_KEY_1);
        //then exception
    }
    @Test(expected = SRetryableException.class)
    public void should_findByIds_throw_retryable_when_persistenceException() throws Exception {
        //given
        doThrow(PersistenceException.class).when(manager).createQuery(any(CriteriaQuery.class));
        //when
        repository.findByIds(Address.class, Collections.singletonList(PRIMARY_KEY_1));
        //then exception
    }

    @Test(expected = SRetryableException.class)
    public void should_findByIdentifiers_throw_retryable_when_persistenceException() throws Exception {
        //given
        doThrow(PersistenceException.class).when(manager).find(Address.class,PRIMARY_KEY_1);
        //when
        repository.findByIdentifiers(Address.class,Collections.singletonList(PRIMARY_KEY_1));
        //then exception
    }

    @Test(expected = SRetryableException.class)
    public void should_findByNamedQuery_throw_retryable_when_persistenceException() throws Exception {
        //given
        TypedQuery typedQuery = mock(TypedQuery.class);
        doThrow(PersistenceException.class).when(typedQuery).getSingleResult();
        doReturn(typedQuery).when(manager).createNamedQuery(anyString(),any(Class.class));
        //when
        repository.findByNamedQuery("queryName",Address.class,Collections.<String, Serializable>emptyMap());
        //then exception
    }
    @Test(expected = SRetryableException.class)
    public void should_findListByNamedQuery_throw_retryable_when_persistenceException() throws Exception {
        //given
        TypedQuery typedQuery = mock(TypedQuery.class);
        doThrow(PersistenceException.class).when(typedQuery).getResultList();
        doReturn(typedQuery).when(manager).createNamedQuery(anyString(),any(Class.class));
        //when
        repository.findListByNamedQuery("queryName",Address.class,Collections.<String, Serializable>emptyMap(),0,10);
        //then exception
    }

    @Test(expected = SRetryableException.class)
    public void should_find_throw_retryable_when_persistenceException() throws Exception {
        //given
        TypedQuery typedQuery = mock(TypedQuery.class);
        doThrow(PersistenceException.class).when(typedQuery).getSingleResult();
        doReturn(typedQuery).when(manager).createQuery(anyString(),any(Class.class));
        //when
        repository.find(Address.class,"the query as string",Collections.<String, Serializable>emptyMap());
        //then exception
    }
    @Test(expected = SRetryableException.class)
    public void should_findList_throw_retryable_when_persistenceException() throws Exception {
        //given
        TypedQuery typedQuery = mock(TypedQuery.class);
        doThrow(PersistenceException.class).when(typedQuery).getResultList();
        doReturn(typedQuery).when(manager).createQuery(anyString(),any(Class.class));
        //when
        repository.findList(Address.class,"the query as string",Collections.<String, Serializable>emptyMap(),0,10);
        //then exception
    }

    @Test(expected = SRetryableException.class)
    public void should_persist_throw_retryable_exception_in_case_of_persistenceException() throws Exception {
        //given
        doThrow(PersistenceException.class).when(manager).persist(any(Address.class));
        //when
        repository.persist(new Address(12));
        //then exception
    }

    @Test(expected = SRetryableException.class)
    public void should_merge_throw_retryable_exception_in_case_of_persistenceException() throws Exception {
        //given
        doThrow(PersistenceException.class).when(manager).merge(any(Address.class));
        //when
        repository.merge(new Address(12));
        //then exception
    }

    @Test(expected = SRetryableException.class)
    public void should_remove_throw_retryable_exception_in_case_of_persistenceException() throws Exception {
        //given
        doThrow(PersistenceException.class).when(manager).remove(any(Address.class));
        //when
        repository.remove(new Address(12));
        //then exception
    }

    class Address implements Entity {

        private static final long serialVersionUID = 2603989953326533907L;

        private final long persistenceId;

        public Address(final long persistenceId) {
            this.persistenceId = persistenceId;
        }

        @Override
        public Long getPersistenceId() {
            return persistenceId;
        }

        @Override
        public Long getPersistenceVersion() {
            return 0L;
        }

    }

}
