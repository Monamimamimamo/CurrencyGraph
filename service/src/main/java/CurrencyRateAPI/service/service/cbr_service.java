package CurrencyRateAPI.service.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import reactor.core.publisher.Mono;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.function.Function;

@Service
@AllArgsConstructor
public class cbr_service {

    private final WebClient webClient = WebClient.builder().baseUrl("http://www.cbr.ru").build();

    public Mono<NodeList> getCurrencyNamesAndValues(String url, String tag) {
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(xmlData -> parseCurrencyData(xmlData, tag));
    }

    private Mono<NodeList> parseCurrencyData(String xmlData, String tag) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(xmlData)));
            Element root = document.getDocumentElement();
            NodeList valuteList = root.getElementsByTagName(tag);
            return Mono.justOrEmpty(valuteList);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            return Mono.error(e); // Возвращаем ошибку, если произошла ошибка
        }
    }
}
