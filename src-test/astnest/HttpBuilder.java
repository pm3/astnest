package astnest;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.ByteArrayPartSource;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.util.EncodingUtil;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class HttpBuilder {

	HttpClient client;
	String path;
	boolean multipat;
	List<WValue> values = new ArrayList<WValue>();

	public HttpBuilder(HttpClient client, String path, boolean multipat) {
		this.client = client;
		this.path = path;
		this.multipat = multipat;
	}

	public HttpBuilder add(String name, String value) {
		values.add(new WValue(name, value, null, null));
		return this;
	}

	public HttpBuilder add(String name, String fileName, byte[] content, String contentType) {
		values.add(new WValue(name, fileName, content, contentType));
		return this;
	}

	public HttpBuilder add(String name, String fileName, InputStream content, String contentType) {
		values.add(new WValue(name, fileName, content, contentType));
		return this;
	}

	public byte[] sendGet() throws Exception {
		// long l1 = System.nanoTime();

		List<NameValuePair> valuePairs = new ArrayList<NameValuePair>(values.size());
		for (WValue v : values) {
			if (v.value != null)
				valuePairs.add(new NameValuePair(v.name, v.value));
		}

		GetMethod g = new GetMethod(path + "?" + EncodingUtil.formUrlEncode(valuePairs.toArray(new NameValuePair[0]), "UTF-8"));
		System.out.println("GET " + g.getPath());
		HttpClient myclient = client != null ? client : new HttpClient();
		int res = myclient.executeMethod(g);
		if (res != 200)
			throw new Exception("http:" + res);
		byte[] data = g.getResponseBody();
		g.abort();
		return data;
		// long l2 = System.nanoTime();
		// System.out.println("get[" + path + "]: " + ((l2 - l1) / 1000000.0));
	}

	public String sendGetStr() throws Exception {
		String s = new String(sendGet(), "UTF-8");
		System.out.println(s);
		return s;
	}

	public JsonElement sendGetJson() throws Exception {
		String s = new String(sendGet(), "UTF-8");
		return new JsonParser().parse(s);
	}

	public byte[] sendPost() throws Exception {
		// long l1 = System.nanoTime();

		HttpClient myclient = client != null ? client : new HttpClient();
		PostMethod p = new PostMethod(path);
		System.out.println("POST " + p.getPath());
		if (multipat) {
			List<Part> parts = new ArrayList<Part>();
			for (WValue v : values) {
				if (v.content instanceof File) {
					parts.add(new FilePart(v.name, (File) v.content));
				} else if (v.content instanceof byte[]) {
					parts.add(new FilePart(v.name, new ByteArrayPartSource(v.name, (byte[]) v.content), v.contentType, null));
				} else if (v.value != null) {
					parts.add(new StringPart(v.name, v.value));
				}
			}
			p.setRequestEntity(new MultipartRequestEntity(parts.toArray(new Part[0]), p.getParams()));
		} else {
			for (WValue v : values)
				if (v.value != null)
					p.addParameter(v.name, v.value);
		}
		int res = myclient.executeMethod(p);
		if (res != 200)
			throw new Exception("http:" + res);
		byte[] data = p.getResponseBody();
		p.abort();
		return data;
		// long l2 = System.nanoTime();
		// System.out.println("post[" + path + "]-" + code + ": " + ((l2 - l1) /
		// 1000000.0));
	}

	public String sendPostString() throws Exception {
		String s = new String(sendPost(), "UTF-8");
		System.out.println(s);
		return s;
	}

	public Object sendPostJson() throws Exception {
		String s = new String(sendPost(), "UTF-8");
		return new JsonParser().parse(s);
	}

	public byte[] sendPost(byte[] data, String contentType) throws Exception {
		// long l1 = System.nanoTime();

		HttpClient myclient = client != null ? client : new HttpClient();

		List<NameValuePair> valuePairs = new ArrayList<NameValuePair>(values.size());
		for (WValue v : values) {
			if (v.value != null)
				valuePairs.add(new NameValuePair(v.name, v.value));
		}

		PostMethod p = new PostMethod(path + "?" + EncodingUtil.formUrlEncode(valuePairs.toArray(new NameValuePair[0]), "UTF-8"));
		p.setRequestEntity(new ByteArrayRequestEntity(data, contentType));
		System.out.println("POST " + p.getPath());
		int res = myclient.executeMethod(p);
		if (res != 200)
			throw new Exception("http:" + res);
		byte[] data2 = p.getResponseBody();
		p.abort();
		return data2;
		// long l2 = System.nanoTime();
		// System.out.println("post[" + path + "]-" + code + ": " + ((l2 - l1) /
		// 1000000.0));
	}

	public String sendPostString(byte[] data, String contentType) throws Exception {
		String s = new String(sendPost(data, contentType), "UTF-8");
		System.out.println(s);
		return s;
	}

	public Object sendPostJson(byte[] data, String contentType) throws Exception {
		return new JsonParser().parse(sendPostString(data, contentType));
	}

	public static HttpBuilder create(HttpClient client, String path) {
		return new HttpBuilder(client, path, false);
	}

	public static HttpBuilder multipart(HttpClient client, String path) {
		return new HttpBuilder(client, path, true);
	}

	public static class WValue {
		String name;
		String value;
		Object content;
		String contentType;

		public WValue(String name, String value, Object content, String contentType) {
			this.name = name;
			this.value = value;
			this.content = content;
			this.contentType = contentType;
		}
	}
}
