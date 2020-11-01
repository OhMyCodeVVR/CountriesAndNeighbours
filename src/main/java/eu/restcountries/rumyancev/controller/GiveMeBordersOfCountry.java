package eu.restcountries.rumyancev.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class GiveMeBordersOfCountry {

    private RestTemplate restTemplate;

    //адрес страницы содержащей описания всех стран.
    String url = "https://restcountries.eu/rest/v2/all";

    public GiveMeBordersOfCountry(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    //получить список всех стран.
    @GetMapping("/all")
    public String getAll() {
        return this.restTemplate.getForObject(url, String.class);
    }

    //получить список стран граничащих с запрашиваемой страной.
    @GetMapping("/getBorders/{countryName}")
    public String getBorders(@PathVariable String countryName) throws JsonProcessingException {
        String findC = restTemplate.getForObject(url, String.class);
        ObjectMapper mapper = new ObjectMapper();
        String tempString = "";

        //строка возвращаемая методом. наполняется значениями из JsonNode при обращении к заданным полям по имени.
        String responseString = "";

        //читаем всю страницу как JsonNode для обращения к его узлам по имени.
        JsonNode jsonNode = mapper.readTree(findC);
        JsonNode alpha3Codes = null;

        //цикл в котором мы получаем список alpha3codes граничащих стран.
        for (int i = 0; i < jsonNode.size(); i++) {
            if (jsonNode.get(i).get("name").asText().equals(countryName)) {
                alpha3Codes = jsonNode.get(i).get("borders");
            }
        }

        //если список alpha3code пуст возвращаем ответ о неуспешном поиске, иначе входим в циклы.
        if (alpha3Codes == null) {
            responseString = "This country has no neighbours, or there is no such country.";
        } else {
            //получаем в tempString  alpha3code для поиска по нему страны-соседа.
            for (int j = 0; j < alpha3Codes.size(); j++) {
                tempString = alpha3Codes.get(j).asText();
                //ищем в узлах JsonNode совпадение по alpha3code.
                for (int i = 0; i < jsonNode.size(); i++) {
                    //если совпадение найдено, наполняем строку для возврата методом.
                    if (jsonNode.get(i).get("alpha3Code").asText().equals(tempString)) {
                        //получаем список языков в виде JsonNode.
                        JsonNode languages = jsonNode.get(i).get("languages");
                        //если языков несколько в стране.
                        if (languages.size() > 1) {
                            String langList = "Languages ";
                            //получаем список языков в строку.
                            for (JsonNode langNode : languages) {
                                langList += langNode.get("name").asText() + ", ";
                            }
                            //заменяем запятую в конце списка языков на символ '.';
                            langList = langList.replaceAll(", $", ".");
                            //заносим в строку ответа значения.
                            responseString += jsonNode.get(i).get("name").asText() + ". Capital " + jsonNode.get(i).get("capital").asText() + ". " + langList + "<br>";
                        } else {
                            //если язык в стране один.
                            //заносим в строку ответа значения.
                            responseString += jsonNode.get(i).get("name").asText() + ". Capital " + jsonNode.get(i).get("capital").asText() + ". Language " + languages.get(0).get("name").asText() + ".<br>";
                        }
                    }
                }
            }
        }
        //заменяем теги <br> для web формата на \n, для консоли. отправляем ответ в консоль.
        System.out.println(responseString.replaceAll("<br>", "\n"));

        //отправляем ответ в формате web-страницы.
        return responseString;
    }
}
