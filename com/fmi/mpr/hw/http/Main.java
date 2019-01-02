package com.fmi.mpr.hw.http;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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

	public void run() {

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

	public void activate() {

		if (!isActive) {
			this.isActive = true;
			run();
		}
	}

	private void processClient(Socket client) throws IOException {

		try (BufferedInputStream br = new BufferedInputStream(client.getInputStream());
				PrintStream ps = new PrintStream(client.getOutputStream(), true)) {

			String response = read(ps, br, client);
			write(ps, response);
		}

	}

	private void write(PrintStream ps, String response) {
		if (ps != null) {

			ps.println("HTTP/1.1 200 OK");
			ps.println();
			ps.println("<!DOCTYPE html>\n" + "<html>\n" + "<head>\n" + "	<title></title>\n" + "</head>\n"
					+ "<body>\n" + "<form action=\"/action_page.php\">\n"
					+ "			  <input type=\"file\" name=\"pic\" accept=\"image/*\">\n"
					+ "			  <input type=\"submit\">\n" + "			</form> " + "</body>\n" + "</html>");
		}
	}

	private String read(PrintStream ps, BufferedInputStream bis, Socket client) throws IOException {

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

			return parseRequest(ps, request.toString(), client);
		}
		return "Error";
	}

	private String parseRequest(PrintStream ps, String request, Socket client) throws IOException {

		System.out.println(request);

		String[] lines = request.split("\n");
		String firstHeader = lines[0];
		String type = firstHeader.split(" ")[0];
		System.out.println(firstHeader);
		String uri = firstHeader.split(" ")[1];
		this.file = uri.substring(1);

		String typeOfExtension = uri.split("\\.")[1];

		if (type.equals("GET")) {
			return get(ps, typeOfExtension);
		} else if (type.equals("POST")) {
			return post(ps, client, lines);
		}

		return null;
	}

	private String post(PrintStream ps, Socket client, String[] lines) throws IOException {
		String header = lines[0];
		String url = header.split(" ")[1];

		if (url.length() != 1) {

			url = url.substring(1);
		}

		if (url.equals("upload.php")) {
			StringBuilder body = new StringBuilder();

			boolean readBody = false;
			for (String line : lines) {
				if (readBody) {

					body.append(line);
				}

				if (line.trim().isEmpty()) {

					readBody = true;
				}

			}

			return parseBody(client, body.toString());
		}

		return null;
	}

	private String get(PrintStream ps, String typeOfExtension) throws IOException {
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
						+ "<body>\n" + "			  Error! This file doesn't exist \n" + "</body>\n" + "</html>");
			}
		}

		else if (typeOfExtension.equals("png") || typeOfExtension.equals("jpg") || typeOfExtension.equals("bmp")) {

			try {
				ps.println();
				sendPic(ps);
			} catch (IOException e) {

				ps.println();
				ps.println("<!DOCTYPE html>\n" + "<html>\n" + "<head>\n" + "	<title></title>\n" + "</head>\n"
						+ "<body>\n" + "			  Error! This file doesn't exist \n" + "</body>\n" + "</html>");
			}
		}

		else if (typeOfExtension.equals("txt")) {

			try {

				ps.println();
				sendTxt(ps);
			} catch (IOException e) {

				ps.println();
				ps.println("<!DOCTYPE html>\n" + "<html>\n" + "<head>\n" + "	<title></title>\n" + "</head>\n"
						+ "<body>\n" + "			  Error! This file doesn't exist \n" + "</body>\n" + "</html>");
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

	private String parseBody(Socket client, String body) throws IOException {
		if (body != null && !body.trim().isEmpty()) {

			String[] operands = body.split(";");
			this.file = operands[2].split("=")[1].split("\"")[1];

			String type = this.file.split("\\.")[1];
			BufferedInputStream bis = new BufferedInputStream(client.getInputStream());
			String data = null;
			PrintStream ps = new PrintStream(client.getOutputStream(), true);

			if (type.equals("jpg") || type.equals("jpeg") || type.equals("bmp") || type.equals("mp4")
					|| type.equals("avi")) {

				data = sendMedia(bis, ps);
			}

			if (type.equals("txt")) {

				data = sendTextFiles(bis, ps);
			}

			File file = new File(this.file);
			FileOutputStream is = new FileOutputStream(file.getAbsolutePath());
			is.write(data.getBytes());
			is.close();
			System.out.println("File sent!");
		}

		return null;
	}

	private String sendTextFiles(BufferedInputStream bis, PrintStream ps) throws IOException {
		int bytesRead = 0;
		byte[] buffer = new byte[8192];

		while ((bytesRead = bis.read(buffer, 0, 8192)) > 0) {

			ps.write(buffer, 0, bytesRead);
		}

		return ps.toString();

	}

	private String sendMedia(BufferedInputStream bis, PrintStream ps) throws IOException {
		int bytesRead = 0;
		byte[] buffer = new byte[8192];

		while ((bytesRead = bis.read(buffer, 0, 8192)) > 0) {

			ps.write(buffer, 0, bytesRead);
		}

		return ps.toString();
	}

	public static void main(String[] args) throws IOException {
		Main n = new Main();
		n.activate();
	}
}