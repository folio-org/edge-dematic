package org.folio.ed.client;

import static org.folio.spring.integration.XOkapiHeaders.TENANT;
import static org.folio.spring.integration.XOkapiHeaders.TOKEN;

import org.folio.ed.domain.dto.AccessionQueueRecord;
import org.folio.ed.domain.dto.Configuration;
import org.folio.ed.domain.dto.ResultList;
import org.folio.ed.domain.dto.RetrievalQueueRecord;
import org.folio.ed.domain.request.ItemBarcodeRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

@HttpExchange(url = "remote-storage")
public interface RemoteStorageClient {
  @GetExchange("/accessions")
  ResultList<AccessionQueueRecord> getAccessionsByQuery(@RequestParam("storageId") String storageId,
      @RequestParam("accessioned") boolean retrieved, @RequestParam("limit") int limit, @RequestHeader(TENANT) String tenantId,
      @RequestHeader(TOKEN) String okapiToken);

  @GetExchange("/retrievals")
  ResultList<RetrievalQueueRecord> getRetrievalsByQuery(@RequestParam("storageId") String storageId,
      @RequestParam("retrieved") boolean retrieved, @RequestParam("limit") int limit, @RequestHeader(TENANT) String tenantId,
      @RequestHeader(TOKEN) String okapiToken);

  @PutExchange("/accessions/barcode/{barcode}")
  ResponseEntity<String> setAccessionedByBarcode(@PathVariable("barcode") String barcode, @RequestHeader(TENANT) String tenantId,
      @RequestHeader(TOKEN) String okapiToken);

  @PutExchange("/retrievals/barcode/{barcode}")
  ResponseEntity<String> setRetrievalByBarcode(@PathVariable("barcode") String barcode, @RequestHeader(TENANT) String tenantId,
      @RequestHeader(TOKEN) String okapiToken);

  @PostExchange("/retrieve/{configurationId}/checkInItem")
  ResponseEntity<String> checkInItem(@PathVariable("configurationId") String configurationId, @RequestBody ItemBarcodeRequest itemBarcodeRequest,
      @RequestHeader(TENANT) String tenantId, @RequestHeader(TOKEN) String okapiToken);

  @PostExchange("/return/{configurationId}")
  ResponseEntity<String> returnItem(@PathVariable("configurationId") String configurationId, @RequestBody ItemBarcodeRequest itemBarcodeRequest,
      @RequestHeader(TENANT) String tenantId, @RequestHeader(TOKEN) String okapiToken);

  @GetExchange("/configurations")
  ResultList<Configuration> getStorageConfigurations(@RequestHeader(TENANT) String tenantId,
      @RequestHeader(TOKEN) String okapiToken);

  @PostExchange("/items/barcode/{barcode}/markAsMissing")
  ResponseEntity<String> markItemAsMissing(@PathVariable String barcode, @RequestHeader(TENANT) String tenantId,
      @RequestHeader(TOKEN) String okapiToken);
}
