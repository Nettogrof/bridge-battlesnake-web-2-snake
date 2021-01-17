package ca.nettogrof.battlesnake.bridge;

import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.post;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.*;

import spark.Request;
import spark.Response;
import spark.Spark;

public class Bridge {
	private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
	private static final Handler HANDLER = new Handler();
	private static final Logger LOG = LoggerFactory.getLogger(Bridge.class);

	private static String port = "8081";

	/**
	 * Main entry point.
	 *
	 * @param args are ignored.
	 */
	public static void main(String[] args) {

		String keyStore = "";
		String keyStorePassword = "";

		if (args.length == 1) {
			port = args[0];

		} else {
			System.out.println("Must provide java args  Port");
		}
		String fileConfig = "Beta.properties";
		try (InputStream input = new FileInputStream(fileConfig)) {

			Properties prop = new Properties();

			// load a properties file
			prop.load(input);

			// get the property value and print it out
			// apiversion= Integer.parseInt(prop.getProperty("apiversion"));
			keyStore = prop.getProperty("keystore");
			keyStorePassword = prop.getProperty("keystorePassword");

		} catch (IOException ex) {
			ex.printStackTrace();
		}

		if (port == null) {
			port = "8081";
			LOG.info("Using default port: {}", port);

		} else {
			LOG.info("Found system provided port: {}", port);
		}
		// CorsFilter.apply();
		port(Integer.parseInt(port));
		Spark.secure(keyStore, keyStorePassword, null, null);
		Spark.options("/*", (request, response) -> {

			String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
			if (accessControlRequestHeaders != null) {
				response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
			}

			String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
			if (accessControlRequestMethod != null) {
				response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
			}

			return "OK";
		});

		Spark.before((request, response) -> response.header("Access-Control-Allow-Origin", "*"));

		get("/", HANDLER::process, JSON_MAPPER::writeValueAsString);
		get("/sMe", HANDLER::process, JSON_MAPPER::writeValueAsString);
		post("/sMe", HANDLER::process, JSON_MAPPER::writeValueAsString);
		post("/ping", HANDLER::process, JSON_MAPPER::writeValueAsString);
		post("/move", HANDLER::process, JSON_MAPPER::writeValueAsString);
		// CorsFilter.apply();
		// Spark.options("/sMe", HANDLER::process, JSON_MAPPER::writeValueAsString);

	}

	/**
	 * Handler class for dealing with the routes set up in the main method.
	 */
	public static class Handler {

		/**
		 * For the ping request
		 */
		@SuppressWarnings("unused")
		private static final Map<String, String> EMPTY = new ConcurrentHashMap<>();

		/**
		 * Generic processor that prints out the request and response from the methods.
		 *
		 * @param req
		 * @param res
		 * @return
		 */
		public Map<String, String> process(Request req, Response res) {

			try {
				JsonNode parsedRequest = JSON_MAPPER.readTree(req.body());
				String uri = req.uri();
				LOG.info("{} called with: {}", uri, req.body());
				Map<String, String> bridgeResponse;
				if (uri.equals("/sMe")) {
					bridgeResponse = startMoveEnd(parsedRequest);

				} else if (uri.equals("/ping")) {
					bridgeResponse = ping();
				} else if (uri.equals("/")) {
					bridgeResponse = root();
				} else if (uri.equals("/move")) {
					bridgeResponse = move(parsedRequest);
				} else {
					throw new IllegalAccessError("Strange call made to the snake: " + uri);
				}
				LOG.info("Responding with: {}", JSON_MAPPER.writeValueAsString(bridgeResponse));
				return bridgeResponse;
			} catch (IOException e) {
				LOG.warn("Something went wrong!", e);
				return null;
			}
		}

		/**
		 * /ping is called by the play application during the tournament or on
		 * play.battlesnake.io to make sure your snake is still alive.
		 *
		 * @param pingRequest a map containing the JSON sent to this snake. See the spec
		 *                    for details of what this contains.
		 * @return an empty response.
		 */
		public Map<String, String> ping() {
			return null;
		}

