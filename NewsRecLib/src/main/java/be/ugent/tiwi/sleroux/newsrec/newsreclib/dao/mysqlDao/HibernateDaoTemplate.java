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
package be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.mysqlDao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public abstract class HibernateDaoTemplate {

    private static final SessionFactory sessionfactory = new Configuration().configure().buildSessionFactory();
    private Session session;
    private Transaction transaction;

    

    protected Session getSession() {
        if (session == null) {
            session = sessionfactory.openSession();
            transaction = session.beginTransaction();
        }
        return session;
    }

    protected void closeSession() {
        if (session != null) {
            transaction.commit();
            session.close();
            session = null;
        }
    }
}
