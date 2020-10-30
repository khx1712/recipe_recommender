package ohkanglee.artmining.controller;

import ohkanglee.artmining.service.Word2VecService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@RestController
public class Word2vecController {

    private Logger logger = LoggerFactory.getLogger(ApplicationRunner.class);

    @Autowired
    Word2VecService word2VecService;

    @PostMapping("/admin/train")
    public ResponseEntity<?> train(
            Authentication authentication
    ) throws IOException, URISyntaxException {
        word2VecService.Word2vecTrain();
        URI location_uri = new URI("/admin/train");
        return ResponseEntity.created(location_uri).body("학습이 완료되었습니다.");
    }

    @PostMapping("/admin/test")
    public ResponseEntity<?> test(
            Authentication authentication
    ) throws IOException, URISyntaxException {
        word2VecService.Word2vecTrain();
        URI location_uri = new URI("/admin/test");
        return ResponseEntity.created(location_uri).body("학습이 완료되었습니다.");
    }
}