		/**
		 * / is called by the play application during the tournament or on
		 * play.battlesnake.io to make sure your snake is still alive.
		 *
		 * @param RootRequest a map containing the JSON sent to this snake. See the spec
		 *                    for details of what this contains.
		 * @return apiversion:string - Battlesnake API Version implemented by this
		 *         Battlesnake author:string - Optional username of the Battlesnake’s
		 *         author head:string - Optional custom head for this Battlesnake
		 *         tail:string - Optional custom tail for this Battlesnake color:string
		 *         - Optional custom color for this Battlesnake .
		 */
		public Map<String, String> root() {
			String snakeUrl = "http://34.221.0.38:8000/move";
			// startRequest.get("url").asText();
			String datajson = "{\"game\":{\"id\":\"404749\",\"ruleset\":{\"name\":\"standard\",\"version\":\"v.1.2.3\"},\"timeout\":500},\"turn\":200,\"you\":{\"health\":100,\"id\":\"you\",\"name\":\"#22aa34\",\"body\":[{\"x\":10,\"y\":3},{\"x\":10,\"y\":4},{\"x\":10,\"y\":5},{\"x\":10,\"y\":6},{\"x\":10,\"y\":7},{\"x\":9,\"y\":7},{\"x\":8,\"y\":7},{\"x\":7,\"y\":7},{\"x\":6,\"y\":7},{\"x\":5,\"y\":7}],\"head\":{\"x\":10,\"y\":3},\"length\":10},\"board\":{\"food\":[{\"x\":10,\"y\":1},{\"x\":0,\"y\":0}],\"height\":11,\"width\":11,\"snakes\":[{\"health\":100,\"id\":\"you\",\"name\":\"#22aa34\",\"body\":[{\"x\":10,\"y\":3},{\"x\":10,\"y\":4},{\"x\":10,\"y\":5},{\"x\":10,\"y\":6},{\"x\":10,\"y\":7},{\"x\":9,\"y\":7},{\"x\":8,\"y\":7},{\"x\":7,\"y\":7},{\"x\":6,\"y\":7},{\"x\":5,\"y\":7}],\"head\":{\"x\":10,\"y\":3},\"length\":10},{\"health\":100,\"id\":\"#FFd05e\",\"name\":\"#FFd05e\",\"body\":[{\"x\":9,\"y\":4},{\"x\":9,\"y\":3},{\"x\":9,\"y\":2},{\"x\":9,\"y\":1},{\"x\":8,\"y\":1},{\"x\":7,\"y\":1},{\"x\":6,\"y\":1},{\"x\":6,\"y\":0},{\"x\":5,\"y\":0},{\"x\":4,\"y\":0},{\"x\":3,\"y\":0},{\"x\":2,\"y\":0},{\"x\":1,\"y\":0},{\"x\":1,\"y\":1},{\"x\":1,\"y\":2}],\"head\":{\"x\":9,\"y\":4},\"length\":15}]}}";
			// startRequest.get("data").asText();
			URL url;
			try {
				url = new URL(snakeUrl);

				HttpURLConnection con = (HttpURLConnection) url.openConnection();
				con.setRequestMethod("POST");
				con.setRequestProperty("Content-Type", "application/json; utf-8");
				con.setRequestProperty("Accept", "application/json");
				con.setDoOutput(true);

				try (OutputStream os = con.getOutputStream()) {
					byte[] input = datajson.getBytes("utf-8");
					os.write(input, 0, input.length);
				}

				String reply;
				try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
					StringBuilder response = new StringBuilder();
					String responseLine = null;
					while ((responseLine = br.readLine()) != null) {
						response.append(responseLine.trim());
					}
					reply = response.toString();
				}

				final Map<String, String> response = new HashMap<>();
				JsonNode r = JSON_MAPPER.readTree(reply);
				response.put("move", r.get("move").asText());
				return response;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return null;
		}

		/**
		 * /start is called by the engine when a game is first run.
		 *
		 * @param startRequest a map containing the JSON sent to this snake.
		 * @return a response back to the engine containing the snake setup values.
		 */
		public Map<String, String> startMoveEnd(JsonNode startRequest) {

			try {
				String snakeUrl = startRequest.get("url").asText();

				String datajson = startRequest.get("data").toString();
				System.out.println(snakeUrl);

				System.out.println(datajson);
				URL url;
				url = new URL(snakeUrl);

				HttpURLConnection con = (HttpURLConnection) url.openConnection();
				con.setRequestMethod("POST");
				con.setRequestProperty("Content-Type", "application/json; utf-8");
				con.setRequestProperty("Accept", "application/json");
				con.setDoOutput(true);

				try (OutputStream os = con.getOutputStream()) {
					byte[] input = datajson.getBytes("utf-8");
					os.write(input, 0, input.length);
				}

				String reply;
				try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
					StringBuilder response = new StringBuilder();
					String responseLine = null;
					while ((responseLine = br.readLine()) != null) {
						response.append(responseLine.trim());
					}
					reply = response.toString();
				}
				System.out.println("Reply form snake " + reply);
				final Map<String, String> response = new HashMap<>();
				JsonNode r = JSON_MAPPER.readTree(reply);
				response.put("move", r.get("move").asText());
				return response;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return null;
		}

		/**
		 * /move is called by the engine for each turn the snake has.
		 *
		 * @param moveRequest a map containing the JSON sent to this snake. See the spec
		 *                    for details of what this contains.
		 * @return a response back to the engine containing snake movement values.
		 */
		public Map<String, String> move(JsonNode moveRequest) {

			return null;

		}

		/**
		 * /end is called by the engine when a game is complete.
		 *
		 * @param endRequest a map containing the JSON sent to this snake. See the spec
		 *                   for details of what this contains.
		 * @return responses back to the engine are ignored.
		 */
		public Map<String, String> end(JsonNode endRequest) {
			return null;

		}
	}

}
