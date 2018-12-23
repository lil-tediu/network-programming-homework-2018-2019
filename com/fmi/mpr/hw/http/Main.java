package com.fmi.mpr.hw.http;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

	private ServerSocket ss;
	private String file;
	private boolean isActive;

	public Main() throws IOException {
		ss = new ServerSocket(8888);
	}

	public void run() throws IOException {

		while (isActive) {

			try {
				listen();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void listen() throws IOException {
		Socket client = null;
		try {
			client = ss.accept();
			System.out.println(client.getInetAddress() + " connected..!");

			processClient(client);

			System.out.println("Connection to " + client.getInetAddress() + " closed..!");
		} finally {
			if (client != null) {
				client.close();
			}
		}
	}

	public void activate() throws IOException {

		if (!isActive) {
			this.isActive = true;
			run();
		}
	}

	private void processClient(Socket client) throws IOException {

		try (BufferedInputStream br = new BufferedInputStream(client.getInputStream());
				PrintStream ps = new PrintStream(client.getOutputStream(), true)) {

			String response = read(ps, br);
			write(ps, response);
		}

	}

	private void write(PrintStream ps, String response) {

	}

	private String read(PrintStream ps, BufferedInputStream bis) throws IOException {

		if (bis != null) {
			StringBuilder request = new StringBuilder();

			byte[] buffer = new byte[1024];
			int bytesRead = 0;

			while ((bytesRead = bis.read(buffer, 0, 1024)) > 0) {
				request.append(new String(buffer, 0, bytesRead));

				if (bytesRead < 1024) {
					break;
				}
			}

			return parseRequest(ps, request.toString());
		}
		return "Error";
	}

	private String parseRequest(PrintStream ps, String request) throws IOException {

		System.out.println(request);

		String firstHeader = request.split("\n")[0];
		String type = firstHeader.split(" ")[0];
		String uri = firstHeader.split(" ")[1];
		this.file = uri.substring(1);

		String typeOfExtension = uri.split("\\.")[1];

		if (type.equals("GET")) {
			return get(ps, typeOfExtension);
		} else if (type.equals("POST")) {
			return post(ps, typeOfExtension);
		}

		return null;
	}

	private String post(PrintStream ps, String typeOfExtension) {
		// TODO Auto-generated method stub
		return null;
	}

	private String get(PrintStream ps, String typeOfExtension) {
		ps.println("HTTP/1.1 200 OK");
		ps.println();

		if (typeOfExtension.equals("mp4") || typeOfExtension.equals("avi")) {

			try {

				ps.println("Content-Type: video/mp4");
				ps.println();
				sendVideo(ps);
			} catch (IOException e) {

				ps.println();
				ps.println("<!DOCTYPE html>\n" + "<html>\n" + "<head>\n" + "	<title></title>\n" + "</head>\n"
						+ "<body>\n" + "<form action=\"/action_page.php\">\n"
						+ "			  <input type=\"file\" name=\"pic\" accept=\"image/*\">\n"
						+ "			  <input type=\"submit\">\n" + "			</form> " + "</body>\n" + "</html>");
			}
		}

		else if (typeOfExtension.equals("png") || typeOfExtension.equals("jpg") || typeOfExtension.equals("bmp")) {

			try {
				ps.println();
				sendPic(ps);
			} catch (IOException e) {

				ps.println();
				ps.println("<!DOCTYPE html>\n" + "<html>\n" + "<head>\n" + "	<title></title>\n" + "</head>\n"
						+ "<body>\n" + "<form action=\"/action_page.php\">\n"
						+ "			  <input type=\"file\" name=\"pic\" accept=\"image/*\">\n"
						+ "			  <input type=\"submit\">\n" + "			</form> " + "</body>\n" + "</html>");
			}
		}

		else if (typeOfExtension.equals("txt")) {

			try {

				ps.println();
				sendTxt(ps);
			} catch (IOException e) {

				ps.println();
				ps.println("<!DOCTYPE html>\n" + "<html>\n" + "<head>\n" + "	<title></title>\n" + "</head>\n"
						+ "<body>\n" + "<form action=\"/action_page.php\">\n"
						+ "			  <input type=\"file\" name=\"pic\" accept=\"image/*\">\n"
						+ "			  <input type=\"submit\">\n" + "			</form> " + "</body>\n" + "</html>");
			}
		}
		return null;
	}

	private void sendTxt(PrintStream ps) throws IOException {
		File f1 = new File(file);
		String path = f1.getAbsolutePath();

		FileInputStream fis = new FileInputStream(path);


		int bytesRead = 0;
		byte[] buffer = new byte[8192];
 		
 		while ((bytesRead = fis.read(buffer, 0, 8192)) > 0) {
 			ps.write(buffer, 0, bytesRead);
 		}
 		
 		ps.flush();
 		System.out.println("Send txt");
		fis.close();
		
	}

	private void sendPic(PrintStream ps) throws IOException {
		File f1 = new File(file);
		String path = f1.getAbsolutePath();

		FileInputStream fis = new FileInputStream(path);


		int bytesRead = 0;
		byte[] buffer = new byte[4096];
 		
 		while ((bytesRead = fis.read(buffer, 0, 4096)) > 0) {
 			ps.write(buffer, 0, bytesRead);
 		}
 		
 		ps.flush();
 		System.out.println("Send pic");
 		fis.close();
		
	}

	private void sendVideo(PrintStream ps) throws IOException {
		File f1 = new File(file);
		String path = f1.getAbsolutePath();

		FileInputStream fis = new FileInputStream(path);

		int bytesRead = 0;
		byte[] buffer = new byte[8192];
 		
 		while ((bytesRead = fis.read(buffer, 0, 8192)) > 0) {
 			ps.write(buffer, 0, bytesRead);
 		}
 		
 		ps.flush();
 		System.out.println("Send video");
 		fis.close();
	}

	private String parseBody(String body) {


		return null;
	}

	public static void main(String[] args) throws IOException {
		Main n = new Main();
		n.activate();
	}
}