package ohkanglee.artmining.dto;

import lombok.*;

import java.util.List;

public class recipeDto {

    @Getter
    public  static class ingredientList{
        List<String> list;
    }

    @Getter
    public  static class ingredientList2{
        String list;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResRecipeDto<T, U> extends ResponseDto<T>{
        private T meta;
        //        private D documents;
        private U url;
    }
}