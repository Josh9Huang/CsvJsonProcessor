package com.example.csvjsonprocessor.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CsvJsonProcessorServiceTest {
    
    @TempDir
    Path tempDir;
    
    @Test
    void testProcessCsvFile() throws Exception {
        CsvJsonProcessorService service = new CsvJsonProcessorService();
        
        // 建立測試用的CSV檔案
        Path inputFile = tempDir.resolve("test_input.csv");
        Path outputFile = tempDir.resolve("test_output.csv");
        
        String csvContent = """
            id,json_data,name
            1,"{""data"":{""user"":""John"",""imageInfos"":{""url"":""test.jpg"",""size"":1024,""result"":""success""}}}",Test1
            2,"{""data"":{""user"":""Jane"",""imageInfos"":[{""url"":""img1.jpg"",""result"":""ok""},{""url"":""img2.jpg"",""result"":""failed""}]}}",Test2
            """;
        
        Files.writeString(inputFile, csvContent);
        
        // 執行處理
        service.processCsvFile(inputFile.toString(), outputFile.toString());
        
        // 驗證結果
        assertTrue(Files.exists(outputFile));
        String result = Files.readString(outputFile);
        
        // 檢查imageInfos中的result參數是否已被移除
        assertFalse(result.contains("\"result\""));
        assertTrue(result.contains("\"user\""));
        assertTrue(result.contains("\"imageInfos\""));
        assertTrue(result.contains("\"url\""));
    }
}
