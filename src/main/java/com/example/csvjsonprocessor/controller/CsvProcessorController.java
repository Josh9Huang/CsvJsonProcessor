package com.example.csvjsonprocessor.controller;

import com.example.csvjsonprocessor.service.CsvJsonProcessorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Scanner;

@Component
public class CsvProcessorController implements CommandLineRunner {
    
    @Autowired
    private CsvJsonProcessorService csvJsonProcessorService;
    
    @Override
    public void run(String... args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== CSV JSON資料處理器 ===");
        System.out.print("請輸入來源CSV檔案路徑: ");
        String inputPath = scanner.nextLine();
        
        System.out.print("請輸入目標CSV檔案路徑: ");
        String outputPath = scanner.nextLine();
        
        try {
            csvJsonProcessorService.processCsvFile(inputPath, outputPath);
            System.out.println("檔案處理完成！");
        } catch (Exception e) {
            System.err.println("處理檔案時發生錯誤: " + e.getMessage());
            e.printStackTrace();
        }
        
        scanner.close();
    }
}
