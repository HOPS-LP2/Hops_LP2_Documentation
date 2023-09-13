package com.example.javawebapp;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "signup", value = "/signup")
public class SignupServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String firstName = req.getParameter("first-name");
        String lastName = req.getParameter("last-name");
        String email = req.getParameter("email");
        String cpf = req.getParameter("ssn");
        String phoneNumber = req.getParameter("phone-number");
        String password = req.getParameter("password");

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        String sqlQueryInsert = "INSERT INTO User (firstName, lastName, email, cpf, phoneNumber, password) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlQueryCheckEmail = "SELECT * FROM User WHERE email = ?";
        String sqlQueryCheckCPF = "SELECT * FROM User WHERE cpf = ?";

        try {
            Context context = new InitialContext();
            DataSource dataSource = (DataSource) context.lookup("java:/comp/env/jdbc/MyDB");

            connection = dataSource.getConnection();

            preparedStatement = connection.prepareStatement(sqlQueryCheckEmail);
            preparedStatement.setString(1, email);

            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                req.getSession().setAttribute("message", "Email already in use");
                resp.sendRedirect("/java-web-app-1.0/pages/signup.jsp");
                return;
            }

            // Close the result set before reusing the preparedStatement
            resultSet.close();
            preparedStatement.close();

            preparedStatement = connection.prepareStatement(sqlQueryCheckCPF);
            preparedStatement.setString(1, cpf);

            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                req.getSession().setAttribute("message", "SSN already in use");
                resp.sendRedirect("/java-web-app-1.0/pages/signup.jsp");
                return;
            }

            // Close the result set before reusing the preparedStatement
            resultSet.close();
            preparedStatement.close();

            preparedStatement = connection.prepareStatement(sqlQueryInsert);

            preparedStatement.setString(1, firstName);
            preparedStatement.setString(2, lastName);
            preparedStatement.setString(3, email);
            preparedStatement.setString(4, cpf);
            preparedStatement.setString(5, phoneNumber);
            preparedStatement.setString(6, password);

            // Execute the query
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Insertion successful.");
                req.getSession().setAttribute("loginFailed", null);
                resp.sendRedirect("/java-web-app-1.0/pages/login.jsp");
            } else {
                System.out.println("Insertion failed.");
            }
        } catch (SQLException | NamingException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
