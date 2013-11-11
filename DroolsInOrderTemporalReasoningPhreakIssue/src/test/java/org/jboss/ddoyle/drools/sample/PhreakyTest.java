package org.jboss.ddoyle.drools.sample;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

public class PhreakyTest {

	private static final String OUT_CHANNEL_NAME = "out";

	private static KieContainer kieContainer;
	private KieSession kieSession;
	private MockChannel channel;

	@BeforeClass
	public static void initKieContainer() {
		KieServices kieServices = KieServices.Factory.get();
		kieContainer = kieServices.getKieClasspathContainer();
	}

	/*
	 * Load and initialize a new KieSession on every test.
	 */
	@Before
	public void initKieSession() {
		kieSession = kieContainer.newKieSession();
		channel = new MockChannel();
		kieSession.getChannels().put(OUT_CHANNEL_NAME, channel);
		AgendaEventListener myAgendaEventListener = new MyAgendaEventListener();
		kieSession.addEventListener(myAgendaEventListener);
	}

	@Test
	public void testRG_FLT_03_One() {
		testFrequency("K08", 4);
	}

	@Test
	public void testRG_FLT_03_Two() {
		//This test is intended to test that the first 4 entries get filtered, even if we add a batch larger than 4.
		testFrequencyTwo("K08", 6);
	}

	private void testFrequency(final String code, final int fq) {

		for (int i = 0; i < fq; i++) {
			insertEvent(code, SimpleEvent.Status.ENRICHED, i);
		}
		kieSession.fireAllRules();
		assertEquals("All event must be still in working memory", fq, kieSession.getFactHandles().size());
		assertEquals("No event must be in channel out", 0, channel.getSentObject().size());

		insertEvent(code, SimpleEvent.Status.ENRICHED, 1);
		kieSession.fireAllRules();
		assertEquals("Last event must be in working memory", 1, kieSession.getFactHandles().size());
		assertEquals("One event must be in channel out", 1, channel.getSentObject().size());

		insertEvent(code, SimpleEvent.Status.ENRICHED, 1);
		kieSession.fireAllRules();
		assertEquals("Two last event must be in working memory", 2, kieSession.getFactHandles().size());
		assertEquals("Only one event must be in channel out", 1, channel.getSentObject().size());
	}

	private void testFrequencyTwo(final String code, final int fq) {
		for (int i = 0; i < fq; i++) {
			insertEvent(code, SimpleEvent.Status.ENRICHED, i);
		}
		kieSession.fireAllRules();
		
		assertEquals("Two last event must be in working memory", 2, kieSession.getFactHandles().size());
		assertEquals("Only one event must be in channel out", 1, channel.getSentObject().size());
	}

	private SimpleEvent createEvent(final String code, final SimpleEvent.Status status, final int index) {
		SimpleEvent event = new SimpleEvent();
		event.setCode(code);
		event.setStatus(status);
		event.setIndex(index);
		return event;
	}

	protected void insertEvent(final String code, final SimpleEvent.Status status, final int index) {
		kieSession.insert(createEvent(code, status, index));
	}

}
