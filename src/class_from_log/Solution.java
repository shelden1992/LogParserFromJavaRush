package class_from_log;

import java.nio.file.Paths;

public class Solution {


    public static void main(String[] args) {
        LogParser logParser = new LogParser(Paths.get("/Users/macuser/Desktop/projects/logParser/src/class_from_log/exempleLog/first.log"));
        System.out.println(logParser.execute("get status"));
        System.out.println(logParser.execute("get ip"));

    }
}
