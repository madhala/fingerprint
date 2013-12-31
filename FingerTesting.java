/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import com.nitgen.SDK.BSP.NBioBSPJNI;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author hanuman
 */
public class FingerTesting {

    /**
     * @param args the command line arguments
     */
    static NBioBSPJNI bsp;
    static NBioBSPJNI.Export exportEngine;
    static NBioBSPJNI.Export.TEMPLATE_DATA SaveWSQData;
    static NBioBSPJNI.FIR_HANDLE hWSQFIR1;
    static NBioBSPJNI.FIR_HANDLE hWSQFIR2;
    static byte[] writebuffer;
    static FileInputStream fis;
    static BufferedOutputStream bos;
    static PreparedStatement pre;
    public static Boolean bResult;
    public static int flag=0;
    public FingerTesting() {



        // TODO code application logic here
      bResult=new Boolean(true);
        bsp = new NBioBSPJNI();
        if (CheckError()) {
            return;
        }
        exportEngine = bsp.new Export();
        if (CheckError()) {
            return;
        }

        //setTitle("NBioAPI_WSQDemo BSP version: " + bsp.GetVersion());

      //  bsp.OpenDevice();

        if (CheckError()) {
            return;
        }

    }

    public void dispose() {
        if (hWSQFIR1 != null) {
            hWSQFIR1.dispose();
            hWSQFIR1 = null;
        }

        if (hWSQFIR2 != null) {
            hWSQFIR2.dispose();
            hWSQFIR2 = null;
        }

        if (bsp != null) {
            bsp.CloseDevice();
            bsp.dispose();
            bsp = null;
        }
    }

    public void Closing() {
        dispose();
    }

    private static Boolean CheckError() {
        if (bsp.IsErrorOccured()) {
            javax.swing.JOptionPane.showMessageDialog(null, "NBioBSP Error Occured [" + bsp.GetErrorCode() + "]");
            return true;
        }

        return false;
    }

