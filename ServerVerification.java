/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author hanuman
 */
import com.nitgen.SDK.BSP.NBioBSPJNI;
//import static fingertesting.Testing.bResult;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JButton;
import javax.swing.JFrame;

public class ServerVerification extends JFrame {

    JButton buton1;
    JButton buton2;
    NBioBSPJNI bsp;
    NBioBSPJNI.Export exportEngine;
    NBioBSPJNI.Export.TEMPLATE_DATA SaveWSQData;
    NBioBSPJNI.FIR_HANDLE hWSQFIR1;
    NBioBSPJNI.FIR_HANDLE hWSQFIR2;
    byte[] writebuffer;
    FileInputStream fis;
    BufferedOutputStream bos;
    PreparedStatement pre;

    public ServerVerification() {



        this.setTitle("Verification....");
        this.setSize(640, 480);
        this.setLayout(null);
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        bsp = new NBioBSPJNI();
        if (CheckError()) {
            return;
        }
        exportEngine = bsp.new Export();
        if (CheckError()) {
            return;
        }

        //setTitle("NBioAPI_WSQDemo BSP version: " + bsp.GetVersion());

        bsp.OpenDevice();

        if (CheckError()) {
            return;
        }

        // buton1.setEnabled(true);

        buton1 = new JButton("Fingure Capture");
        this.add(buton1);   // veya this.getContentPane().add(buton1);
        buton1.setBounds(101, 150, 150, 50);
        buton1.setVisible(true);
        buton2 = new JButton("verification..");
        this.add(buton2);
        // buton2.setVisible(false);
        // veya this.getContentPane().add(buton1);
        buton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCaptureActionPerformed(evt);
            }
        });


        buton2.setBounds(350, 150, 150, 50);
        buton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegistrationActionPerformed(evt);
            }
        });

        // String s1=javax.swing.JOptionPane.showInputDialog("enter first name::");
        // System.out.println(s1);
        // javax.swing.JOptionPane.showInputDialog("enter first name::");


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

    private Boolean CheckError() {
        if (bsp.IsErrorOccured()) {
            javax.swing.JOptionPane.showMessageDialog(null, "NBioBSP Error Occured [" + bsp.GetErrorCode() + "]");
            return true;
        }

        return false;
    }

    private void btnCaptureActionPerformed(java.awt.event.ActionEvent evt) {
        NBioBSPJNI.FIR_HANDLE hSavedFIR;
        NBioBSPJNI.FIR_HANDLE hSavedAuditFIR;

        hSavedFIR = bsp.new FIR_HANDLE();
        hSavedAuditFIR = bsp.new FIR_HANDLE();

        bsp.Capture(NBioBSPJNI.FIR_PURPOSE.VERIFY, hSavedFIR, -1, hSavedAuditFIR, null);

        if (CheckError()) {
            return;
        }

        if (hSavedAuditFIR != null) {
            NBioBSPJNI.INPUT_FIR inputFIR = bsp.new INPUT_FIR();

            inputFIR.SetFIRHandle(hSavedAuditFIR);

            NBioBSPJNI.Export.AUDIT exportAudit = exportEngine.new AUDIT();

            exportEngine.ExportAudit(inputFIR, exportAudit);

            if (CheckError()) {
                return;
            }

            float fQuality = 0.7f;

            if (SaveWSQData != null) {
                SaveWSQData = null;
            }

            SaveWSQData = exportEngine.new TEMPLATE_DATA();
            exportEngine.ConvertRawToWsq(exportAudit.FingerData[0].Template[0].Data, exportAudit.ImageWidth, exportAudit.ImageHeight, SaveWSQData, fQuality);
            if (SaveWSQData != null) {
                String szSavePath = "D:\\sample\\1" + ".wsq";

                if (WriteFile(szSavePath, SaveWSQData.Data) == false) {
                    javax.swing.JOptionPane.showMessageDialog(null, "WSQ Image save fail");
                } else {
                    try {
                        HttpURLConnection servletConnection = getUrlConnection();
                        OutputStream os = servletConnection.getOutputStream();

                        FileInputStream fis = new FileInputStream(szSavePath);
                        int msg = fis.read();
                        while (msg != -1) {
                            os.write(msg);
                            msg = fis.read();
                        }
//send data  
                        os.flush();
                        os.close();


//Remember your doPost() method gets fired actually by these last 2 lines.  
                        servletConnection.getResponseMessage();
                        servletConnection.disconnect();

                        servletConnection = null;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    System.out.println("File sent!");
                   // System.exit(0);

                }

            }
            if (CheckError()) {
                return;
            }


            javax.swing.JOptionPane.showMessageDialog(null, "Make WSQ Data Success");
            buton2.setEnabled(true);
        }
    }

    private void btnRegistrationActionPerformed(java.awt.event.ActionEvent evt) {

        try {
         String Url="http://192.168.35.56/IUATC/FingerVerification.jsp";
            HttpURLConnection con = (HttpURLConnection) new URL(Url).openConnection();
           // InputStream is =  con.getInputStream();
            BufferedReader brr = new BufferedReader(new InputStreamReader(con.getInputStream()));
           // String output = brr.readLine();
        //  while(output!=null)
         // {
         //     System.out.println(output);
          //    output = brr.readLine();
         // }  
          String line2;
            while(( line2= brr.readLine()) !=null)
            {
            //  if(line2.equals("Verify Failed.."))
                System.out.println(line2);
                 //javax.swing.JOptionPane.showMessageDialog(null, "Verify Failed..");
                 // javax.swing.JOptionPane.showMessageDialog(null, "Verify OK..");
            }
           
          //javax.swing.JOptionPane.showMessageDialog(null, output);
         
          brr.close();
         con.disconnect();
        } catch (MalformedURLException e) {
        } catch (IOException i) {
        }
    }

    private static HttpURLConnection getUrlConnection() throws MalformedURLException, IOException {
        String servletLocation = "http://192.168.35.56/IUATC/FingerServer.jsp";
        URL testServlet = new URL(servletLocation);
        HttpURLConnection servletConnection = (HttpURLConnection) testServlet.openConnection();

//inform the connection that we will send output and accept input  
        servletConnection.setDoInput(true);
        servletConnection.setDoOutput(true);
        servletConnection.setAllowUserInteraction(true);

//Don't use a cached version of URL connection.  
        servletConnection.setUseCaches(false);
        servletConnection.setDefaultUseCaches(false);

//Specify the content type that we will send binary data  
        servletConnection.setRequestProperty("Content-Type", "application/octet-stream");
        servletConnection.setRequestMethod("POST");


        return servletConnection;
    }

  /* private static HttpURLConnection getUrlConnection1() throws MalformedURLException, IOException {
        String servletLocation1 = "http://192.168.35.56/IUATC/FingerVerification.jsp";
        URL testServlet1 = new URL(servletLocation1);
        HttpURLConnection servletConnection = (HttpURLConnection) testServlet1.openConnection();

//inform the connection that we will send output and accept input  
        servletConnection.setDoInput(true);
        servletConnection.setDoOutput(true);
        servletConnection.setAllowUserInteraction(true);

//Don't use a cached version of URL connection.  
        servletConnection.setUseCaches(false);
        servletConnection.setDefaultUseCaches(false);

//Specify the content type that we will send binary data  
        servletConnection.setRequestProperty("Content-Type", "application/octet-stream");
        servletConnection.setRequestMethod("POST");


        return servletConnection;
    } */

    private Boolean WriteFile(String fileName, byte[] data) {
        java.io.File newFile = new java.io.File(fileName);
        java.io.DataOutputStream out;

        try {
            out = new java.io.DataOutputStream(new java.io.FileOutputStream(newFile, false));
        } catch (java.io.FileNotFoundException ex) {
            javax.swing.JOptionPane.showMessageDialog(null, "File Creat failed!!");
            return false;
        }

        try {
            out.write(data);
            out.close();
        } catch (java.io.IOException e) {
            javax.swing.JOptionPane.showMessageDialog(null, "File Write failed!!");
            return false;
        }

        return true;
    }

    public static void main(String[] args) {
        new ServerVerification();
    }
}
