package com.github.blindpirate.gogradle.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class ProcessUtils {
    public static String getOutput(Process process) throws IOException, InterruptedException {
        process.waitFor();
        try (BufferedReader br =
                     new BufferedReader(
                             new InputStreamReader(process.getInputStream(), Charset.forName("UTF8")))) {
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        }
    }
}