    private static void saveDatabaseFileToFileSystem(InputStream bis) {
        try {
            //now = Calendar.getInstance();
            bos = new BufferedOutputStream(new FileOutputStream("D:\\sample\\27.wsq"));
            writebuffer = new byte[100000];
            int read = 0;
            while ((read = bis.read(writebuffer)) > 0) {
                bos.write(writebuffer, 0, read);
            }
            bos.flush();
            // saveToDataBase(bis);
            //count++;
            System.out.println("database file transfer success");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {

                bos.close();
                //bis.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }



    }

    public static void main(String[] args) {
        // TODO code application logic here

      new FingerTesting();
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Where is your MySQL JDBC Driver?");
            e.printStackTrace();
        }

        Connection con = null;

        try {
            con = DriverManager
                    .getConnection("jdbc:mysql://localhost:3306/mysql", "root", "root");

        } catch (SQLException e) {
            System.out.println("Connection Failed! Check output console");
            e.printStackTrace();
        }

        //Statements allow to issue SQL queries to the database

        try {
            Statement statement = con.createStatement();
            // Result set get the result of the SQL query
            ResultSet resultSet = statement.executeQuery("select * from reg");

            while (resultSet.next()) {  //retrieve data

                try {
                    Blob b = resultSet.getBlob(5);
                    InputStream inputstream = b.getBinaryStream();
                    saveDatabaseFileToFileSystem(inputstream);
                    byte[] loadData;
                    int nLoadLen = 0;
                    byte[] loadData1;
                    int nLoadLen1 = 0;

                    //   try
                    //  {
                    File f1 = new File("D:\\sample\\27.wsq");
                    FileInputStream fis = new FileInputStream(f1);
                    long nFileLen = f1.length();
                    loadData = new byte[(int) nFileLen];
                    nLoadLen = fis.read(loadData);
                    File f2 = new File("D:\\sample\\1.wsq");
                    FileInputStream fis1 = new FileInputStream(f2);
                    long nFileLen1 = f2.length();
                    loadData1 = new byte[(int) nFileLen1];
                    nLoadLen1 = fis1.read(loadData1);
                    if (nLoadLen > 0 && nLoadLen1 > 0) {

                        NBioBSPJNI.Export.AUDIT exportAudit = exportEngine.new AUDIT();
                        NBioBSPJNI.FIR_HANDLE hAudtiFIR;

                        exportEngine.ConvertWsqToRaw(loadData, nLoadLen, exportAudit);
                        NBioBSPJNI.Export.AUDIT exportAudit1 = exportEngine.new AUDIT();
                        NBioBSPJNI.FIR_HANDLE hAudtiFIR1;

                        exportEngine.ConvertWsqToRaw(loadData1, nLoadLen1, exportAudit1);
                        // System.out.println("ok");
                        f1.deleteOnExit();
                        // System.out.println("ok");  
                        if (CheckError()) {
                            return;
                        }

                        // if (hWSQFIR1 != null)  {
                        // hWSQFIR1.dispose();
                        //hWSQFIR1 = null;
                        //  }

                        hAudtiFIR = bsp.new FIR_HANDLE();

                        exportEngine.ImportAudit(exportAudit, hAudtiFIR);

                        hAudtiFIR1 = bsp.new FIR_HANDLE();

                        exportEngine.ImportAudit(exportAudit1, hAudtiFIR1);
                        if (CheckError()) {
                            return;
                        }

                        NBioBSPJNI.INPUT_FIR inputFIR = bsp.new INPUT_FIR();

                        inputFIR.SetFIRHandle(hAudtiFIR);
                        hWSQFIR2 = bsp.new FIR_HANDLE();

                        bsp.Process(inputFIR, hWSQFIR2);
                        NBioBSPJNI.INPUT_FIR inputFIR1 = bsp.new INPUT_FIR();

                        inputFIR1.SetFIRHandle(hAudtiFIR1);
                        hWSQFIR1 = bsp.new FIR_HANDLE();
                        bsp.Process(inputFIR1,hWSQFIR1);
                        if (CheckError()) {
                            return;
                        }
                        if (hWSQFIR2 != null && hWSQFIR1 != null) {
                            NBioBSPJNI.INPUT_FIR inputFIR11 = bsp.new INPUT_FIR();
                            NBioBSPJNI.INPUT_FIR inputFIR22 = bsp.new INPUT_FIR();
                            inputFIR11.SetFIRHandle(hWSQFIR1);
                            inputFIR22.SetFIRHandle(hWSQFIR2);
                            // System.out.println("kkkkkk");  
                            bsp.VerifyMatch(inputFIR22, inputFIR11, bResult, null);
                            // System.out.println("kkkkkk");
                            if (CheckError()) {
                                return;
                            }
                            //  System.out.println("kkkkkk");
                            if (exportAudit != null) {
                                exportAudit = null;
                                inputFIR11 = null;
                                inputFIR22 = null;
                                loadData = null;
                                nLoadLen = 0;
                                hAudtiFIR = null;
                                loadData1 = null;
                                nLoadLen1 = 0;
                                hAudtiFIR1 = null;
                                fis.close();
                                fis1.close();
                                //System.gc();
                            }

                            if (bResult) {
                                // System.out.println("matched...");
                                flag=1;
                                javax.swing.JOptionPane.showMessageDialog(null, "Verify OK");
                                return;

                            } else // javax.swing.JOptionPane.showMessageDialog(null, "Verify Failed");
                            {

                                flag=0;
                                //System.out.println("failed....");
                                if (hWSQFIR2 != null) {
                                    hWSQFIR2.dispose();
                                    hWSQFIR2 = null;
                                }

                            }
                            //    btnMatch.setEnabled(true);
                            //    }

                            //   javax.swing.JOptionPane.showMessageDialog(null, "Make FIR Handle Success");
                        }
                    }
              
                
                } catch (IOException e) {
                }



            } if(flag==0){ javax.swing.JOptionPane.showMessageDialog(null, "Verify Failed");}
        } catch (SQLException e) {
        }
    }
}
