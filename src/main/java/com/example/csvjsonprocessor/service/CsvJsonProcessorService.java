package com.example.csvjsonprocessor.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvJsonProcessorService {
    
    private static final Logger logger = LoggerFactory.getLogger(CsvJsonProcessorService.class);
    private final ObjectMapper objectMapper;
    
    public CsvJsonProcessorService() {
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * 處理CSV檔案中的JSON資料
     * @param inputFilePath 輸入CSV檔案路徑
     * @param outputFilePath 輸出CSV檔案路徑
     * @throws IOException 檔案操作異常
     */
    public void processCsvFile(String inputFilePath, String outputFilePath) throws Exception {
        logger.info("開始處理CSV檔案: {}", inputFilePath);
        
        List<String[]> processedRows = new ArrayList<>();
        String[] headers = null;
        
        // 讀取CSV檔案
        try (CSVReader csvReader = new CSVReader(new FileReader(inputFilePath))) {
            String[] nextLine;
            boolean isFirstRow = true;
            
            while ((nextLine = csvReader.readNext()) != null) {
                if (isFirstRow) {
                    headers = nextLine.clone();
                    processedRows.add(headers);
                    isFirstRow = false;
                    continue;
                }
                
                String[] processedRow = processJsonInRow(nextLine);
                processedRows.add(processedRow);
            }
        }
        
        // 寫入處理後的資料到新的CSV檔案
        try (CSVWriter csvWriter = new CSVWriter(new FileWriter(outputFilePath))) {
            csvWriter.writeAll(processedRows);
            logger.info("處理完成，結果已儲存至: {}", outputFilePath);
        }
    }
    
    /**
     * 處理CSV行中的JSON資料
     * @param row CSV行資料
     * @return 處理後的行資料
     */
    private String[] processJsonInRow(String[] row) {
        String[] processedRow = new String[row.length];
        
        for (int i = 0; i < row.length; i++) {
            String cellValue = row[i];
            
            // 檢查是否為JSON格式
            if (isJsonFormat(cellValue)) {
                try {
                    String processedJson = extractDataAndRemoveResult(cellValue);
                    processedRow[i] = processedJson;
                    logger.debug("已處理JSON資料於欄位 {}", i);
                } catch (Exception e) {
                    logger.warn("處理JSON資料時發生錯誤，保留原始資料: {}", e.getMessage());
                    processedRow[i] = cellValue;
                }
            } else {
                processedRow[i] = cellValue;
            }
        }
        
        return processedRow;
    }
    
    /**
     * 檢查字串是否為JSON格式
     * @param str 待檢查的字串
     * @return 是否為JSON格式
     */
    private boolean isJsonFormat(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }
        
        str = str.trim();
        return (str.startsWith("{") && str.endsWith("}")) || 
               (str.startsWith("[") && str.endsWith("]"));
    }
    
    /**
     * 提取data參數並從imageInfos中移除result參數
     * @param jsonStr 原始JSON字串
     * @return 處理後的JSON字串
     * @throws IOException JSON處理異常
     */
    private String extractDataAndRemoveResult(String jsonStr) throws IOException {
        JsonNode rootNode = objectMapper.readTree(jsonStr);
        
        // 檢查是否有data節點
        if (rootNode.has("data")) {
            JsonNode dataNode = rootNode.get("data");
            
            // 如果data是物件類型，處理其中的imageInfos
            if (dataNode.isObject()) {
                ObjectNode dataObjectNode = (ObjectNode) dataNode;
                
                // 檢查是否有imageInfos節點
                if (dataObjectNode.has("imageInfos")) {
                    JsonNode imageInfosNode = dataObjectNode.get("imageInfos");
                    
                    // 如果imageInfos是物件，移除其中的result參數
                    if (imageInfosNode.isObject()) {
                        ObjectNode imageInfosObjectNode = (ObjectNode) imageInfosNode;
                        imageInfosObjectNode.remove("result");
                        logger.debug("已從imageInfos物件中移除result參數");
                    }
                    // 如果imageInfos是陣列，處理陣列中每個物件的result參數
                    else if (imageInfosNode.isArray()) {
                        for (JsonNode imageInfo : imageInfosNode) {
                            if (imageInfo.isObject()) {
                                ObjectNode imageInfoObjectNode = (ObjectNode) imageInfo;
                                imageInfoObjectNode.remove("result");
                            }
                        }
                        logger.debug("已從imageInfos陣列中的物件移除result參數");
                    }
                }
                
                return objectMapper.writeValueAsString(dataObjectNode);
            } else {
                // 如果data不是物件，直接返回data的值
                return objectMapper.writeValueAsString(dataNode);
            }
        } else {
            // 如果沒有data節點，檢查根節點是否有imageInfos並處理
            if (rootNode.isObject()) {
                ObjectNode rootObjectNode = (ObjectNode) rootNode;
                
                if (rootObjectNode.has("imageInfos")) {
                    JsonNode imageInfosNode = rootObjectNode.get("imageInfos");
                    
                    if (imageInfosNode.isObject()) {
                        ObjectNode imageInfosObjectNode = (ObjectNode) imageInfosNode;
                        imageInfosObjectNode.remove("result");
                    } else if (imageInfosNode.isArray()) {
                        for (JsonNode imageInfo : imageInfosNode) {
                            if (imageInfo.isObject()) {
                                ObjectNode imageInfoObjectNode = (ObjectNode) imageInfo;
                                imageInfoObjectNode.remove("result");
                            }
                        }
                    }
                }
                
                return objectMapper.writeValueAsString(rootObjectNode);
            }
        }
        
        return jsonStr;
    }
}
