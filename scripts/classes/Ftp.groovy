@Grab(group='commons-net', module='commons-net', version='3.4')
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTP;

import java.nio.charset.StandardCharsets;

public class Ftp {

  static String FTP_SERVER     = System.getProperty("FTP_SERVER");
  static String FTP_USER       = System.getProperty("FTP_USER");
  static String FTP_PASSWORD   = System.getProperty("FTP_PASSWORD");
  
  static FTPClient ftpClient;


  public static void connect() {
    ftpClient = new FTPClient();
    ftpClient.connect(FTP_SERVER);
    ftpClient.login(FTP_USER, FTP_PASSWORD);
    String replyString = ftpClient.getReplyString();
    
    if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
      ftpClient.disconnect();
      System.out.println("ERROR!  Failed to connect to " + FTP_SERVER + "; " + replyString);
      System.exit(1);
    }
    
    System.out.println("CONNECTED to " + FTP_SERVER + "; " + replyString);
  }
  
  
  public static void disconnect() {
    ftpClient.logout();
    ftpClient.disconnect();
    System.out.println("DISCONNECTED from " + FTP_SERVER);
  }


  public static void mkdir(String remoteDir) {
    System.out.println("CREATING remote directory: " + remoteDir);
    boolean created = ftpClient.makeDirectory(remoteDir);
    if (created) {
      System.out.println("CREATED the directory: " + remoteDir);
    } else {
      System.out.println("COULD NOT create the directory: " + remoteDir + " " + ftpClient.getReplyString());
    }
  }


  public static void uploadDirectory( String remoteDirPath,
                                      String localParentDir,
                                      String remoteParentDir) throws IOException {
   
      File localDir = new File(localParentDir);
      File[] subFiles = localDir.listFiles();
      if (subFiles != null && subFiles.length > 0) {
          for (File item : subFiles) {
              String remoteFilePath = remoteDirPath + "/" + remoteParentDir + "/" + item.getName();
              if (remoteParentDir.equals("")) {
                  remoteFilePath = remoteDirPath + "/" + item.getName();
              }
   
              if (item.isFile()) {
                  // upload the file
                  String localFilePath = item.getAbsolutePath();
                  boolean uploaded = uploadSingleFile(localFilePath, remoteFilePath);
                  if (uploaded) {
                      System.out.println("UPLOADED a file to: " + remoteFilePath);
                  } else {
                      System.out.println("COULD NOT upload the file: " + localFilePath + " " + ftpClient.getReplyString());
                  }
              } else {
                  // create directory on the server
                  boolean created = ftpClient.makeDirectory(remoteFilePath);
                  if (created) {
                      System.out.println("CREATED the directory: " + remoteFilePath);
                  } else {
                      System.out.println("COULD NOT create the directory: " + remoteFilePath + " " + ftpClient.getReplyString());
                  }
   
                  // upload the sub directory
                  String parent = remoteParentDir + "/" + item.getName();
                  if (remoteParentDir.equals("")) {
                      parent = item.getName();
                  }
   
                  localParentDir = item.getAbsolutePath();
                  uploadDirectory(remoteDirPath, localParentDir, parent);
              }
          }
      }
  }


  public static boolean uploadSingleFile( String localFilePath, String remoteFilePath) throws IOException {
      File localFile = new File(localFilePath);
   
      InputStream inputStream = new FileInputStream(localFile);
      try {
          ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
          return ftpClient.storeFile(remoteFilePath, inputStream);
      } finally {
          inputStream.close();
      }
  }
  
  
  public static boolean uploadFileContents( String fileContents, String remoteFilePath ) throws IOException {
      InputStream inputStream = new ByteArrayInputStream(fileContents.getBytes(StandardCharsets.UTF_8));
      
      try {
          ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
          return ftpClient.storeFile(remoteFilePath, inputStream);
      } finally {
          inputStream.close();
      }
  }

}