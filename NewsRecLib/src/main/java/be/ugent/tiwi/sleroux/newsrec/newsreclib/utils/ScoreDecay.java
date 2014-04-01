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
package be.ugent.tiwi.sleroux.newsrec.newsreclib.utils;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class ScoreDecay {

    private double a = 1.1;
    private double b = 1.0;
    private double m = 9e-10;

    public ScoreDecay() {
    }

    public ScoreDecay(double a, double b, double m) {
        this.a = a;
        this.b = b;
        this.m = m;
    }

    public double getA() {
        return a;
    }

    public void setA(double a) {
        this.a = a;
    }

    public double getB() {
        return b;
    }

    public void setB(double b) {
        this.b = b;
    }

    public double getM() {
        return m;
    }

    public void setM(double m) {
        this.m = m;
    }
    
    public double getBoost(long ageMs){
        return a / (ageMs * m + b);
    }
    
    
}
