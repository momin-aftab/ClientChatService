package mainApplication;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;
import javax.swing.JFrame;
import java.time.LocalTime;


/**
 * A simple Swing-based client for the chat server. Graphically it is a frame with a text
 * field for entering messages and a textarea to see the whole dialog.
 *
 * The client follows the following Chat Protocol. When the server sends "SUBMITNAME" the
 * client replies with the desired screen name. The server will keep sending "SUBMITNAME"
 * requests as long as the client submits screen names that are already in use. When the
 * server sends a line beginning with "NAMEACCEPTED" the client is now allowed to start
 * sending the server arbitrary strings to be broadcast to all chatters connected to the
 * server. When the server sends a line beginning with "MESSAGE" then all characters
 * following this string should be displayed in its message area.
 */
public class NewChatClient {

    Scanner in;
    PrintWriter out;  
    
    // This object manages all the GUI components of the client
    ClientGUI gui;
    
    JFrame frame;

    public NewChatClient() {

        gui = new ClientGUI(out);
        frame = gui.getFrame();
        
        gui.getTextField().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                out.println(gui.getTextFieldText());
                gui.setTextFieldText();
            }
        });
   
    } 
    
    // Used to implement time stamps for each message
    public String getCurrentTime()
    {
    	LocalTime current = LocalTime.now();
    	String time = current.toString();
    	String[] splitter = time.split(":");
    	String displayTime = splitter[0] + ":" + splitter[1];
    	return displayTime;
    }
    
    
    
    private void run() throws IOException {
        try {
        	
        	// Get the details provided by the client
        	String[] server = new GetServerInfo().getServerInfo(frame);
        	String ip = server[0];
        	String user_ip = server[2];
        	String user_port = server[3];
        	System.out.println(user_port);
        	int port = Integer.parseInt(server[1]);
            Socket socket = new Socket(ip, port);
            in = new Scanner(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream(), true);
            String id = "";

            // The input stream will be split into a list by "@"
            while (in.hasNextLine()) {
            	String[] line = in.nextLine().split("@");
            	
            	
            	System.out.println(Arrays.toString(line));
            	
                // Processes output based on 'SUBMITNAME' token
                if (line[0].equals("SUBMITNAME")) {
                	id = gui.getName(frame);
                	String group = id + " " + user_ip + " " + user_port;
                    out.println(group);
                    
                }
                
                // Processes output based on 'NAMEACCEPTED' token 
                else if (line[0].equals("NAMEACCEPTED")) {
                    this.frame.setTitle("Chatter - " + line[1]);
                    gui.getTextField().setEditable(true);
                    gui.setLabelText(line[2]);
                } 
                
                // Processes output based on 'MESSAGE' token
                // This token primarily displays public client broadcasts in the chat window or some server notifications
                else if (line[0].equals("MESSAGE")) {
                	
                	String time = getCurrentTime();
                	gui.appendToMsg(line[1], time);
                	gui.setLabelText(line[2]);
                	System.out.println(getCurrentTime());
                }
                
                // Processes output based on 'PRIVATE' token
                // This token is used to process private message broadcasting between any two clients connected to the server
                else if (line[0].equals("PRIVATE")) {
                	String user = line[1];
                	int tabIndex = gui.getTabIndex(user);
                	
                	if (tabIndex == -1)
                	{
                		gui.createTab(user);
                	}
                	
                	tabIndex = gui.getTabIndex(user);
                	String time = getCurrentTime();
                	gui.appendToTab(tabIndex, line[2], time);
                	
                }
                
                // Processes output based on 'COORDINATOR' token
                // This token is used to display the relevant message when a new coordinator has been selected
                else if (line[0].equals("COORDINATOR"))
                {
                	String time = getCurrentTime();
                	gui.appendToMsg(line[1], time);
                	this.frame.setTitle(line[2]);
                }
                
                // Processes output based on 'PING' token
                else if (line[0].equals("PING"))
                {
                	String time = getCurrentTime();
                	gui.appendToMsg(line[1], time);
                }

                else if (line[0].equals("KICK"))
                {
                	String time = getCurrentTime();
                	gui.appendToMsg(line[1], time);
                }
                
                else if (line[0].equals("KICK"))
                {
                	String time = getCurrentTime();
                	gui.appendToMsg(line[1], time);
                }
                
                // Processes output based on 'GAME' token
                // Used to display the TIC TAC TOE game to the client
                else if (line[0].equals("GAME")) {
                	gui.appendGame(line[1]);
                }
                	                
            }
            
        } finally {
            frame.setVisible(false);
            frame.dispose();
        }
    }

    public static void main(String[] args) throws Exception {

        NewChatClient client = new NewChatClient();

        client.run();
    }
}

