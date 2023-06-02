package io.quarkiverse.opensearch.restclient.highlevel.runtime;

import java.io.IOException;
import java.util.List;

import org.opensearch.client.RestClient;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.common.CheckedConsumer;
import org.opensearch.core.xcontent.NamedXContentRegistry;

/**
 * The RestHighLevelClient cannot be built with an existing RestClient.
 * <p>
 * The only (and documented - see javadoc) way to do it is to subclass it and use its protected constructor.
 */
class QuarkusRestHighLevelClient extends RestHighLevelClient {

    protected QuarkusRestHighLevelClient(RestClient restClient, CheckedConsumer<RestClient, IOException> doClose,
            List<NamedXContentRegistry.Entry> namedXContentEntries) {
        super(restClient, doClose, namedXContentEntries);
    }
}
