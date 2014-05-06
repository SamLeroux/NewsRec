/*
 * Copyright 2014 Sam Leroux <sam.leroux@ugent.be>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package be.ugent.tiwi.sleroux.newsrec.webnewsrecommender;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class TestingLogWriter {

    private final Writer outputWriter;

    public TestingLogWriter(String filename) throws IOException {
        File f = new File(filename);
        outputWriter = new FileWriter(f, true);
    }

    public void write(long userId, String recommendedBy, String articleId) {
        try {
            outputWriter.write(System.currentTimeMillis() + ";" + userId + ";" + recommendedBy + ";" + articleId + "\n");
            outputWriter.flush();
        } catch (IOException ex) {
            System.out.println(System.currentTimeMillis() + ";" + userId + ";" + recommendedBy + ";" + articleId);
        }
    }

    public void close() {
        try {
            outputWriter.close();
        } catch (IOException ex) {
        }
    }

}
