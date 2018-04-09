package edu.uco.cmagueyal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

@WebServlet(name = "ImageServlet", urlPatterns = {"/studentFolder/ImageServlet", "/advisorFolder/ImageServlet"})
public class ImageServlet extends HttpServlet {

    @Resource(name = "jdbc/ds_wsp")
    private DataSource ds;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int fileID = Integer.parseInt(request.getParameter("fileid"));
        String inLineParam = request.getParameter("inline");
        boolean inLine = false;
        if (inLineParam != null && inLineParam.equals("true")) {
            inLine = true;
        }

        try {
            Connection conn = ds.getConnection();
            PreparedStatement selectQuery = conn.prepareStatement(
                    "SELECT * FROM FILESTORAGE WHERE FILE_ID=?");
            selectQuery.setInt(1, fileID);

            ResultSet result = selectQuery.executeQuery();
            if (!result.next()) {
                System.out.println("***** SELECT query failed for ImageServlet");
            }

            String fileType = result.getString("FILE_TYPE");
            String fileName = result.getString("FILE_NAME");
            long fileSize = result.getLong("FILE_SIZE");
            Blob fileBlob = result.getBlob("FILE_CONTENTS");

            response.setContentType(fileType);
            if (inLine) {
                response.setHeader("Content-Disposition", "inline; filename=\""
                        + fileName + "\"");
            } else {
                response.setHeader("Content-Disposition", "attachment; filename=\""
                        + fileName + "\"");
            }

            final int BYTES = 1024;
            int length = 0;
            InputStream in = fileBlob.getBinaryStream();
            OutputStream out = response.getOutputStream();
            byte[] bbuf = new byte[BYTES];

            while ((in != null) && ((length = in.read(bbuf)) != -1)) {
                out.write(bbuf, 0, length);
            }

            out.flush();
            out.close();
            conn.close();

        } catch (SQLException e) {

        }
    }

}
