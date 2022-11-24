package org.oneedtech.inspect.vc.probe;

import java.io.StringReader;
import java.net.URI;
import java.util.Map;

import org.oneedtech.inspect.core.probe.Probe;
import org.oneedtech.inspect.core.probe.RunContext;
import org.oneedtech.inspect.core.report.ReportItems;
import org.oneedtech.inspect.vc.Credential;
import org.oneedtech.inspect.vc.util.CachingDocumentLoader;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdOptions;
import com.apicatalog.jsonld.api.CompactionApi;
import com.apicatalog.jsonld.document.JsonDocument;

import jakarta.json.JsonObject;


public class JsonLDCompactionProve extends Probe<Credential> {
    private final String context;
    private final Map<URI, String> localDomains;

    public JsonLDCompactionProve(String context) {
        this(context, null);
    }

    public JsonLDCompactionProve(String context, Map<URI, String> localDomains) {
        super(ID);
        this.context = context;
        this.localDomains = localDomains;
    }

    @Override
    public ReportItems run(Credential crd, RunContext ctx) throws Exception {
		try {
            // compact JSON
            JsonDocument jsonDocument = JsonDocument.of(new StringReader(crd.getJson().toString()));
            CompactionApi compactApi = JsonLd.compact(jsonDocument, context);
            compactApi.options(new JsonLdOptions(new CachingDocumentLoader(localDomains)));
            JsonObject compactedObject = compactApi.get();

            // TODO: Handle mismatch between URL node source and declared ID.
            if (compactedObject.get("id") != null && crd.getResource().getID() != null
              && !compactedObject.get("id").toString().equals(crd.getResource().getID())) {

                // TODO: warning!!
                // report_message(
                //     "Node fetched from source {} declared its id as {}".format(
                //         task_meta['node_id'], node_id), MESSAGE_LEVEL_WARNING, success=False
                // ),

            }
			return success(this, ctx);

		} catch (Exception e) {
			return fatal("Error while parsing credential: " + e.getMessage(), ctx);
		}
    }

	public static final String ID = JsonLDCompactionProve.class.getSimpleName();
}
