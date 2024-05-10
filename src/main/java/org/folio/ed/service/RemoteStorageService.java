package org.folio.ed.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.extern.log4j.Log4j2;
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
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Log4j2
public class RemoteStorageService {

  private static final String STAGING_DIRECTOR_NAME = "DEMATIC_SD";

  private final Map<String, List<RetrievalQueueRecord>> retrievalsMap = new HashMap<>();
  private final Map<String, LocalDateTime> lastMessageTimesMap = new HashMap<>();

  private final RemoteStorageClient remoteStorageClient;
  private final AccessionQueueRecordToAsrItemConverter accessionQueueRecordToAsrItemConverter;
  private final RetrievalQueueRecordToAsrRequestConverter retrievalQueueRecordToAsrRequestConverter;

  public List<AccessionQueueRecord> getAccessionQueueRecords(String storageId, String tenantId, String okapiToken) {
    log.debug("getAccessionQueueRecords :: storageId:{} tenantId:{}",storageId,tenantId);
    return remoteStorageClient.getAccessionsByQuery(storageId, false, Integer.MAX_VALUE, tenantId, okapiToken)
      .getResult();
  }

  public List<RetrievalQueueRecord> getRetrievalQueueRecords(String storageId, String tenantId, String okapiToken) {
    log.debug("getRetrievalQueueRecords :: storageId:{} tenantId:{}",storageId,tenantId);
    retrievalsMap.put(storageId, remoteStorageClient.getRetrievalsByQuery(storageId, false, Integer.MAX_VALUE, tenantId, okapiToken).getResult());
    return retrievalsMap.get(storageId);
  }

  public RetrievalQueueRecord getRetrievalByBarcode(String barcode, String configId) {
    log.debug("getRetrievalByBarcode :: barcode:{} configId:{}",barcode,configId);
    return retrievalsMap.getOrDefault(configId, Collections.emptyList()).stream()
      .filter(record -> barcode.equals(record.getItemBarcode()))
      .findAny().orElse(null);
  }


  public void removeBarcodeFromMap(String barcode, String configId) {
    log.debug("removeBarcodeFromMap :: barcode:{} configId:{}", barcode, configId);
    synchronized (retrievalsMap) {
      List<RetrievalQueueRecord> retrievals = retrievalsMap.getOrDefault(configId, Collections.emptyList());
      List<RetrievalQueueRecord> updatedRetrievals = new ArrayList<>(retrievals);
      updatedRetrievals.removeIf(record -> barcode.equals(record.getItemBarcode()));
      retrievalsMap.put(configId, updatedRetrievals);
    }
  }

  public List<Configuration> getStagingDirectorConfigurations(String tenantId, String okapiToken) {
    log.debug("getStagingDirectorConfigurations :: tenantId:{}",tenantId);
    List<Configuration> stagingDirectorConfigurations = new ArrayList<>();
    remoteStorageClient.getStorageConfigurations(tenantId, okapiToken)
      .getResult()
      .forEach(configuration -> {
        configuration.setTenantId(tenantId);
        if (STAGING_DIRECTOR_NAME.equals(configuration.getProviderName())) {
          stagingDirectorConfigurations.add(configuration);
        }
      });
    return stagingDirectorConfigurations;
  }

  public AsrItems getAsrItems(String remoteStorageConfigurationId, String tenantId, String okapiToken) {
    log.debug("getAsrItems :: tenantId:{}",tenantId);
    var asrItems = new AsrItems();
    asrItems.asrItems(getAccessionQueueRecords(remoteStorageConfigurationId, tenantId, okapiToken).stream()
      .map(accessionQueueRecordToAsrItemConverter::convert)
      .toList());
    return asrItems;
  }

  public ResponseEntity<String> checkInItemByBarcode(String remoteStorageConfigurationId, String itemBarcode, String tenantId,
      String okapiToken) {
    log.debug("checkInItemByBarcode :: remoteStorageConfigurationId:{} itemBarcode:{}",remoteStorageConfigurationId,itemBarcode);
    var itemBarcodeRequest = new ItemBarcodeRequest();
    itemBarcodeRequest.setItemBarcode(itemBarcode);
    return remoteStorageClient.checkInItem(remoteStorageConfigurationId, itemBarcodeRequest, tenantId, okapiToken);
  }

  public ResponseEntity<String> returnItemByBarcode(String remoteStorageConfigurationId, String itemBarcode, String tenantId, String okapiToken) {
    log.debug("returnItemByBarcode :: remoteStorageConfigurationId:{} itemBarcode:{}",remoteStorageConfigurationId,itemBarcode);
    var itemBarcodeRequest = new ItemBarcodeRequest();
    itemBarcodeRequest.setItemBarcode(itemBarcode);
    return remoteStorageClient.returnItem(remoteStorageConfigurationId, itemBarcodeRequest, tenantId, okapiToken);
  }

  public AsrRequests getRequests(String remoteStorageConfigurationId, String tenantId, String okapiToken) {
    log.debug("getRequests :: remoteStorageConfigurationId:{} tenantId:{}",remoteStorageConfigurationId,tenantId);
    var asrRequests = new AsrRequests();
    asrRequests.asrRequests(remoteStorageClient
      .getRetrievalsByQuery(remoteStorageConfigurationId, false, Integer.MAX_VALUE, tenantId, okapiToken)
      .getResult()
      .stream()
      .map(retrievalQueueRecordToAsrRequestConverter::convert)
      .toList());
    return asrRequests;
  }

  public ResponseEntity<String> setAccessionedAsync(String itemBarcode, String tenantId, String okapiToken) {
    log.debug("setAccessionedAsync :: itemBarcode:{} tenantId:{}",itemBarcode,tenantId);
    return remoteStorageClient.setAccessionedByBarcode(itemBarcode, tenantId, okapiToken);
  }

  public ResponseEntity<String> setRetrievedAsync(String itemBarcode, String tenantId, String okapiToken) {
    log.debug("setRetrievedAsync :: itemBarcode:{} tenantId:{}",itemBarcode,tenantId);
    return remoteStorageClient.setRetrievalByBarcode(itemBarcode, tenantId, okapiToken);
  }

  public ResponseEntity<String> markItemAsMissingAsync(String itemBarcode, String tenantId, String okapiToken) {
    log.debug("markItemAsMissingAsync :: itemBarcode:{} tenantId:{}",itemBarcode,tenantId);
    return remoteStorageClient.markItemAsMissing(itemBarcode, tenantId, okapiToken);
  }

  public void updateLastMessageTime(String configId) {
    lastMessageTimesMap.put(configId, LocalDateTime.now());
  }

  public LocalDateTime getLastMessageTime(String configId) {
    return lastMessageTimesMap.get(configId);
  }
}
