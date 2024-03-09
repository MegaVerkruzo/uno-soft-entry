import org.example.Main;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class BackendTest {
    private static final String INPUT_FOLDER = "input";
    private static final String CORRECT_OUTPUT_FOLDER = "correctOutput";
    private static final String TEST_FILE_1 = "1.txt";
    private static final String TEST_FILE_2 = "2.txt";
    private static final String TEST_FILE_3 = "3.txt";
    private static final String TEST_FILE_4 = "4.txt";
    private static final String TEST_FILE_5 = "5.txt";
    private static final String OUTPUT_FILE = "output.txt";

    @AfterAll
    public static void afterAll() throws IOException {
        Files.deleteIfExists(Paths.get(OUTPUT_FILE));
    }

    @Test
    public void checkSimpleTest() throws IOException {
        checkTest(TEST_FILE_1);
    }

    @Test
    public void checkEmptyTest() throws IOException {
        checkTest(TEST_FILE_2);
    }

    @Test
    public void checkOneRowTest() throws IOException {
        checkTest(TEST_FILE_3);
    }

    @Test
    public void checkTwoSingleRowTest() throws IOException {
        checkTest(TEST_FILE_4);
    }

    @Test
    public void checkTwoTogetherRowTest() throws IOException {
        checkTest(TEST_FILE_5);
    }



    private void checkTest(String fileName) throws IOException {
        Main.main(new String[]{INPUT_FOLDER + "/" + fileName});
        Assertions.assertEquals(
                -1,
                Files.mismatch(
                        Paths.get(OUTPUT_FILE),
                        Paths.get(CORRECT_OUTPUT_FOLDER, fileName)
                )
        );
    }
}
