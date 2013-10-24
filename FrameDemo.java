/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fil;

/**
 *
 * @author iith hanuman
 */
import com.nitgen.SDK.BSP.NBioBSPJNI;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.swing.JButton;
import javax.swing.JFrame;

public class FrameDemo extends JFrame
{
        JButton buton1;
        JButton buton2;
        NBioBSPJNI     bsp;
        NBioBSPJNI.Export   exportEngine;
        NBioBSPJNI.Export.TEMPLATE_DATA  SaveWSQData;
        NBioBSPJNI.FIR_HANDLE   hWSQFIR1;
        NBioBSPJNI.FIR_HANDLE hWSQFIR2;
        byte[] writebuffer;
        FileInputStream fis;
        BufferedOutputStream  bos; 
        PreparedStatement pre;
	public FrameDemo()
	{
            
            
            
                this.setTitle("Registration....");
		this.setSize(640 , 480);
		this.setLayout(null);
	        this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                bsp = new NBioBSPJNI();
                  if (CheckError())
                    return ;
                     exportEngine = bsp.new Export();
                        if (CheckError())
                         return ;

        //setTitle("NBioAPI_WSQDemo BSP version: " + bsp.GetVersion());

               bsp.OpenDevice();
 
               if (CheckError())
                   return ;

                // buton1.setEnabled(true);
                
                buton1 = new JButton("Fingure Capture");
                this.add(buton1);   // veya this.getContentPane().add(buton1);
                buton1.setBounds(101,150,150,50);
                buton1.setVisible(true);
                buton2 = new JButton("registration");
                this.add(buton2);  
               // buton2.setVisible(false);
                // veya this.getContentPane().add(buton1);
               buton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCaptureActionPerformed(evt);
            }
        });
               
               
               buton2.setBounds(350,150,150,50);
               buton2.addActionListener(new java.awt.event.ActionListener() {
               public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegistrationActionPerformed(evt);
            }
        });

        // String s1=javax.swing.JOptionPane.showInputDialog("enter first name::");
        // System.out.println(s1);
        // javax.swing.JOptionPane.showInputDialog("enter first name::");
        
        
    }

    public void dispose()
    {
        if (hWSQFIR1 != null)  {
            hWSQFIR1.dispose();
            hWSQFIR1 = null;
        }

        if (hWSQFIR2 != null)  {
            hWSQFIR2.dispose();
            hWSQFIR2 = null;
        }

        if (bsp != null) {
            bsp.CloseDevice();
            bsp.dispose();
            bsp = null;
        }
    }

    public void Closing()
    {
        dispose();
    }

    private Boolean CheckError()
    {
        if (bsp.IsErrorOccured())  {
            javax.swing.JOptionPane.showMessageDialog(null, "NBioBSP Error Occured [" + bsp.GetErrorCode() + "]");
            return true;
        }

        return false;
    }
		
 private void btnCaptureActionPerformed(java.awt.event.ActionEvent evt) 
 {
        NBioBSPJNI.FIR_HANDLE       hSavedFIR;
        NBioBSPJNI.FIR_HANDLE       hSavedAuditFIR;

        hSavedFIR = bsp.new FIR_HANDLE();
        hSavedAuditFIR = bsp.new FIR_HANDLE();

        bsp.Capture(NBioBSPJNI.FIR_PURPOSE.VERIFY, hSavedFIR, -1, hSavedAuditFIR, null);

        if (CheckError())
            return ;

        if (hSavedAuditFIR != null)  {
            NBioBSPJNI.INPUT_FIR inputFIR = bsp.new INPUT_FIR();

            inputFIR.SetFIRHandle(hSavedAuditFIR);

            NBioBSPJNI.Export.AUDIT exportAudit = exportEngine.new AUDIT();

            exportEngine.ExportAudit(inputFIR, exportAudit);

            if (CheckError())
                return ;

            float fQuality = 0.7f;

            if (SaveWSQData != null)
                SaveWSQData = null;

            SaveWSQData = exportEngine.new TEMPLATE_DATA();
            exportEngine.ConvertRawToWsq(exportAudit.FingerData[0].Template[0].Data , exportAudit.ImageWidth, exportAudit.ImageHeight, SaveWSQData, fQuality);

            if (CheckError())
                return ;

          
            javax.swing.JOptionPane.showMessageDialog(null, "Make WSQ Data Success");
            buton2.setEnabled(true);
        }
      
 
 }
 private void btnRegistrationActionPerformed(java.awt.event.ActionEvent evt) 
 {
      if (SaveWSQData != null)  
        {           
     String szSavePath = "D:\\sample\\1" + ".wsq";

                if (WriteFile(szSavePath, SaveWSQData.Data) == false)  {
                    javax.swing.JOptionPane.showMessageDialog(null, "WSQ Image save fail");
                }
                else  {
                   
                 try {
		Class.forName("com.mysql.jdbc.Driver");
	} catch (ClassNotFoundException e) {
		System.out.println("Where is your MySQL JDBC Driver?");
		e.printStackTrace();
	}
 
           Connection con = null;
 
	try 
        {
		con = DriverManager
		.getConnection("jdbc:mysql://localhost:3306/mysql","root","root");
 
	} catch (SQLException e) {
		System.out.println("Connection Failed! Check output console");
		e.printStackTrace();
	}
           try {
            //con = MyConnectionPool.getSingletonObject().getConnection();
             pre = con.prepareStatement("insert into reg values(?,?,?,?,?)");
             FileInputStream inputStream=new FileInputStream(new File(szSavePath));
             pre.setBinaryStream(5,(InputStream)inputStream);
           // BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
           // System.out.println("enter age:");
            // reg r1=new reg();
           // r1.setVisible(true);
           String s1=javax.swing.JOptionPane.showInputDialog("enter reg_id::");
            int regid=Integer.parseInt(s1);
             //System.out.println("enter reg_id:");
           String FirstName= javax.swing.JOptionPane.showInputDialog("enter First_Name::");
            
           
              String s3= javax.swing.JOptionPane.showInputDialog("enter Last_Name::");
             String Email= javax.swing.JOptionPane.showInputDialog("enter Email::");
             // int =Integer.parseInt(s4);
              pre.setInt(1,regid);
              pre.setString(2,FirstName);            
              pre.setString(3,FirstName); 
              pre.setString(4,Email);
              pre.executeUpdate();
            javax.swing.JOptionPane.showMessageDialog(null, "WSQ Image save success");            
        } catch (SQLException ex) {
           ex.printStackTrace();
            // Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }catch(IOException ex)
        {
        ex.printStackTrace();
        }
        finally {
            try {
                pre.close();
                con.close();
            } catch (SQLException ex) {
              ex.printStackTrace();
                // Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
      
                
                }
            }
 }
  private Boolean WriteFile(String fileName, byte[] data)
    {
        java.io.File newFile = new java.io.File(fileName);
        java.io.DataOutputStream out;

        try  {
            out = new java.io.DataOutputStream(new java.io.FileOutputStream(newFile, false));
        }
            catch (java.io.FileNotFoundException ex)  {
            javax.swing.JOptionPane.showMessageDialog(null, "File Creat failed!!");
            return false;
        }
          try{
            out.write(data);
            out.close();
        }
        catch (java.io.IOException e)  {
            javax.swing.JOptionPane.showMessageDialog(null, "File Write failed!!");
            return false;
        }

        return true;
    }
	public static void main(String[] args)
	{
		new FrameDemo();
	}

}
