package test;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;

public class MyFile {

    // 현재 경로(path) 반환
    public static String getCurrentDirectoryFullPath() {
        String currentPath = Paths.get("").toAbsolutePath().toString();
        return currentPath;
    }

    // 주어진 경로(path)의 상위 경로 반환
    public static String getParentDirectoryFullPath(String directoryFullPath) {
        File directory = new File(directoryFullPath);
        return directory.getParent();
    }

    // 주어진 경로(path) 하위의 모든 파일 이름 반환
    public static List<String> getFileNames(String directoryFullPath) {
        List<String> fileNames = new ArrayList<String>();
        File directory = new File(directoryFullPath);
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                fileNames.add(file.getName());
            }
        }
        return fileNames;
    }

    // 주어진 경로(path)에 해당하는 파일이 존재하는지 여부 반환
    public static boolean checkFileExists(String fileFullPath) {
        File file = new File(fileFullPath);
        return file.exists() && file.isFile();
    }

    // 주어진 경로(path)에 해당하는 디렉토리가 존재하는지 여부 반환
    public static boolean checkDirectoryExists(String directoryFullPath) {
        File directory = new File(directoryFullPath);
        return directory.exists() && directory.isDirectory();
    }

    // 주어진 경로(path)에 해당하는 파일 또는 디렉토리가 존재하는지 여부 반환
    public static boolean checkFileOrDirectoryExists(String fileOrDirectoryFullPath) {
        File fileOrDirectory = new File(fileOrDirectoryFullPath);
        return fileOrDirectory.exists();
    }

    // 주어진 경로(path)가 없으면 디렉토리 생성 (하위 디렉토리 포함 여부 선택 가능)
    public static void createDirectory(String directoryFullPath, boolean includeSubdirectories) {
        // 디렉토리 객체 생성
        File directory = new File(directoryFullPath);
        // 디렉토리가 존재하지 않는 경우에만 디렉토리 생성
        if (!directory.exists()) {
            // 디렉토리 생성
            directory.mkdirs();
            // 하위 디렉토리 포함 여부가 true인 경우에만 하위 디렉토리 생성
            if (includeSubdirectories) {
                // 디렉토리의 상위 디렉토리 경로를 가져옴
                String parentDirectoryPath = directory.getParent();
                // 상위 디렉토리가 존재하지 않는 경우에만 상위 디렉토리 생성
                if (!checkDirectoryExists(parentDirectoryPath)) {
                    createDirectory(parentDirectoryPath, true);
                }
            }
        }
    }

    // 주어진 경로에 파일 생성
    public static void createFile(String fileFullPath) {
        // 파일 객체 생성
        File file = new File(fileFullPath);
        // 파일이 존재하지 않는 경우에만 파일 생성
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 주어진 경로에 파일 삭제
    public static void deleteFile(String fileFullPath) {
        // 파일 객체 생성
        File file = new File(fileFullPath);
        // 파일이 존재하는 경우에만 파일 삭제
        if (file.exists()) {
            file.delete();
        }
    }

    // 특정 경로에서 시작 키워드와 종료 키워드에 맞는 파일의 절대 경로 목록을 찾아서 String 배열로 반환 (하위 디렉토리 포함 여부 선택 가능)
    public static String[] findFiles(String path, String startKeyword, String endKeyword, boolean includeSubdirectories) {
        // ArrayList 객체를 사용해서 파일 경로 저장
        List<String> fileList = new ArrayList<>();
        // 디렉토리 객체 생성
        File directory = new File(path);
        // 디렉토리가 존재하고 디렉토리인 경우에만 파일 목록 읽기
        if (directory.exists() && directory.isDirectory()) {
            // 디렉토리의 파일 목록을 읽어서 파일 경로 목록에 추가
            File[] files = directory.listFiles();
            // 파일 목록이 null이 아닌 경우에만 파일 경로 목록에 추가
            if (files != null) {
                // 파일 목록을 순회하면서 파일 경로 목록에 추가
                for (File file : files) {
                    // 파일이고 시작 키워드와 종료 키워드에 맞는 경우에만 파일 경로 목록에 추가
                    if (file.isFile() && file.getName().startsWith(startKeyword) && file.getName().endsWith(endKeyword)) {
                        fileList.add(file.getAbsolutePath());
                        // 하위 디렉토리 포함 여부가 true인 경우에만 하위 디렉토리의 파일 경로 목록을 추가
                    } else if (includeSubdirectories && file.isDirectory()) {
                        // 재귀 호출을 사용해서 하위 디렉토리의 파일 경로 목록을 추가
                        String subdirectoryPath = file.getAbsolutePath();
                        // 하위 디렉토리의 파일 경로 목록을 재귀 호출을 사용해서 추가
                        String[] subdirectoryFiles = findFiles(subdirectoryPath, startKeyword, endKeyword, true);
                        // 하위 디렉토리의 파일 경로 목록을 파일 경로 목록에 추가
                        fileList.addAll(Arrays.asList(subdirectoryFiles));
                    }
                }
            }
        }
        // ArrayList 객체를 String 배열로 변환해서 반환
        return fileList.toArray(new String[0]);
    }

    // 특정 경로에서 시작 키워드와 종료 키워드에 맞는 파일의 절대 경로 목록을 찾아서 파일들 삭제
    public static void deleteFiles(String path, String startKeyword, String endKeyword) {
        // 파일 경로 목록을 찾아서 String 배열로 반환
        String[] files = findFiles(path, startKeyword, endKeyword, true);
        // 파일 경로 목록을 순회하면서 파일 삭제
        for (String file : files) {
            deleteFile(file);
        }
    }

    // 파일 내용을 읽어서 String 객체로 반환
    public static String readFileContent(File file) throws IOException {
        // StringBuilder 객체를 사용해서 파일 내용 저장
        StringBuilder contentBuilder = new StringBuilder();
        // 파일이 존재하고 파일인 경우에만 파일 내용 읽기
        if (file.exists() && file.isFile()) {
            // BufferedReader 객체를 사용해서 파일 내용 읽기
            // try-with-resources 구문 사용 (Java 7 이상)
            // try 블록이 끝나면 자동으로 close() 메소드가 호출됨
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                // 파일 내용을 모두 읽어서 StringBuilder 객체에 저장
                while ((line = reader.readLine()) != null) {
                    // StringBuilder 객체에 파일 내용 저장
                    contentBuilder.append(line).append(System.lineSeparator());
                }
            }
        }
        // StringBuilder 객체를 String 객체로 변환해서 반환
        return contentBuilder.toString();
    }

    // 파일 내용을 모두 읽어서 단일 String 객체로 반환
    public static String readFileContent(String fileFullPath) {
        String content = "";
        // 파일 객체 생성
        File file = new File(fileFullPath);

        try {
            // 파일 내용을 모두 읽어서 String 객체로 반환
            content = readFileContent(file);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return content;
    }

    // 파일 내용을 모두 읽어서 String 배열로 반환
    public static String[] readFileContentToArray(String fileFullPath) {
        // ArrayList 객체를 사용해서 파일 내용 저장
        List<String> lines = new ArrayList<>();
        // 파일 객체 생성
        File file = new File(fileFullPath);
        // 파일이 존재하고 파일인 경우에만 파일 내용 읽기
        if (file.exists() && file.isFile()) {
            // try-with-resources 구문 사용 (Java 7 이상)
            // try 블록이 끝나면 자동으로 close() 메소드가 호출됨
            // BufferedReader 객체를 사용해서 파일 내용 읽기
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
            } catch (IOException e) {
                // 예외 발생 시 스택 트레이스 출력
                e.printStackTrace();
            }
        }
        // ArrayList 객체를 String 배열로 변환해서 반환
        return lines.toArray(new String[0]);
    }

    // 파일에 문자열 입력
    // position: "prepend" - 파일의 맨 앞에 입력
    //           "overwrite" - 파일의 내용을 모두 지우고 입력
    //           "append" - 파일의 맨 뒤에 입력
    public static boolean writeToFile(String path, String content, String position) {

        // 파일의 맨 앞에 입력하는 경우 기존 내용을 읽어서 변수에 담기
        String existingContent = "";
        if (position.equals("prepend")) {
            try {
                existingContent = readFileContent(new File(path));
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        // try-with-resources 구문 사용 (Java 7 이상)
        // try 블록이 끝나면 자동으로 close() 메소드가 호출됨
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path, position.equals("append")))) {
            if (position.equals("prepend")) {
                writer.write(content);
                writer.newLine();
                writer.write(existingContent);
            } else if (position.equals("overwrite")) {
                writer.write(content);
                writer.newLine();
            } else {
                writer.write(content);
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            System.out.println("파일에 문자열을 입력하는 도중 오류가 발생했습니다: " + e.getMessage());
            return false;
        }
    }

}
