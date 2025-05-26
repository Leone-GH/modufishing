// FishingIndexRegion.java
package com.fishtripplanner.api.khoa;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public enum FishingIndexRegionType {

    BUSAN("부산", List.of("부산", "부산광역시", "영도", "해운대")),
    GEOJE("거제도", List.of("거제", "거제도")),
    YEOSU("여수", List.of("여수")),
    JINDO("진도", List.of("진도")),
    WANDO("완도", List.of("완도")),
    MOKPO("목포", List.of("목포")),
    SINAN("신안", List.of("신안", "흑산도", "비금도", "도초도")),
    GUNSAN("군산", List.of("군산")),
    BORYEONG("보령", List.of("보령", "대천")),
    INCHEON("인천", List.of("인천")),
    DONGHAE("동해", List.of("동해", "묵호")),
    SOKCHO("속초", List.of("속초")),
    SAMCHEOK("삼척", List.of("삼척")),
    ULSAN("울산", List.of("울산")),
    POHANG("포항", List.of("포항")),
    MUKHO("묵호", List.of("묵호")),
    HUPOHANG("후포항", List.of("후포", "후포항")),
    DAEHEUNG("대흥", List.of("대흥")),
    INCHEONHANG("인천항", List.of("인천항")),
    ANPYEONGHANG("안평항", List.of("안평항")),
    MOKPOHANG("목포항", List.of("목포항")),
    YEOSU_NAMBUL("여수 남불", List.of("여수남불", "남불")),
    DODOOHANG("도두항", List.of("도두항")),
    HALLIHANG("하리항", List.of("하리항")),
    JUKPOHANG("죽포항", List.of("죽포항")),
    YANGPOHANG("양포항", List.of("양포항")),
    GANGREUNGHANG("강릉항", List.of("강릉항")),
    GONGHYUNJINHANG("공현진항", List.of("공현진항"));
    public static void main(String[] args) {
        System.out.println(FishingIndexRegionType.BUSAN);
    }
    @Getter
    private final String apiRegionName;
    private final List<String> keywords;

    FishingIndexRegionType(String apiRegionName, List<String> keywords) {
        this.apiRegionName = apiRegionName;
        this.keywords = keywords;
    }

    public static Optional<String> mapToApiRegion(String input) {
        return Arrays.stream(values())
                .filter(region -> region.keywords.stream().anyMatch(input::contains))
                .map(FishingIndexRegionType::getApiRegionName)
                .findFirst();
    }
}