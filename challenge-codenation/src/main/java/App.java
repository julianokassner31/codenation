import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONObject;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public class App {

	private static final String URI = "https://api.codenation.dev/v1/challenge/dev-ps/submit-solution?token=ccd10bd3203682cdbb9fc58a7814e8f3b7ca84d3";

	public static void main(String[] args) throws IOException {

		App app = new App();

		JSONObject json = app.parseStringToJson(app.getJsonString());

		String decipheredPhrase = app.mountPhrase(json);

		json.put("decifrado", decipheredPhrase);
		json.put("resumo_criptografico", DigestUtils.sha1Hex(decipheredPhrase));

		app.saveFile(json.toString());

		app.enviarArquivo();

	}

	private String mountPhrase(JSONObject json) {
		
		Map<String, String> decipheredWords = new HashMap<String, String>();
		
		StringBuilder decipheredPhrase = new StringBuilder();
		
		String[] cipheredWords = json.getString("cifrado").split(" ");
		
		Integer numeroCasas = new Integer(json.getInt("numero_casas"));
		
		for (String word : cipheredWords) {

			if (decipheredWords.containsKey(word)) {
				decipheredPhrase.append(decipheredWords.get(word));
				decipheredPhrase.append(" ");
				continue;

			} else {
				String decipheredWord = getDecipheredWord(word, numeroCasas);
				decipheredWords.put(word, decipheredWord);
				decipheredPhrase.append(decipheredWord);
				decipheredPhrase.append(" ");
			}
		}
		
		return decipheredPhrase.toString();
	}

	private void enviarArquivo() throws FileNotFoundException {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();

		body.add("answer", new InputStreamResource(new FileInputStream(getPathToFile())));
		HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<MultiValueMap<String, Object>>(body, headers);

		RestTemplate restTemplate = new RestTemplate();
		
		ResponseEntity<String> response = restTemplate.postForEntity(URI, requestEntity, String.class);
	}

	private String getDecipheredWord(String cipheredWord, Integer numeroCasas) {

		StringBuilder sb = new StringBuilder();
		char[] charArray = cipheredWord.toCharArray();

		for (char c : charArray) {

			Integer valueKey = getAlphabet().get(String.valueOf(c));

			if (valueKey != null) {

				Integer possibleCorrecValue = valueKey - numeroCasas;

				if (possibleCorrecValue > 0) {
					sb.append(findLetterByValue(possibleCorrecValue));

				} else {
					int sizeMap = getAlphabet().values().size();
					Integer result = new Integer(sizeMap) + possibleCorrecValue;
					sb.append(findLetterByValue(result));
				}

				// senao tem no mapa, só retorno
			} else {
				sb.append(c);
			}

		}

		return sb.toString();
	}

	private String findLetterByValue(Integer possibleCorrecValue) {
		String letter = null;
		for (Entry<String, Integer> entry : getAlphabet().entrySet()) {
			if (entry.getValue().equals(possibleCorrecValue)) {
				letter = entry.getKey();
				break;
			}
		}

		return letter;
	}

	String getPathToFile() {
		return System.getProperty("java.io.tmpdir").concat(File.separator).concat("answer.json");
	}

	private void saveFile(String json) throws IOException {
		try {
			String path = getPathToFile();
			FileWriter fileWriter = new FileWriter(path);
			fileWriter.write(json);
			fileWriter.close();

		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
	}

	private JSONObject parseStringToJson(String stringJson) {

		return new JSONObject(stringJson);
	}

	private String getJsonString() {
		InputStream inputStream = App.class.getResourceAsStream("answerbkp.json");

		StringBuilder sb = new StringBuilder();

		try {
			int len;
			while ((len = inputStream.read()) != -1) {
				sb.append((char) len);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return sb.toString();
	}

	private Map<String, Integer> getAlphabet() {
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("a", 1);
		map.put("b", 2);
		map.put("c", 3);
		map.put("d", 4);
		map.put("e", 5);
		map.put("f", 6);
		map.put("g", 7);
		map.put("h", 8);
		map.put("i", 9);
		map.put("j", 10);
		map.put("k", 11);
		map.put("l", 12);
		map.put("m", 13);
		map.put("n", 14);
		map.put("o", 15);
		map.put("p", 16);
		map.put("q", 17);
		map.put("r", 18);
		map.put("s", 19);
		map.put("t", 20);
		map.put("u", 21);
		map.put("v", 22);
		map.put("w", 23);
		map.put("x", 24);
		map.put("y", 25);
		map.put("z", 26);

		return map;
	}

}
