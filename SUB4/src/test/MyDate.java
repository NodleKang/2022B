package test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MyDate {

    // 현재 시각을 LocalDateTime 객체로 반환
    public LocalDateTime now() {
        // 현재 시각 가져오기
        LocalDateTime now = LocalDateTime.now();
        return now;
    }

    // 시각을 문자열로 반환 (추천 포멧: "yyyy-MM-dd HH:mm:ss")
    public String convertDateToString(LocalDateTime localDateTime, String format) {
        // 포맷 지정
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        // 문자열로 변환
        String formattedDateTime = localDateTime.format(formatter);

        return formattedDateTime;
    }

    // 문자열을 시각으로 반환
    public LocalDateTime convertStringToDate(String dateTimeString, String format) {
        // 포맷 지정
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        // 문자열을 시각으로 변환
        LocalDateTime localDateTime = LocalDateTime.parse(dateTimeString, formatter);

        return localDateTime;
    }
}
