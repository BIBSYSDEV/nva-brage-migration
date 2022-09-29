package no.sikt.nva;

import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {

        UnZipper unZipper = new UnZipper("src/main/java/resources/2637176.zip",
                                         new File("src/main/java/resources/destination"));
        unZipper.unzip();
        System.out.println("Unzipping completed");
    }
}