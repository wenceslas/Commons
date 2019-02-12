package lt.lb.commons.io;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import lt.lb.commons.iteration.ReadOnlyIterator;
import lt.lb.commons.parsing.CommentParser;

/**
 *
 * @author laim0nas100
 */
public class FileReader {

    public static ArrayList<String> readFromFile(String URL) throws UnsupportedEncodingException, FileNotFoundException, IOException {
        ArrayList<String> list = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(URL), "UTF-8"))) {
            reader.lines().forEach((String line) -> {
                list.add(line);
            });
        }
        return list;
    }

    public static ArrayList<String> readFromFile(String URL, String lineComment, String commentStart, String commentEnd) throws FileNotFoundException, IOException {
        ArrayList<String> list = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(URL), "UTF-8"))) {
            reader.lines().forEach((String ln) -> {
                list.add(ln);
            });
        }

        return CommentParser.parseAllComments(list, lineComment, commentStart, commentEnd);
    }

    public static ArrayList<String> readFromFile(String URL, String lineComment) throws FileNotFoundException, IOException {
        ArrayList<String> list = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(URL), "UTF-8"))) {
            reader.lines().forEach((String ln) -> {
                list.add(ln);
            });
        }
        return CommentParser.parseLineComments(list, lineComment);
    }

    public static void writeToFile(String URL, Collection<String> list) throws FileNotFoundException, UnsupportedEncodingException {
        writeToFile(URL, ReadOnlyIterator.of(list));
    }

    public static void writeToFile(String URL, ReadOnlyIterator<String> lines) throws FileNotFoundException, UnsupportedEncodingException {
        try (PrintWriter out = new PrintWriter(URL, "UTF-8");
                ReadOnlyIterator<String> ln = lines) {
            for (String line : ln) {
                out.println(line);
            }
        }
    }

}
