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

import be.ugent.tiwi.sleroux.newsrec.newsreclib.recommend.scorers.IScorer;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class ViewedServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    private static final Logger logger = Logger.getLogger(ViewedServlet.class);

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        try {
            Map<String, String> params = request.getParameterMap();

            long user = (Long) request.getSession().getAttribute("userId");
            if (params.containsKey("itemId")) {
                String itemId = request.getParameterValues("itemId")[0];
                IScorer scorer = (IScorer) getServletContext().getAttribute("scorer");
                scorer.view(user, itemId);

                String recommendedBy = request.getParameterValues("recommendedBy")[0];
                if (recommendedBy != null) {
                    TestingLogWriter w = (TestingLogWriter) getServletContext().getAttribute("testlogger");
                    w.write(user, recommendedBy, itemId);
                }

            } else if (params.containsKey("url")) {
                String url = request.getParameterValues("url")[0];
                IScorer scorer = (IScorer) getServletContext().getAttribute("scorer");
                scorer.viewUrl(user, url);

            }

            out.write("{\"response\":\"OK\"}");
            logger.debug("recorded view");

        } catch (NumberFormatException ex) {
            logger.error(ex);
            out.write("{\"exception\":\"" + ex.getMessage() + "\"}");
        } finally {
            out.close();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
