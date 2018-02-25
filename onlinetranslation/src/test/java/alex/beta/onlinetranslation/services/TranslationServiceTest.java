/**
 * <p>
 * File Name: TranslationServiceTest.java
 * </p>
 * <p>
 * Project:   beta
 * </p>
 * <p>
 * Copyright: Copyright (c) 2018, All Rights Reserved
 * E-mail: song_liping@hotmail.com
 * </p>
 * <p>
 * Created on 2018/2/24 下午10:24
 * </p>
 */
package alex.beta.onlinetranslation.services;

import alex.beta.onlinetranslation.Application;
import alex.beta.onlinetranslation.models.TranslationModel;
import alex.beta.onlinetranslation.persistence.HousekeepingRepository;
import alex.beta.onlinetranslation.persistence.TranslationEntity;
import alex.beta.onlinetranslation.persistence.TranslationRepository;
import alex.beta.onlinetranslation.persistence.TranslationStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;


/**
 * @author alexsong
 * @version ${project.version}
 */

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@EnableAutoConfiguration
@Transactional
public class TranslationServiceTest {
    @Autowired
    private TranslationService translationService;

    @Autowired
    private TranslationRepository translationRepository;

    @Autowired
    private HousekeepingRepository housekeepingRepository;

    @Autowired
    private EntityManager entityManager;

    @Before
    public void setUp() {
        //Make sure the database is clean before each test case
        Number result = (Number) entityManager.createNativeQuery("select count(uuid) from translation").getSingleResult();
        assertEquals(0, result.intValue());
    }

    @After
    public void tearDown() {
    }

    @Test
    @Rollback
    public void testSubmit() {
        TranslationModel tm1 = translationService.submit("auto", "zh", "hello");
        TranslationModel tm2 = translationService.submit("auto", "zh", "hello");
        TranslationModel tm3 = translationService.submit("auto", "zh", "hello");

        assertNotNull(tm1.getUuid());
        assertNotNull(tm2.getUuid());
        assertNotNull(tm3.getUuid());


        List result = entityManager.createNativeQuery("select uuid from translation order by created_on ASC").getResultList();
        assertEquals(3, result.size());

        assertEquals(tm1.getUuid(), result.get(0));
        assertEquals(tm2.getUuid(), result.get(1));
        assertEquals(tm3.getUuid(), result.get(2));
    }

    @Test
    @Rollback
    public void testFindRequestsToTranslate() {
        TranslationModel tm1 = translationService.submit("auto", "zh", "hello1");
        TranslationModel tm2 = translationService.submit("auto", "zh", "hello2");
        TranslationModel tm3 = translationService.submit("auto", "zh", "hello3");
        TranslationModel tm4 = translationService.submit("auto", "zh", "hello4");
        TranslationModel tm5 = translationService.submit("auto", "zh", "hello5");

        TranslationEntity te2 = new TranslationEntity(tm2.getUuid());
        te2.setLastUpdatedOn(new Date(System.currentTimeMillis() + 30 * 1000L));
        te2 = translationService.updateTranslationRequest(te2);
        assertEquals("hello2", te2.getText());
        assertTrue(te2.getLastUpdatedOn().getTime() > System.currentTimeMillis());

        Number result = (Number) entityManager.createNativeQuery("select count(uuid) from translation").getSingleResult();
        assertEquals(5, result.intValue());

        List<TranslationEntity> tes = translationService.findRequestsToTranslate();

        assertEquals(3, tes.size());

        assertEquals(tm1.getUuid(), tes.get(0).getUuid());
        assertEquals(tm3.getUuid(), tes.get(1).getUuid());
        assertEquals(tm4.getUuid(), tes.get(2).getUuid());
    }

    @Test
    @Rollback
    public void testPerformHousekeeping() throws Exception {
        TranslationModel tm1 = translationService.submit("auto", "zh", "hello1");
        TranslationModel tm2 = translationService.submit("auto", "zh", "hello2");
        TranslationModel tm3 = translationService.submit("auto", "zh", "hello3");
        TranslationModel tm4 = translationService.submit("auto", "zh", "hello4");
        TranslationModel tm5 = translationService.submit("auto", "zh", "hello5");

        TranslationEntity te2 = new TranslationEntity(tm2.getUuid());
        te2.setLastUpdatedOn(new Date(System.currentTimeMillis() - 25 * 60 * 60 * 1000L));
        te2 = translationService.updateTranslationRequest(te2);
        assertTrue(System.currentTimeMillis() > te2.getLastUpdatedOn().getTime());

        TranslationEntity te3 = new TranslationEntity(tm3.getUuid());
        te3.setLastUpdatedOn(new Date(System.currentTimeMillis() - 15 * 60 * 60 * 1000L));
        te3 = translationService.updateTranslationRequest(te3);
        assertTrue(System.currentTimeMillis() > te3.getLastUpdatedOn().getTime());
        assertTrue(System.currentTimeMillis() - 12 * 60 * 60 * 1000L > te3.getLastUpdatedOn().getTime());

        TranslationEntity te4 = new TranslationEntity(tm4.getUuid());
        Date oldDate = tm4.getLastUpdatedOn();
        te4.setLastUpdatedOn(null);
        te4.setStatus(TranslationStatus.ERROR);
        te4 = translationService.updateTranslationRequest(te4);
        assertTrue(te4.getLastUpdatedOn().after(oldDate));

        TranslationEntity te5 = new TranslationEntity(tm5.getUuid());
        te5.setLastUpdatedOn(new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000L - 1L));
        te5.setStatus(TranslationStatus.READY);
        te5 = translationService.updateTranslationRequest(te5);

        translationService.performHousekeeping();
        Number result = (Number) entityManager.createNativeQuery("select count(uuid) from translation").getSingleResult();
        assertEquals(3, result.intValue());
    }
}
