package org.xbib.elasticsearch.support.client.ingest;

import org.elasticsearch.action.search.SearchAction;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.unit.TimeValue;
import org.junit.Test;
import org.xbib.elasticsearch.support.helper.AbstractNodeRandomTestHelper;

import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class IngestTransportDuplicateIDTest extends AbstractNodeRandomTestHelper {

    private final static ESLogger logger = ESLoggerFactory.getLogger(IngestTransportDuplicateIDTest.class.getSimpleName());

    private final static Integer MAX_ACTIONS = 1000;

    private final static Integer NUM_ACTIONS = 12345;

    @Test
    public void testDuplicateDocIDs() throws Exception {
        final IngestTransportClient client = new IngestTransportClient()
                .maxActionsPerRequest(MAX_ACTIONS)
                .init(getSettings())
                .newIndex("test");
        try {
            for (int i = 0; i < NUM_ACTIONS; i++) {
                client.index("test", "test", randomString(1), "{ \"name\" : \"" + randomString(32) + "\"}");
            }
            client.flushIngest();
            logger.info("flushed, waiting for responses");
            client.waitForResponses(TimeValue.timeValueSeconds(30));
            logger.info("refreshing");
            client.refreshIndex("test");
            logger.info("searching");
            SearchRequestBuilder searchRequestBuilder = new SearchRequestBuilder(client.client(), SearchAction.INSTANCE)
                    .setIndices("test")
                    .setTypes("test")
                    .setQuery(matchAllQuery());
            long hits = searchRequestBuilder.execute().actionGet().getHits().getTotalHits();
            logger.info("hits = {}", hits);
            assertTrue(hits < NUM_ACTIONS);
        } catch (NoNodeAvailableException e) {
            logger.warn("skipping, no node available");
        } catch (Throwable t) {
            logger.error("oops", t);
        } finally {
            logger.info("shutting down client");
            client.shutdown();
            assertEquals(NUM_ACTIONS / MAX_ACTIONS + 1, client.getMetric().getTotalIngest().count());
            if (client.hasThrowable()) {
                logger.error("error", client.getThrowable());
            }
            assertFalse(client.hasThrowable());
            logger.info("done");
        }
    }
}
