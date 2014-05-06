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
package be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.mysqlImpl;

import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.DaoException;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public abstract class HibernateDaoTemplate {

    private static SessionFactory sessionfactory = null;
    protected Session session = null;
    protected Transaction transaction = null;

    public HibernateDaoTemplate() {
        if (sessionfactory == null) {
            sessionfactory = new Configuration().configure().buildSessionFactory();
        }
    }

    public void startSession() throws DaoException {
        try {
            session = sessionfactory.openSession();
            transaction = session.beginTransaction();
        } catch (HibernateException ex) {
            throw new DaoException();
        }

    }

    public void stopSession() throws DaoException {

        try {
            if (transaction != null) {
                transaction.commit();
            }
        } catch (HibernateException ex) {
            throw new DaoException();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

}
