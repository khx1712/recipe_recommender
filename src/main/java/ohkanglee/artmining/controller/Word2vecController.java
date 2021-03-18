package ohkanglee.artmining.controller;

import ohkanglee.artmining.service.Word2VecService;
import ohkanglee.artmining.dto.recipeDto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        word2VecService.Word2vecTest();
        URI location_uri = new URI("/admin/test");
        return ResponseEntity.created(location_uri).body("model을 load하여 학습을 시작하였습니다.");
    }

    @GetMapping("/user/recommend")
    public ResponseEntity<?> recommend(
            @RequestBody ingredientList list,
            @RequestParam("size") Integer uriSize
    ) throws URISyntaxException {
        List<String> uriList = word2VecService.Word2vecRecommend(list, uriSize);
        Map<String, Object> meta = new HashMap<>();
        meta.put("success", true);
        meta.put("msg", "추천 레시피 URI 리스트입니다.");
        ResRecipeDto<?,?> resRecipeDto = ResRecipeDto.builder()
                .meta(meta)
                .url(uriList)
                .build();
        URI location_uri = new URI("/admin/recommend");
        return ResponseEntity.created(location_uri).body(resRecipeDto);
    }

    @GetMapping("/user/recommend2")
    public ResponseEntity<?> recommend2(
            @RequestParam("ingredient") String list,
            @RequestParam("size") Integer uriSize
    ) throws URISyntaxException {
        List<String> uriList = word2VecService.Word2vecRecommend2(list, uriSize);
        Map<String, Object> meta = new HashMap<>();
        meta.put("success", true);
        meta.put("msg", "추천 레시피 URI 리스트입니다.");
        ResRecipeDto<?,?> resRecipeDto = ResRecipeDto.builder()
                .meta(meta)
                .url(uriList)
                .build();
        URI location_uri = new URI("/admin/recommend2");
        return ResponseEntity.created(location_uri).body(resRecipeDto);
    }
}