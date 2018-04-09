package edu.uco.cmagueyal;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.security.Principal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.inject.Named;
import javax.servlet.http.Part;
import javax.sql.DataSource;

@Named
@SessionScoped
public class FileBean implements Serializable {

    private String username;
    private Part part;
    private List<FileInfo> list;

    @Resource(name = "jdbc/ds_wsp")
    private DataSource ds;

    @PostConstruct
    public void init() {
        try {
            list = loadFileList();
        } catch (SQLException ex) {
            Logger.getLogger(FileBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        FacesContext fc = FacesContext.getCurrentInstance();
        Principal p = fc.getExternalContext().getUserPrincipal();
        username = p.getName();
    }

    public List<FileInfo> getList() throws SQLException {
        return list;
    }

    public void updateList() throws SQLException {
        try {
            list = loadFileList();
        } catch (SQLException ex) {
            Logger.getLogger(FileBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String updateList(String student) throws SQLException {
        try {
            loadFileList(student);
        } catch (SQLException ex) {
            Logger.getLogger(FileBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private List<FileInfo> loadFileList() throws SQLException {
        List<FileInfo> files = new ArrayList<>();
        Connection conn = ds.getConnection();
        try {
            Statement stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery(
                    "SELECT FILE_ID, FILE_NAME, FILE_TYPE, FILE_SIZE, USERNAME FROM FILESTORAGE where USERNAME = '" + username + "' and FILE_NAME is not null"
            );
            while (result.next()) {
                FileInfo file = new FileInfo();
                file.setId(result.getLong("FILE_ID"));
                file.setName(result.getString("FILE_NAME"));
                file.setType(result.getString("FILE_TYPE"));
                file.setSize(result.getLong("FILE_SIZE"));
                file.setUsername(result.getString("USERNAME"));
                files.add(file);
            }
        } finally {
            conn.close();
        }
        return files;
    }

    private void loadFileList(String student) throws SQLException {

        List<FileInfo> files = new ArrayList<>();
        Connection conn = ds.getConnection();
        try {
            Statement stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery(
                    "SELECT FILE_ID, FILE_NAME, FILE_TYPE, FILE_SIZE, USERNAME FROM FILESTORAGE where USERNAME = '" + student + "' and FILE_NAME is not null"
            );
            while (result.next()) {
                FileInfo file = new FileInfo();
                file.setId(result.getLong("FILE_ID"));
                file.setName(result.getString("FILE_NAME"));
                file.setType(result.getString("FILE_TYPE"));
                file.setSize(result.getLong("FILE_SIZE"));
                file.setUsername(result.getString("USERNAME"));
                files.add(file);
            }
        } finally {
            conn.close();
        }
        list = files;
    }

    public void uploadFile() throws IOException, SQLException {
        Connection conn = ds.getConnection();
        InputStream inputStream;
        inputStream = null;
        try {
            inputStream = part.getInputStream();
            PreparedStatement insertQuery = conn.prepareStatement(
                    "update FILESTORAGE set FILE_NAME=?, FILE_TYPE=?, FILE_SIZE=?, FILE_CONTENTS=?, USERNAME=? "
                    + " where USERNAME=?");
            insertQuery.setString(1, part.getSubmittedFileName());
            insertQuery.setString(2, part.getContentType());
            insertQuery.setLong(3, part.getSize());
            insertQuery.setBinaryStream(4, inputStream);
            insertQuery.setString(5, username);
            insertQuery.setString(6, username);

            int result = insertQuery.executeUpdate();

            if (result == 1) {
                Utility.infoMessage(part.getSubmittedFileName() + " uploaded successfully", "uploadForm:upload");
            } else {// if not 1, it must be an error.
                Utility.errorMessage(result + " file uploaded", "uploadForm:upload");
            }
        } catch (IOException e) {
            Utility.errorMessage("file upload failed", "uploadForm:upload");
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
        list = loadFileList();
    }

    public void validateFile(FacesContext ctx, UIComponent comp, Object value) {
        if (value == null) {
            throw new ValidatorException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "No file selected. Select a file.", null));
        }
        Part file = (Part) value;
        long size = file.getSize();
        if (size <= 0) {
            throw new ValidatorException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Selected file is empty", null));
        }
        if (size > 1024 * 1024 * 16) { // 16 MB limit
            throw new ValidatorException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "file is "
                            + size + "bytes. Size limit = 16MB)", null));
        }
    }

    public boolean hasPicture() {
        return list.size() > 0;
    }

    public Part getPart() {
        return part;
    }

    public void setPart(Part part) {
        this.part = part;
    }
}
