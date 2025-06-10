package com.fishtripplanner.api.khoa;

import java.util.Arrays;
import java.util.Optional;

public enum AreaCodes {

    // 갯바위 낚시 (rock)
    아야진항("24", "38.5649", "128.4536", AreaType.ROCK),
    외웅치항("23", "38.5267", "128.4536", AreaType.ROCK),
    남애항("34", "38.3071", "128.551", AreaType.ROCK),
    대진항("33", "38.4768", "128.4531", AreaType.ROCK),
    울릉도("29", "37.4892", "130.9062", AreaType.ROCK),
    울진("9", "36.9931", "129.4001", AreaType.ROCK),
    후포("19", "36.6787", "129.4605", AreaType.ROCK),
    포항("18", "36.019", "129.3435", AreaType.ROCK),
    울산("8", "35.5384", "129.3114", AreaType.ROCK),
    부산동부("26", "35.1796", "129.0756", AreaType.ROCK),
    부산남부("28", "35.0987", "129.0401", AreaType.ROCK),
    부산서부("27", "35.1046", "128.9742", AreaType.ROCK),
    거제도("1", "34.8801", "128.6216", AreaType.ROCK),
    욕지도("6", "34.6286", "128.2614", AreaType.ROCK),
    연도("5", "34.3583", "127.9494", AreaType.ROCK),
    거문도("17", "34.0286", "127.3054", AreaType.ROCK),
    성산포("4", "33.4563", "126.9254", AreaType.ROCK),
    서귀포("3", "33.253", "126.5618", AreaType.ROCK),
    비양도("32", "33.389", "126.2134", AreaType.ROCK),
    김녕("25", "33.5564", "126.7528", AreaType.ROCK),
    신지도("13", "34.1019", "126.7802", AreaType.ROCK),
    추자도("7", "33.964", "126.3", AreaType.ROCK),
    하조도("14", "34.072", "126.3202", AreaType.ROCK),
    가거도("10", "34.074", "125.1126", AreaType.ROCK),
    상태도("15", "34.6586", "125.1287", AreaType.ROCK),
    비금도("31", "34.7674", "125.9731", AreaType.ROCK),
    계마항("30", "35.0407", "126.7052", AreaType.ROCK),
    상왕등도("12", "35.8392", "126.1463", AreaType.ROCK),
    신시도("2", "35.8384", "126.5496", AreaType.ROCK),
    방포항("22", "36.5006", "126.3334", AreaType.ROCK),
    어청도("16", "36.0645", "125.9996", AreaType.ROCK),
    모항항("21", "36.7206", "126.224", AreaType.ROCK),
    국화도("11", "37.1635", "126.5001", AreaType.ROCK),
    영흥도("20", "37.3167", "126.4881", AreaType.ROCK),

    // 선상 낚시 (boat)
    공현진항동남동("3", "38.5194", "128.5365", AreaType.BOAT),
    강릉항북동("6", "37.7674", "128.9518", AreaType.BOAT),
    양포항남동("8", "37.3081", "129.3672", AreaType.BOAT),
    하리항남서("9", "34.7264", "128.0372", AreaType.BOAT),
    척포항남남서("5", "34.6242", "126.1287", AreaType.BOAT),
    목포북항서측("7", "34.8113", "126.3426", AreaType.BOAT),
    안흥항북측("4", "36.6713", "126.1261", AreaType.BOAT),
    인천항서측("1", "37.4325", "126.5399", AreaType.BOAT),

    // 날씨정보 (trip)
    인천북서("ICNWIS","37.7432","126.3700",AreaType.TRIP),
    인천내륙("ICCN","37.4563","126.7052",AreaType.TRIP),
    인천남서("ICSWIS","37.3702","126.5880",AreaType.TRIP),
    태안북부("TAN","36.9127","126.2730",AreaType.TRIP),
    태안남부("TAS","36.6678","126.2024",AreaType.TRIP),
    부안("BA","35.7274","126.7339",AreaType.TRIP),
    신안북동("SANE","35.0092","126.1683",AreaType.TRIP),
    신안남서("SASW","34.6045","125.9101",AreaType.TRIP),
    해남("HN","34.5744","126.5988",AreaType.TRIP),
    제주북서("JJNW","33.5115","126.4512",AreaType.TRIP),
    제주남서("JJSW","33.2447","126.3884",AreaType.TRIP),
    제주남동("JJSE","33.2405","126.5657",AreaType.TRIP),
    제주북동("JJNE","33.5533","126.7286",AreaType.TRIP),
    여수내륙("YSCN","34.7604","127.6622",AreaType.TRIP),
    여수도서("YSIS","34.5824","127.7497",AreaType.TRIP),
    통영("TY","34.8543","128.4333",AreaType.TRIP),
    부산남서("BSSW","35.0976","129.0215",AreaType.TRIP),
    부산북동("BSNE","35.2565","129.0796",AreaType.TRIP),
    포항날씨("PH","36.0190","129.3435",AreaType.TRIP),
    영덕("YD","36.4151","129.3650",AreaType.TRIP),
    강릉("GN","37.7519","128.8761",AreaType.TRIP),
    속초("SC","38.2048","128.5911",AreaType.TRIP);

    private final String code;
    private final String lat;
    private final String lon;
    private final AreaType type;

    AreaCodes(String code, String lat, String lon, AreaType type) {
        this.code = code;
        this.lat = lat;
        this.lon = lon;
        this.type = type;
    }

    public static Optional<AreaCodes> findByName(String name) {
        return Arrays.stream(values())
                .filter(ac -> ac.name().equals(name))
                .findFirst();
    }

    public String getLatitude() {
        return lat;
    }

    public String getLongitude() {
        return lon;
    }

    public String getCode() {
        return this.code;
    }

    public AreaType getType() {
        return this.type;
    }

    public enum AreaType {
        ROCK, BOAT, TRIP
    }
}

