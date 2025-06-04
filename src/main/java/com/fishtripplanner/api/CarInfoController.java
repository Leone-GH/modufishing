package com.fishtripplanner.api;

import com.opencsv.CSVReader;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/car")
@RequiredArgsConstructor
public class CarInfoController {

    private static final String FILE_PATH = "static/csv/FuelInfo.csv";
    private static final Charset FILE_ENCODING = Charset.forName("EUC-KR");

    // CSV 파일 불러오기
    private List<String[]> loadCsv() throws Exception {
        var resource = new ClassPathResource(FILE_PATH);
        try (var reader = new CSVReader(new InputStreamReader(resource.getInputStream(), FILE_ENCODING))) {
            return reader.readAll();
        }
    }

    // 차량 모델 자동완성 검색
    @GetMapping("/search")
    public ResponseEntity<List<String>> search(@RequestParam("keyword") String keyword) throws Exception {
        var rows = loadCsv();
        List<String> results = rows.stream()
                .skip(1)
                .map(r -> {
                    if (r.length >= 2) return r[0] + " " + r[1];  // 브랜드 + 모델명
                    else return "";
                })
                .filter(name -> name.toLowerCase().contains(keyword.toLowerCase()))
                .distinct()
                .limit(10)
                .collect(Collectors.toList());
        return ResponseEntity.ok(results);
    }

    // 차량 모델 정보 가져오기
    @GetMapping("/model")
    public ResponseEntity<Map<String, Object>> getModel(@RequestParam("name") String name) throws Exception {
        var rows = loadCsv();
        for (String[] r : rows) {
            if (r.length < 5) continue;

            String brand = r[0].trim();
            String model = r[1].trim();
            String fullName = brand + " " + model;

// 괄호, 공백 제거하고 소문자로 통일
            String normalizedInput = name.replaceAll("[()\\s]", "").toLowerCase();
            String normalizedModel = model.replaceAll("[()\\s]", "").toLowerCase();
            String normalizedFull = fullName.replaceAll("[()\\s]", "").toLowerCase();

// 🔥 정규화된 값끼리 비교
            if (normalizedModel.contains(normalizedInput) || normalizedFull.contains(normalizedInput)) {
                System.out.println("✅ 일치하는 모델 발견: " + fullName);
                String fuelType = r[2];
                String cityEffRaw = r[3];
                String highwayEffRaw = r[4];

                if (!isNumeric(cityEffRaw) || !isNumeric(highwayEffRaw)) {
                    return ResponseEntity.badRequest().body(Map.of("error", "연비 정보가 잘못되었습니다."));
                }

                Map<String, Object> result = new HashMap<>();
                result.put("fuelType", fuelType);
                result.put("cityEff", Double.parseDouble(cityEffRaw));
                result.put("highwayEff", Double.parseDouble(highwayEffRaw));
                return ResponseEntity.ok(result);
            }
        }
        return ResponseEntity.status(404).body(Map.of("error", "모델명을 찾을 수 없습니다."));
    }

    private boolean isNumeric(String str) {
        if (str == null || str.trim().isEmpty()) return false;
        try {
            Double.parseDouble(str.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
