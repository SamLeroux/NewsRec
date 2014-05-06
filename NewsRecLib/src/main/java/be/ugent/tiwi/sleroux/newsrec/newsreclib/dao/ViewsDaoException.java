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
package be.ugent.tiwi.sleroux.newsrec.newsreclib.dao;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class ViewsDaoException extends DaoException {

    public ViewsDaoException() {
    }

    public ViewsDaoException(String message) {
        super(message);
    }

    public ViewsDaoException(String message, Throwable cause) {
        super(message, cause);
    }

    public ViewsDaoException(Throwable cause) {
        super(cause);
    }

    public ViewsDaoException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
