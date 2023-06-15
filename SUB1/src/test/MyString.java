package test;

import java.util.*;

public class MyString {

    // 문자열에 '#'이나 ':'과 같은 여러 구분 기호가 모두 포함되어 있는 경우 delimiter에 정규식을 사용하는 예
    // str.split("[#:]");

    // 문자열(str)을 주어진 구분자(delimiter)로 분리하여 문자열 배열로 반환
    public static String[] splitToStringArray(String str, String delimiter) {
        String[] strArr = str.split(delimiter);
        return strArr;
    }

    // 문자열(str)을 주어진 구분자(delimiter)로 분리하여 문자열 배열로 반환(빈 문자열 제거 여부 선택)
    public static String[] splitToStringArray(String str, String delimiter, boolean removeEmptyString) {
        String[] strArr = str.split(delimiter);
        ArrayList<String> strList = new ArrayList<String>();
        for (String s : strArr) {
            // 빈 문자열 제거 여부가 true이고 빈 문자열인 경우에는 리스트에 추가하지 않음
            if (removeEmptyString && s.isEmpty()) {
                continue;
            }
            strList.add(s);
        }
        strArr = strList.toArray(new String[strList.size()]);
        return strArr;
    }

    // 문자열(str)을 주어진 구분자(delimiter)로 분리하여 순서가 있는 문자열 리스트로 반환
    public static LinkedList<String> splitToLinkedList(String str, String delimiter) {
        String[] strArr = str.split(delimiter);
        LinkedList<String> strList = new LinkedList<String>();
        for (String s : strArr) {
            strList.add(s);
        }
        return strList;
    }

    // 문자열(str)을 주어진 구분자(delimiter)로 분리하여 순서가 있는 문자열 리스트로 반환 (빈 문자열 제거)
    public static LinkedList<String> splitToLinkedList(String str, String delimiter, boolean removeEmptyString) {
        String[] strArr = str.split(delimiter);
        LinkedList<String> strList = new LinkedList<String>();
        for (String s : strArr) {
            // 빈 문자열 제거 여부가 true이고 빈 문자열인 경우에는 리스트에 추가하지 않음
            if (removeEmptyString && s.isEmpty()) {
                continue;
            }
            strList.add(s);
        }
        return strList;
    }

    // 문자열 배열을 이어붙인 문자열 반환
    public static String joinString(String[] strArr, String delimiter) {
        // String.join() 메소드는 Java 8부터 지원
        String str = String.join(delimiter, strArr);
        return str;
    }

    // 문자열 리스트를 이어붙인 문자열 반환
    public static String joinString(LinkedList<String> strList, String delimiter) {
        // String.join() 메소드는 Java 8부터 지원
        String str = String.join(delimiter, strList);
        return str;
    }

    // 문자열 배열을 정렬해서 반환
    public static String[] sortArray(String[] strArr, boolean ascending) {
        Arrays.sort(strArr, new Comparator<String>() {
            @Override
            public int compare(String str1, String str2) {
                // 오름차순 정렬
                if (ascending) {
                    return str1.compareTo(str2);
                }
                // 내림차순 정렬
                return str2.compareTo(str1);
            }
        });
        return strArr;
    }

    // 문자열 리스트를 정렬해서 반환
    public static LinkedList<String> sortLinkedList(LinkedList<String> strList, boolean ascending) {
        Collections.sort(strList, new Comparator<String>() {
            @Override
            public int compare(String str1, String str2) {
                // 오름차순 정렬
                if (ascending) {
                    return str1.compareTo(str2);
                }
                // 내림차순 정렬
                return str2.compareTo(str1);
            }
        });
        return strList;
    }

    // 문자열 배열을 문자열 리스트로 변환
    public static LinkedList<String> convertStringArrayToLinkedList(String[] strArr) {
        LinkedList<String> strList = new LinkedList<String>();
        for (String s : strArr) {
            strList.add(s);
        }
        return strList;
    }

    // 문자열 리스트를 문자열 배열로 반환
    public static String[] convertStringListToStringArray(LinkedList<String> strList) {
        String[] strArr = new String[strList.size()];
        strArr = strList.toArray(strArr);
        return strArr;
    }
}
