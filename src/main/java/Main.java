import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Scanner;

class Quote {
    int id;
    String content;
    String author;

    Quote(int id, String content, String author) {
        this.id = id;
        this.content = content;
        this.author = author;
    }
}

public class Main {
    public static void main(String[] args) {
        System.out.println("== 명언 앱 ==");

        Scanner sc = new Scanner(System.in);

        File dbDir = new File("db/wiseSaying");
        if (!dbDir.exists()) {
            dbDir.mkdirs();
        }

        int lastId = 0;
        File lastIdFile = new File(dbDir + "/lastId.txt");
        if (lastIdFile.exists()) {
            try {
                String lastIdStr = Files.readString(lastIdFile.toPath());
                lastId = Integer.parseInt(lastIdStr.trim());
            } catch (IOException | NumberFormatException e) {
                lastId = 0;
            }
        }

        while (true) {
            System.out.print("명령) ");
            String cmd = sc.nextLine().trim();

            if (cmd.equals("종료")) {
                break;
            }

            if (cmd.equals("등록")) {
                lastId++;
                int id = lastId;

                System.out.print("명언) ");
                String content = sc.nextLine().trim();

                System.out.print("작가) ");
                String author = sc.nextLine().trim();

                File quoteFile = new File(dbDir + "/" + id + ".json");
                try (FileWriter writer = new FileWriter(quoteFile)) {
                    writer.write("{\n");
                    writer.write("  \"id\": " + id + ",\n");
                    writer.write("  \"content\": \"" + content + "\",\n");
                    writer.write("  \"author\": \"" + author + "\"\n");
                    writer.write("}");
                } catch (IOException e) {
                    System.out.println("파일 저장 실패");
                }

                try (FileWriter writer = new FileWriter(lastIdFile)) {
                    writer.write(String.valueOf(lastId));
                } catch (IOException e) {

                }

                System.out.println(id + "번 명언이 등록되었습니다.");
            }

            else if (cmd.equals("목록")) {
                System.out.println("번호 / 작가 / 명언");
                System.out.println("-----------------");

                File[] files = dbDir.listFiles((dir, name) -> name.endsWith(".json"));
                if (files == null || files.length == 0) {
                    System.out.println("등록된 명언이 없습니다.");
                } else {
                    ArrayList<Quote> quotes = new ArrayList<>();
                    for (File file : files) {
                        try {
                            String jsonStr = Files.readString(file.toPath());
                            int id = extractInt(jsonStr, "\"id\":");
                            String content = extractString(jsonStr, "\"content\":");
                            String author = extractString(jsonStr, "\"author\":");
                            quotes.add(new Quote(id, content, author));
                        } catch (IOException e) {

                        }
                    }

                    for (int i = quotes.size() - 1; i >= 0; i--) {
                        Quote q = quotes.get(i);
                        System.out.println(q.id + " / " + q.author + " / " + q.content);
                    }
                }
            }

            else if (cmd.startsWith("삭제?id=")) {
                try {
                    String[] parts = cmd.split("=");
                    int targetId = Integer.parseInt(parts[1]);

                    File file = new File(dbDir + "/" + targetId + ".json");
                    if (!file.exists()) {
                        System.out.println(targetId + "번 명언은 존재하지 않습니다.");
                    } else {
                        file.delete();
                        System.out.println(targetId + "번 명언이 삭제되었습니다.");
                    }

                } catch (NumberFormatException e) {
                    System.out.println("올바른 번호를 입력해주세요.");
                }
            }

            else if (cmd.startsWith("수정?id=")) {
                try {
                    String[] parts = cmd.split("=");
                    int targetId = Integer.parseInt(parts[1]);

                    File file = new File(dbDir + "/" + targetId + ".json");
                    if (!file.exists()) {
                        System.out.println(targetId + "번 명언은 존재하지 않습니다.");
                    } else {
                        String jsonStr = Files.readString(file.toPath());
                        int id = extractInt(jsonStr, "\"id\":");
                        String oldContent = extractString(jsonStr, "\"content\":");
                        String oldAuthor = extractString(jsonStr, "\"author\":");

                        System.out.println("명언(기존) : " + oldContent);
                        System.out.print("명언) ");
                        String newContent = sc.nextLine().trim();

                        System.out.println("작가(기존) : " + oldAuthor);
                        System.out.print("작가) ");
                        String newAuthor = sc.nextLine().trim();

                        try (FileWriter writer = new FileWriter(file)) {
                            writer.write("{\n");
                            writer.write("  \"id\": " + id + ",\n");
                            writer.write("  \"content\": \"" + newContent + "\",\n");
                            writer.write("  \"author\": \"" + newAuthor + "\"\n");
                            writer.write("}");
                        }

                        System.out.println(targetId + "번 명언이 수정되었습니다.");
                    }

                } catch (IOException | NumberFormatException e) {
                    System.out.println("올바른 번호를 입력해주세요.");
                }
            }

            else if (cmd.equals("빌드")) {
                File[] files = dbDir.listFiles((dir, name) -> name.endsWith(".json"));
                if (files != null) {
                    ArrayList<Quote> quotes = new ArrayList<>();
                    for (File file : files) {
                        try {
                            String jsonStr = Files.readString(file.toPath());
                            int id = extractInt(jsonStr, "\"id\":");
                            String content = extractString(jsonStr, "\"content\":");
                            String author = extractString(jsonStr, "\"author\":");
                            quotes.add(new Quote(id, content, author));
                        } catch (IOException e) {

                        }
                    }

                    quotes.sort((a, b) -> Integer.compare(a.id, b.id));

                    File dataFile = new File(dbDir + "/data.json");
                    try (FileWriter writer = new FileWriter(dataFile)) {
                        writer.write("[\n");
                        for (int i = 0; i < quotes.size(); i++) {
                            Quote q = quotes.get(i);
                            writer.write("  {\n");
                            writer.write("    \"id\": " + q.id + ",\n");
                            writer.write("    \"content\": \"" + q.content + "\",\n");
                            writer.write("    \"author\": \"" + q.author + "\"\n");
                            writer.write("  }");
                            if (i < quotes.size() - 1) {
                                writer.write(",");
                            }
                            writer.write("\n");
                        }
                        writer.write("]");
                        System.out.println("data.json 파일의 내용이 갱신되었습니다.");
                    } catch (IOException e) {
                        System.out.println("빌드 실패");
                    }
                }
            }
        }

        sc.close();
    }

    private static int extractInt(String json, String key) {
        int idx = json.indexOf(key);
        if (idx == -1) return 0;
        int start = idx + key.length();
        int end = json.indexOf(",", start);
        if (end == -1) end = json.indexOf("}", start);
        return Integer.parseInt(json.substring(start, end).trim());
    }

    private static String extractString(String json, String key) {
        int idx = json.indexOf(key);
        if (idx == -1) return "";
        int start = json.indexOf("\"", idx + key.length()) + 1;
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }
}