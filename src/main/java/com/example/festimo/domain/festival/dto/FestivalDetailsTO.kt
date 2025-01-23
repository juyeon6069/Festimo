package com.example.festimo.domain.festival.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class FestivalDetailsTO {
    private List<Detail> details = new ArrayList<>();

    @Getter
    @Setter
    public static class Detail {
        private int contentId;
        private String infoName;
        private String infoText;

        public Detail(String infoName, String infoText) {
            this.infoName = infoName;
            this.infoText = infoText;
        }
    }


}
