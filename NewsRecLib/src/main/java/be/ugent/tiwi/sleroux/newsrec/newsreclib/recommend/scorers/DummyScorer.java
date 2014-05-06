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
package be.ugent.tiwi.sleroux.newsrec.newsreclib.recommend.scorers;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class DummyScorer implements IScorer {

    @Override
    public void score(long user, String item, double rating) {
    }

    @Override
    public void view(long user, String item) {
    }

    @Override
    public void viewUrl(long user, String url) {
    }

}
