import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;


public class GUIConsole {

	private OutputStream consoleOut;
	public InputStream consoleIn;
	public PrintStream log;
	private byte[] inputByteStream = new byte[1024]; 
	private ByteArrayInputStream bs = new ByteArrayInputStream(inputByteStream);
	
	private JFrame f = new JFrame();
	private JPanel pnlConsole = new JPanel();
	private JPanel pnlButton = new JPanel();

	public JTextArea taConsole = new JTextArea(30, 80);
	private JButton btnClear = new JButton("Clear Console");
	private JScrollPane spConsole = new JScrollPane(taConsole);
	private JButton btnSend = new JButton("Send");
	private JTextField tfMessage = new JTextField(33);
	
	private JMenuBar mb = new JMenuBar(); // Menubar
	private JMenu mnuFile = new JMenu("File"); // File Entry on Menu bar
	private JMenuItem mnuItemQuit = new JMenuItem("Quit"); // Quit sub item
	private JMenu mnuHelp = new JMenu("Help"); // Help Menu entry
	private JMenuItem mnuItemAbout = new JMenuItem("About"); // About Entry

	public GUIConsole(String title) {

		f.setTitle(title);
		f.setJMenuBar(mb);

		mnuFile.add(mnuItemQuit);  // Create Quit line
		mnuHelp.add(mnuItemAbout); // Create About line
		mb.add(mnuFile);        // Add Menu items to form
		mb.add(mnuHelp);

		pnlConsole.add(spConsole);
		pnlButton.add(tfMessage);
		pnlButton.add(btnSend);
		pnlButton.add(btnClear);
		
		f.getContentPane().setLayout(new BorderLayout());
		f.getContentPane().add(pnlConsole, BorderLayout.NORTH);
		f.getContentPane().add(pnlButton, BorderLayout.SOUTH);

		f.addWindowListener(new ListenCloseWdw());
		mnuItemQuit.addActionListener(new ListenMenuQuit());
		btnClear.addActionListener(new ClearConsole());
		btnSend.addActionListener(new SendToInputStream());
		
		taConsole.setEditable(false);
		taConsole.setWrapStyleWord(true);
		taConsole.setFont(new Font("Courier New", Font.PLAIN, 14));
		taConsole.setAutoscrolls(true);

		consoleOut = new OutputStream() {

			@Override
			public void write(int arg0) throws IOException {
				taConsole.append(Character.toString((char) arg0));
			}
		};
		
		consoleIn = new InputStream() {
			
			@Override
			public int read() throws IOException {
				// TODO Auto-generated method stub
				return bs.read();
			}
		};
		
		log = new PrintStream(consoleOut);

	}

	public class SendToInputStream implements ActionListener{
		public void actionPerformed(ActionEvent e){
			inputByteStream = tfMessage.toString().getBytes();
		}
	}

	public class ClearConsole implements ActionListener{
		public void actionPerformed(ActionEvent e){
			taConsole.setText("");
		}
	}

	public class ListenMenuQuit implements ActionListener{
		public void actionPerformed(ActionEvent e){
			System.exit(0);         
		}
	}

	public class ListenCloseWdw extends WindowAdapter{
		public void windowClosing(WindowEvent e){
			System.exit(0);         
		}
	}

	public void launchFrame(){
		// Display Frame
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.pack(); //Adjusts panel to components for display
		f.setVisible(true);
	}

}
