import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class WebServer extends Thread implements Runnable{
	static final int PORT = 8888;
	Socket socket;
	
	public static void main(String[] args) throws IOException {
		ExecutorService pool = Executors.newFixedThreadPool(4);
		while (!Thread.interrupted())
			try(ServerSocket ss = new ServerSocket(PORT);) {

				Runnable r1 = new WebServer(ss.accept());
				
				pool.execute(r1);
				
			} 
			pool.shutdown();  
		}

		WebServer(Socket s) {
			socket = s;
		}        

		@Override
		public void run() {
			
			try( OutputStream out = socket.getOutputStream();
				InputStream in = socket.getInputStream() ) {
				
				try{
					BufferedReader br = new BufferedReader(new InputStreamReader(in));
					BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));                   
					String line1=br.readLine();
					System.out.println(line1);
					String details[]=line1.split(" ");
					if (details[1].equals("/")) {
						details[1] = "/index.html";
					}
					String htmlv = details[2];
					System.out.println(htmlv);
					String details1[]=details[1].split("/");


					String fileName =details1[1];

					String temp[] = fileName.split("\\.");
					String extension= temp[1];
					
					
					String dataReturn = "";
					if(extension.equals("png") || extension.equals("gif") || extension.equals("jpg") || extension.equals("jpeg"))
					{

						File file = new File(fileName);
						FileInputStream fis = new FileInputStream(file);
						byte[] data = new byte[(int) file.length()];
						fis.read(data);
						fis.close();

						DataOutputStream binaryOut = new DataOutputStream(out);
						binaryOut.writeBytes(htmlv+" 200 OK\r\n");
						binaryOut.writeBytes("Content-Type: image/"+extension+"\r\n");
						binaryOut.writeBytes("Content-Length: " + data.length);
						binaryOut.writeBytes("\r\n\r\n");
						binaryOut.write(data);

						binaryOut.close();
					}else if(extension.equals("html") || extension.equals("txt") || extension.equals("text"))
					{
						bw.write(htmlv+" 200 OK\r\n");
						bw.write("Content-Type: text/"+extension+"\r\n");
						bw.write("\r\n");



						FileReader myFile = new FileReader(fileName);
						Scanner scanner = new Scanner(myFile);
						dataReturn = "";
						while(scanner.hasNextLine()) {
							dataReturn = scanner.nextLine();
							bw.write(dataReturn);
						}
						scanner.close();
						
					}else{          
						bw.write(htmlv+"  Status 404 – Not Found\r\n");
						bw.write("Content-Type: text/html\r\n");
						bw.write("\r\n");
						bw.write("<h1> 404 - Not Found</h1>");
						
					}   

					bw.close();

				}catch(Exception e)
				{System.out.println(e);
					BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));                   

					bw.write("HTTP/1.0  Status 404 – Not Found\r\n");
					bw.write("Content-Type: text/html\r\n");
					bw.write("\r\n");
					bw.write("<h1> 404 - Not Found</h1>");
					
					bw.close();
					

				}
            //TODO
			} catch (IOException e) {
//            out.write("400 ERROR".getBytes());
				
				Logger.getGlobal().severe(e.getMessage());
			}
		}

	}