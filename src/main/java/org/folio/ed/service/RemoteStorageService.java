package org.folio.ed.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.folio.ed.client.RemoteStorageClient;
import org.folio.ed.converter.AccessionQueueRecordToAsrItemConverter;
import org.folio.ed.converter.RetrievalQueueRecordToAsrRequestConverter;
import org.folio.ed.domain.dto.AccessionQueueRecord;
import org.folio.ed.domain.dto.Configuration;
import org.folio.ed.domain.dto.RetrievalQueueRecord;
import org.folio.ed.domain.request.ItemBarcodeRequest;
import org.folio.ed.domain.dto.AsrItems;
import org.folio.ed.domain.dto.AsrRequests;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RemoteStorageService {
  private static final String STAGING_DIRECTOR_NAME = "Dematic_SD";

  private final Map<String, List<RetrievalQueueRecord>> retrievalsMap = new HashMap<>();

  private final RemoteStorageClient remoteStorageClient;
  private final AccessionQueueRecordToAsrItemConverter accessionQueueRecordToAsrItemConverter;
  private final RetrievalQueueRecordToAsrRequestConverter retrievalQueueRecordToAsrRequestConverter;

  public List<AccessionQueueRecord> getAccessionQueueRecords(String storageId) {
    return remoteStorageClient.getAccessionsByQuery("storageId=" + storageId + "&accessioned=false")
      .getResult();
  }

  public List<RetrievalQueueRecord> getRetrievalQueueRecords(String storageId) {
    retrievalsMap.put(storageId, remoteStorageClient.getRetrievalsByQuery("storageId=" + storageId + "&retrieved=false")
      .getResult());
    return retrievalsMap.get(storageId);
  }

  public RetrievalQueueRecord getRetrievalByBarcode(String barcode, String configId) {
    return retrievalsMap.getOrDefault(configId, Collections.emptyList()).stream()
      .filter(record -> barcode.equals(record.getItemBarcode()))
      .findAny().orElse(null);
  }

  public List<Configuration> getStagingDirectorConfigurations() {
    return remoteStorageClient.getStorageConfigurations()
      .getConfigurations()
      .stream()
      .filter(configuration -> STAGING_DIRECTOR_NAME.equals(configuration.getProviderName()))
      .collect(Collectors.toList());
  }

  public AsrItems getAsrItems(String storageId) {
    var asrItems = new AsrItems();
    asrItems.asrItems(getAccessionQueueRecords(storageId).stream()
      .map(accessionQueueRecordToAsrItemConverter::convert)
      .collect(Collectors.toList()));
    return asrItems;
  }

  public ResponseEntity<String> checkInItemByBarcode(String remoteStorageConfigurationId, String itemBarcode) {
    var itemBarcodeRequest = new ItemBarcodeRequest();
    itemBarcodeRequest.setItemBarcode(itemBarcode);
    return remoteStorageClient.checkInItem(remoteStorageConfigurationId, itemBarcodeRequest);
  }

  public ResponseEntity<String> returnItemByBarcode(String remoteStorageConfigurationId, String itemBarcode) {
    var itemBarcodeRequest = new ItemBarcodeRequest();
    itemBarcodeRequest.setItemBarcode(itemBarcode);
    return remoteStorageClient.returnItem(remoteStorageConfigurationId, itemBarcodeRequest);
  }

  public AsrRequests getRequests(String remoteStorageConfigurationId) {
    var asrRequests = new AsrRequests();
    asrRequests
      .asrRequests(remoteStorageClient.getRetrievalsByQuery("storageId=" + remoteStorageConfigurationId + "&retrieved=false")
        .getResult()
        .stream()
        .map(retrievalQueueRecordToAsrRequestConverter::convert)
        .collect(Collectors.toList()));
    return asrRequests;
  }

  @Async
  public ResponseEntity<String> setAccessionedAsync(String itemBarcode) {
    return remoteStorageClient.setAccessionedByBarcode(itemBarcode);
  }

  @Async
  public ResponseEntity<String> setRetrievedAsync(String itemBarcode) {
    return remoteStorageClient.setRetrievalByBarcode(itemBarcode);
  }

}
